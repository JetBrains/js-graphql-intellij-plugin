package com.intellij.lang.jsgraphql

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.checkCanceled
import com.intellij.util.ConcurrencyUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor

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

private const val QUOTA_MS = ConcurrencyUtil.DEFAULT_TIMEOUT_MS

internal suspend fun awaitCoroutine(job: Job, timeoutMills: Long) {
  checkCanceled()

  if (job.isCompleted) {
    job.join()
    return
  }

  var totalWait = timeoutMills
  while (totalWait > 0 && !job.isCompleted) {
    checkCanceled()

    delay(QUOTA_MS)
    totalWait -= QUOTA_MS
  }

  if (job.isCompleted) {
    job.join()
  }
}
