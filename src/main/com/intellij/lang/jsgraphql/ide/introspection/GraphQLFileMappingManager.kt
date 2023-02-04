package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.lang.jsgraphql.GraphQLLanguage
import com.intellij.lang.jsgraphql.GraphQLSettings
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import java.io.IOException
import java.lang.ref.WeakReference

@Service
class GraphQLFileMappingManager(private val project: Project) {
    companion object {
        private val LOG = logger<GraphQLFileMappingManager>()

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLFileMappingManager>()

        /**
         * Set on a source file (only PSI) to get the generated file.
         */
        private val GRAPHQL_SOURCE_TO_SDL = Key.create<CachedValue<GraphQLFile>>("graphql.source.to.sdl")

        /**
         * Set on a source file (both PSI and VirtualFile) to check if it has the corresponding generated file.
         */
        private val GRAPHQL_SOURCE_HAS_GENERATED_FILE = Key.create<Boolean>("graphql.source.has.generated.file")

        /**
         * Set on the generated file (both PSI and VirtualFile) that is the generated file based on a different source.
         */
        private val GRAPHQL_IS_GENERATED_SDL = Key.create<Boolean>("graphql.is.generated.sdl")

        /**
         * Reverse mapping of [GRAPHQL_SOURCE_TO_SDL].
         * Set on a generated file (both PSI and VirtualFile) to get the source path in the VFS.
         */
        private val GRAPHQL_SOURCE_PATH = Key.create<String>("graphql.source.path")

        /**
         * Reverse mapping of [GRAPHQL_SOURCE_TO_SDL].
         * Set on a generated file (only PSI) to get the source file that the file is derived from.
         */
        private val GRAPHQL_SDL_TO_SOURCE = Key.create<WeakReference<PsiFile>>("graphql.sdl.to.source")

        /**
         * Set on a scratch Virtual File to indicate which project it's been associated with
         */
        private val GRAPHQL_SCRATCH_PROJECT_KEY = Key.create<String>("graphql.scratch.project.key")
    }

    fun getCachedIntrospectionSDL(psiFile: PsiFile): GraphQLFile? {
        val cachedValue = psiFile.getUserData(GRAPHQL_SOURCE_TO_SDL) ?: return null
        return if (cachedValue.hasUpToDateValue()) cachedValue.value else null
    }

    fun getOrCreateIntrospectionSDL(file: VirtualFile): GraphQLFile? {
        val psiFile = runReadAction { PsiManager.getInstance(project).findFile(file) } ?: return null

        return CachedValuesManager.getCachedValue(psiFile, GRAPHQL_SOURCE_TO_SDL) {
            val project = psiFile.project

            val introspection = try {
                GraphQLIntrospectionService.getInstance(project).printIntrospectionAsGraphQL(runReadAction { psiFile.text })
            } catch (e: ProcessCanceledException) {
                throw e
            } catch (e: Exception) {
                LOG.info("SDL generation failed for: ${file.path}", e)
                return@getCachedValue null
            }

            psiFile.putUserData(GRAPHQL_SOURCE_HAS_GENERATED_FILE, true)

            val psiFileFactory = PsiFileFactory.getInstance(project)
            val newIntrospectionFile =
                psiFileFactory.createFileFromText(file.path, GraphQLLanguage.INSTANCE, introspection) as GraphQLFile

            newIntrospectionFile.putUserData(GRAPHQL_IS_GENERATED_SDL, true)
            newIntrospectionFile.virtualFile.putUserData(GRAPHQL_IS_GENERATED_SDL, true)

            newIntrospectionFile.putUserData(GRAPHQL_SOURCE_PATH, file.path)
            newIntrospectionFile.virtualFile.putUserData(GRAPHQL_SOURCE_PATH, file.path)

            // only for PSI, we shouldn't store any PSI references in a VirtualFile
            newIntrospectionFile.putUserData(GRAPHQL_SDL_TO_SOURCE, WeakReference(psiFile))

            try {
                newIntrospectionFile.virtualFile.isWritable = false
            } catch (e: IOException) {
                LOG.warn(e)
            }

            CachedValueProvider.Result.create(newIntrospectionFile, psiFile, GraphQLSettings.getSettings(project).modificationTracker)
        }
    }

    fun isSourceForGeneratedFile(psiFile: PsiFile?): Boolean {
        return psiFile?.getUserData(GRAPHQL_SOURCE_HAS_GENERATED_FILE)
            ?: psiFile?.virtualFile?.getUserData(GRAPHQL_SOURCE_HAS_GENERATED_FILE)
            ?: false
    }

    fun isGeneratedFile(psiFile: PsiFile?): Boolean {
        return psiFile?.getUserData(GRAPHQL_IS_GENERATED_SDL) ?: isGeneratedFile(psiFile?.virtualFile)
    }

    fun isGeneratedFile(virtualFile: VirtualFile?): Boolean {
        return virtualFile?.getUserData(GRAPHQL_IS_GENERATED_SDL) ?: false
    }
}
