package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.EnvironmentUtil
import com.intellij.util.ui.FormBuilder
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvBuilder
import io.github.cdimascio.dotenv.DotenvException
import org.jetbrains.annotations.VisibleForTesting
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Function
import javax.swing.JComponent


@Service(Service.Level.PROJECT)
class GraphQLConfigEnvironment(private val project: Project) : ModificationTracker {
    companion object {
        @JvmField
        @VisibleForTesting
        var getEnvVariable = Function<String?, String?> { key: String? -> key?.let { System.getProperty(it) } }

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLConfigEnvironment>()

        private val FILENAMES = linkedSetOf(
            ".env.local",
            ".env.development.local",
            ".env.development",
            ".env.dev.local",
            ".env.dev",
            ".env"
        )
    }

    private val explicitVariables: ConcurrentMap<String, String> = ConcurrentHashMap()
    private val modificationTracker = SimpleModificationTracker()

    fun createSnapshot(variables: Collection<String>): GraphQLEnvironmentSnapshot {
        return GraphQLEnvironmentSnapshot(variables.associateWith { getVariable(it) })
    }

    fun setExplicitVariable(name: String, value: String?) {
        if (value == null) {
            explicitVariables.remove(name)
        } else {
            explicitVariables[name] = value
        }

        notifyEnvironmentChanged()
    }

    fun getExplicitVariable(name: String): String? {
        return explicitVariables[name]
    }

    fun getVariable(name: String?, dir: VirtualFile? = null): String? {
        if (name.isNullOrBlank()) {
            return null
        }

        // Try to load the variable from the jvm parameters
        var value = getEnvVariable.apply(name)
        if (value.isNullOrBlank()) {
            value = tryToGetVariableFromDotEnvFile(name, dir)
        }
        if (value.isNullOrBlank()) {
            value = explicitVariables[name]
        }
        if (value.isNullOrBlank()) {
            value = EnvironmentUtil.getValue(name)
        }
        return value
    }

    private fun tryToGetVariableFromDotEnvFile(varName: String, dir: VirtualFile?): String? {
        var value: String? = null

        if (dir != null) {
            value = findVariableValueInDirectory(dir, varName)
        }

        if (value == null) {
            value = project.guessProjectDir()?.let { findVariableValueInDirectory(it, varName) }
        }
        return value
    }

    private fun findVariableValueInDirectory(dir: VirtualFile, name: String): String? {
        val filename = findEnvFileInDirectory(dir) ?: return null
        return try {
            val dotenv = createDotenvBuilder(dir.path).filename(filename).load()
            dotenv[name]
        } catch (e: DotenvException) {
            thisLogger().warn(e)
            null
        }
    }

    private fun findEnvFileInDirectory(dir: VirtualFile): String? {
        if (!dir.isDirectory) return null

        for (candidate in FILENAMES) {
            val file = dir.findChild(candidate)
            if (file != null && file.exists()) {
                return candidate
            }
        }
        return null
    }

    fun notifyEnvironmentChanged() {
        invokeLater {
            modificationTracker.incModificationCount()
            project.messageBus.syncPublisher(GraphQLConfigEnvironmentListener.TOPIC).onEnvironmentChanged()
        }
    }

    private fun createDotenvBuilder(path: String): DotenvBuilder {
        return Dotenv
            .configure()
            .directory(path)
            .ignoreIfMalformed()
            .ignoreIfMissing()
    }

    override fun getModificationCount(): Long = modificationTracker.modificationCount
}

class GraphQLVariableDialog(private val project: Project, private val name: String) : DialogWrapper(project) {

    private val textField = JBTextField().apply {
        text = GraphQLConfigEnvironment.getInstance(project).getExplicitVariable(name).orEmpty()
    }

    val value: String
        get() = textField.text

    init {
        title = "Enter Missing GraphQL \"$name\" Environment Variable"
        init()
    }

    override fun createCenterPanel(): JComponent? {
        myPreferredFocusedComponent = textField
        val hint =
            JBLabel("<html><b>Hint</b>: Specify environment variables using <code>-DvarName=varValue</code> on the IDE command line.<div style=\"margin-top: 6\">This dialog stores the entered value until the IDE is restarted.</div></html>")
        return FormBuilder.createFormBuilder().addLabeledComponent(name, textField).addComponent(hint).panel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return textField
    }

    override fun doOKAction() {
        GraphQLConfigEnvironment.getInstance(project).setExplicitVariable(name, value)

        super.doOKAction()
    }
}

data class GraphQLEnvironmentSnapshot(val variables: Map<String, String?>) {
    companion object {
        val EMPTY = GraphQLEnvironmentSnapshot(emptyMap())
    }
}
