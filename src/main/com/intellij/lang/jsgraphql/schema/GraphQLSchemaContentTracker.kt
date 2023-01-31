/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.json.psi.JsonFile
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionSearchHelper
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLOperationDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLTemplateDefinition
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Alarm


/**
 * Tracks PSI changes that can affect declared GraphQL schemas.
 * For configuration changes use [com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider].
 */
@Service
class GraphQLSchemaContentTracker(private val myProject: Project) : Disposable, ModificationTracker {

    companion object {
        private val LOG = logger<GraphQLSchemaContentTracker>()


        private const val EVENT_PUBLISH_TIMEOUT = 300

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLSchemaContentTracker>()
    }

    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private val myModificationTracker = SimpleModificationTracker()

    init {
        PsiManager.getInstance(myProject).addPsiTreeChangeListener(PsiChangeListener(), this)
    }

    fun schemaChanged() {
        LOG.debug("GraphQL schema cache invalidated", if (LOG.isTraceEnabled) Throwable() else null)

        alarm.cancelAllRequests()
        alarm.addRequest({
            myModificationTracker.incModificationCount()
            myProject.messageBus.syncPublisher(GraphQLSchemaContentChangeListener.TOPIC).onSchemaChanged()
        }, EVENT_PUBLISH_TIMEOUT)
    }

    override fun getModificationCount(): Long {
        return myModificationTracker.modificationCount
    }

    override fun dispose() {}

    /**
     * always consider the schema changed when editing an endpoint file
     * change in injection target
     * ignore the generic event which fires for all other cases above
     * if it's not the generic case, children have been replaced, e.g. using the commenter
     */
    private inner class PsiChangeListener : PsiTreeChangeAdapter() {
        private fun checkForSchemaChange(event: PsiTreeChangeEvent) {
            if (myProject.isDisposed) {
                return
            }

            if (event.file is GraphQLFile) {
                if (affectsGraphQLSchema(event)) {
                    schemaChanged()
                }
            }

            if (event.parent is PsiLanguageInjectionHost) {
                val injectionHelper = GraphQLInjectionSearchHelper.getInstance()
                if (injectionHelper != null && injectionHelper.isGraphQLLanguageInjectionTarget(event.parent)) {
                    // change in injection target
                    schemaChanged()
                }
            }

            if (event.file is JsonFile) {
                var introspectionJsonUpdated = false
                if (event.file?.getUserData(GraphQLSchemaKeys.GRAPHQL_INTROSPECTION_JSON_TO_SDL) != null) {
                    introspectionJsonUpdated = true
                } else {
                    val virtualFile = event.file?.virtualFile
                    if (virtualFile?.getUserData(GraphQLSchemaKeys.IS_GRAPHQL_INTROSPECTION_JSON) == true) {
                        introspectionJsonUpdated = true
                    }
                }
                if (introspectionJsonUpdated) {
                    schemaChanged()
                }
            }
        }

        override fun propertyChanged(event: PsiTreeChangeEvent) {
            checkForSchemaChange(event)
        }

        override fun childAdded(event: PsiTreeChangeEvent) {
            checkForSchemaChange(event)
        }

        override fun childRemoved(event: PsiTreeChangeEvent) {
            checkForSchemaChange(event)
        }

        override fun childMoved(event: PsiTreeChangeEvent) {
            checkForSchemaChange(event)
        }

        override fun childReplaced(event: PsiTreeChangeEvent) {
            checkForSchemaChange(event)
        }

        override fun childrenChanged(event: PsiTreeChangeEvent) {
            if (event is PsiTreeChangeEventImpl) {
                if (!event.isGenericChange) {
                    // ignore the generic event which fires for all other cases above
                    // if it's not the generic case, children have been replaced, e.g. using the commenter
                    checkForSchemaChange(event)
                }
            }
        }

        /**
         * Evaluates whether the change event can affect the associated GraphQL schema
         *
         * @param event the event that occurred
         * @return true if the change can affect the declared schema
         */
        private fun affectsGraphQLSchema(event: PsiTreeChangeEvent): Boolean {
            if (PsiTreeChangeEvent.PROP_FILE_NAME == event.propertyName || PsiTreeChangeEvent.PROP_DIRECTORY_NAME == event.propertyName) {
                // renamed and moves are likely to affect schema blobs etc.
                return true
            }
            val elements = sequenceOf(event.parent, event.child, event.newChild, event.oldChild)
            for (element in elements) {
                if (element == null) {
                    continue
                }

                val containingDeclaration = PsiTreeUtil.findFirstParent(element) {
                    it is GraphQLOperationDefinition ||
                        it is GraphQLFragmentDefinition ||
                        it is GraphQLTemplateDefinition
                }

                if (containingDeclaration != null) {
                    // edits inside query, mutation, subscription, fragment etc. don't affect the schema
                    return false
                }
            }
            // fallback to assume the schema can be affected by the edit
            return true
        }
    }
}
