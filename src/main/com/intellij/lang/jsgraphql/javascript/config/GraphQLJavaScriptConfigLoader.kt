package com.intellij.lang.jsgraphql.javascript.config

import com.google.gson.Gson
import com.intellij.execution.process.CapturingProcessRunner
import com.intellij.execution.process.ProcessOutput
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.execution.NodeTargetRunOptions
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.settings.NodeSettingsConfigurable
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigCustomLoader
import com.intellij.lang.jsgraphql.ide.notifications.GRAPHQL_NOTIFICATION_GROUP_ID
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import java.util.concurrent.atomic.AtomicBoolean

private const val TIMEOUT = 15000

private val LOG = logger<GraphQLJavaScriptConfigLoader>()

private val EXTENSIONS = setOf("js", "ts", "cjs")

class GraphQLJavaScriptConfigLoader : GraphQLConfigCustomLoader {

  private val marker = "___GRAPHQL_LOADER___"

  private val interpreterNotificationShown = AtomicBoolean()

  override fun accepts(file: VirtualFile): Boolean {
    return file.extension in EXTENSIONS
  }

  override fun load(project: Project, file: VirtualFile): Map<*, *>? {
    val interpreter = getInterpreter(project)
    if (interpreter == null) {
      if (interpreterNotificationShown.compareAndSet(false, true)) {
        val notification = Notification(
          GRAPHQL_NOTIFICATION_GROUP_ID,
          GraphQLBundle.message("graphql.config.error.title"),
          GraphQLBundle.message("graphql.config.evaluation.interpreter.not.found.error", file.name),
          NotificationType.WARNING
        ).addAction(NodeSettingsConfigurable.createConfigureInterpreterAction(project, null))
        Notifications.Bus.notify(notification)
      }

      throw RuntimeException(GraphQLBundle.message("graphql.config.node.interpreter.error"))
    }

    val (_, run) = run(project, interpreter, file)
    val stdout = run.stdout.trim()
    val stderr = run.stderr.trim()

    if (run.exitCode == 0) {
      LOG.debug { "Evaluated ${file.path} config: stdout=$stdout\nstderr=$stderr" }
      val lastNewLine = stdout.lastIndexOf(marker)
      val startIndex = lastNewLine + marker.length + 1
      val result = if (lastNewLine >= 0 && stdout.length >= startIndex) stdout.substring(startIndex) else stdout
      if (result.isBlank() && stderr.isNotEmpty()) {
        completeExceptionally(file, run)
      }

      try {
        return parseJsonResult(result)
      }
      catch (e: Exception) {
        LOG.warn("${e.message}\nstdout: ${stdout}\nstderr: $stderr", e)
        throw RuntimeException(GraphQLBundle.message("graphql.config.evaluation.error"), e)
      }
    }
    else {
      completeExceptionally(file, run)
    }
  }

  private fun completeExceptionally(file: VirtualFile, run: ProcessOutput): Nothing {
    LOG.warn(
      """
            Failed to evaluate ${file.path} config. Exit code: ${run.exitCode}${if (run.isTimeout) ", timed out" else ""}
            stdout: ${run.stdout}
            stderr: ${run.stderr}
            """.trimIndent()
    )

    val errorDetails = run.stderr.trim()
    if (errorDetails.isNotEmpty()) {
      throw RuntimeException(GraphQLBundle.message("graphql.config.evaluation.error"), Throwable(run.stderr))
    }
    else {
      throw RuntimeException(GraphQLBundle.message("graphql.config.evaluation.error"))
    }
  }

  private fun parseJsonResult(result: String): Map<*, *>? {
    return Gson().fromJson(result, Map::class.java)
  }

  private fun run(
    project: Project,
    interpreter: NodeJsInterpreter,
    file: VirtualFile,
  ): Pair<NodeTargetRun, ProcessOutput> {
    val packageJson = findPackageJson(file)
    val workingDir = file.parent
    val isESM = if (packageJson != null) PackageJsonData.getOrCreate(packageJson).isModuleType else false
    val targetRun = createTargetRun(interpreter, project, workingDir.path)
    LOG.info("Loading ${file.path} config")
    configureCommandLine(targetRun, file.path, isESM)
    val processHandler = targetRun.startProcessEx().processHandler
    val processOutput = CapturingProcessRunner(processHandler).runProcess(TIMEOUT, true)
    return Pair(targetRun, processOutput)
  }

  private fun createTargetRun(interpreter: NodeJsInterpreter, project: Project, workingDir: String): NodeTargetRun {
    val targetRun = NodeTargetRun(interpreter, project, null, NodeTargetRunOptions.of(false))
    targetRun.commandLineBuilder.setWorkingDirectory(targetRun.path(workingDir))
    targetRun.commandLineBuilder.addEnvironmentVariable("NODE_ENV", "development")
    return targetRun
  }

  private fun configureCommandLine(
    targetRun: NodeTargetRun,
    path: String,
    isESM: Boolean,
  ) {
    val filePath = "./${PathUtil.getFileName(path)}"

    val commandLine = targetRun.commandLineBuilder
    if (isESM) {
      commandLine.addParameter("--input-type")
      commandLine.addParameter("module")
    }

    if (TypeScriptUtil.isTypeScriptFile(path)) {
      if (isESM) {
        commandLine.addParameter("--loader")
        commandLine.addParameter("ts-node/esm")
      }
      else {
        commandLine.addParameter("-r")
        commandLine.addParameter("ts-node/register")
      }
    }

    val runnable = if (isESM) {
      //language=JavaScript
      """
            import(modulePath).then(config => printConfig(config)).catch(err => console.error(err));
            """.trimIndent()
    }
    else {
      //language=JavaScript
      """
            var config = require(modulePath);
            printConfig(config);
            """.trimIndent()
    }

    commandLine.addParameter("-e")
    //language=JavaScript
    commandLine.addParameter(
      """
            function printConfig(config) {
              if (config.default !== undefined) config = config.default;

              console.log("$marker");
              console.log(JSON.stringify(config, null, 2));
              process.exit(0);
            }

            var modulePath = '${StringUtil.escapeBackSlashes(filePath)}';
            $runnable
            """.trimIndent().trim()
    )
  }

  private fun getInterpreter(project: Project): NodeJsInterpreter? {
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
    return if (interpreter != null && interpreter.validate(project) == null) interpreter else null
  }

  private fun findPackageJson(from: VirtualFile?): VirtualFile? {
    if (from == null) {
      return null
    }
    var packageJson = PackageJsonUtil.findUpPackageJson(from)
    while (packageJson != null && JSLibraryUtil.hasDirectoryInPath(packageJson, JSLibraryUtil.NODE_MODULES, null)) {
      packageJson = PackageJsonUtil.findUpPackageJson(packageJson.parent.parent)
    }
    return packageJson
  }
}
