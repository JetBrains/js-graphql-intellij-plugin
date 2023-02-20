package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.VfsUtil
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

    fun createSnapshot(variables: Collection<String>, dir: VirtualFile?): GraphQLEnvironmentSnapshot {
        return GraphQLEnvironmentSnapshot(variables.associateWith { getVariable(it, dir) })
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

    fun getVariable(name: String?, dir: VirtualFile?): String? {
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
        var value = findClosestEnvFile(dir)?.let { findVariableValueInFile(it, varName) }
        if (value == null) {
            value = project.guessProjectDir()
                ?.let { findEnvFileInDirectory(it) }
                ?.let { findVariableValueInFile(it, varName) }
        }
        return value
    }

    private fun findClosestEnvFile(dir: VirtualFile?): VirtualFile? {
        if (dir == null) return null
        var file: VirtualFile? = null
        GraphQLResolveUtil.processDirectoriesUpToContentRoot(project, dir) {
            file = findEnvFileInDirectory(it)
            file == null
        }
        return file
    }

    private fun findVariableValueInFile(file: VirtualFile, name: String): String? {
        return try {
            if (ApplicationManager.getApplication().isUnitTestMode) {
                return loadFromVirtualFile(file)[name]
            }

            val dotenv = createDotenvBuilder(file.parent.path).filename(file.name).load()
            dotenv[name]
        } catch (e: DotenvException) {
            thisLogger().warn(e)
            null
        }
    }

    // test only, dotenv can't read from the TempFS
    private fun loadFromVirtualFile(file: VirtualFile?): Map<String, String?> {
        if (file == null) return emptyMap()
        return VfsUtil.loadText(file)
            .lines()
            .asSequence()
            .map { it.split('=', limit = 2) }
            .filter { it.isNotEmpty() }
            .associate { it[0].trim() to it.getOrNull(1)?.trim() }
    }

    private fun findEnvFileInDirectory(dir: VirtualFile): VirtualFile? {
        if (!dir.isDirectory) return null

        for (candidate in FILENAMES) {
            val file = dir.findChild(candidate)
            if (file != null && file.isValid) {
                return file
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
