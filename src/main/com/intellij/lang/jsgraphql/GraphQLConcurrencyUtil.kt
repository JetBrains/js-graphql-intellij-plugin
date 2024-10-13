package com.intellij.lang.jsgraphql

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import java.util.concurrent.*

private val LOG = fileLogger()

fun inEdt(modalityState: ModalityState? = null) = Executor { runnable -> runInEdt(modalityState) { runnable.run() } }

fun inWriteAction(modalityState: ModalityState? = null) = Executor { runnable ->
  runInEdt(modalityState) {
    ApplicationManager.getApplication().runWriteAction(runnable)
  }
}

fun isCancellation(error: Throwable?): Boolean {
  if (error is ExecutionException) {
    return error.cause?.let(::isCancellation) ?: false
  }

  return error is ProcessCanceledException
         || error is CancellationException
         || error is InterruptedException
}

fun executeOnPooledThread(runnable: () -> Unit) {
  val app = ApplicationManager.getApplication()
  if (app.isUnitTestMode) {
    invokeLater(ModalityState.defaultModalityState(), runnable)
  }
  else {
    app.executeOnPooledThread(runnable)
  }
}

private const val QUOTA_MILLS: Long = 5

internal fun <T> awaitFuture(future: Future<T?>, timeoutMills: Long): T? {
  if (ApplicationManager.getApplication().isDispatchThread() &&
      !ApplicationManager.getApplication().isHeadlessEnvironment()
  ) {
    LOG.error("Await future on EDT may cause a deadlock");
    return null
  }

  ProgressManager.checkCanceled()

  try {
    if (future.isDone) {
      return future.get()
    }

    var totalWait = timeoutMills
    while ((timeoutMills < 0 || totalWait > 0)) {
      try {
        return future.get(QUOTA_MILLS, TimeUnit.MILLISECONDS)
      }
      catch (_: TimeoutException) {
        //no action
      }
      catch (e: ExecutionException) {
        if (e.cause !is CancellationException) {
          LOG.info(e)
        }
        return null
      }
      catch (_: CancellationException) {
        return null
      }
      catch (_: InterruptedException) {
        return null
      }
      totalWait -= QUOTA_MILLS

      ProgressManager.checkCanceled()
    }
    if (future.isDone) {
      return future.get()
    }
    else {
      LOG.debug("Timeout waiting for $timeoutMills ms.")
    }
  }
  catch (e: ProcessCanceledException) {
    throw e
  }
  catch (e: ExecutionException) {
    if (e.cause !is CancellationException) {
      LOG.info("Awaiting future failed with exception", e)
    }
  }
  catch (_: Exception) {
    //no actions
  }

  return null
}
