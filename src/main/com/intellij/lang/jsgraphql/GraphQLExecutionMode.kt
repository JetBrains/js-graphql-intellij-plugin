package com.intellij.lang.jsgraphql

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.TestOnly

/**
 * The execution mode of the GraphQL plugin.
 *
 * Synchronous mode: the configuration and the schema are computed on demand under the read action of the caller.
 * There is no daemon restart and there are no debounced collectors, because no editor will highlight the file again later.
 * It is the mode of a headless environment: a language server, Qodana, `idea inspect`, unit tests.
 *
 * Asynchronous mode: the configuration and the schema are computed by background jobs, and the daemon is restarted when they change.
 * It is the mode of the IDE.
 */
object GraphQLExecutionMode {
  @Volatile
  private var testOverride: Boolean? = null

  val isSynchronous: Boolean
    get() = testOverride ?: ApplicationManager.getApplication().isHeadlessEnvironment

  /**
   * Overrides [isSynchronous] until [parentDisposable] is disposed. Pass `null` to use the default mode.
   */
  @TestOnly
  fun setSynchronousForTests(value: Boolean?, parentDisposable: Disposable) {
    val previous = testOverride
    testOverride = value
    Disposer.register(parentDisposable) {
      testOverride = previous
    }
  }
}
