package io.github.guangwen.kinesis

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason
import com.amazonaws.services.kinesis.clientlibrary.types.{InitializationInput, ProcessRecordsInput, ShutdownInput}

import scala.concurrent.duration.FiniteDuration

class RecordProcessor(recordHandler: RecordHandler, gracePeriod: FiniteDuration) extends IRecordProcessor {
  private var _initializationInput: InitializationInput = _

  override def initialize(initializationInput: InitializationInput): Unit = {
    _initializationInput = initializationInput
  }

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {
    recordHandler(ProcessRecordsInputWithInitializationInput(processRecordsInput, _initializationInput))
  }

  override def shutdown(shutdownInput: ShutdownInput): Unit = {
    shutdownInput.getShutdownReason match {
      case ShutdownReason.TERMINATE =>
        shutdownInput.getCheckpointer.checkpoint()
        Thread.sleep(gracePeriod.toMillis)
      case ShutdownReason.ZOMBIE =>
      case ShutdownReason.REQUESTED =>
    }
  }
}

object RecordProcessor {
  def apply(graceTerminatePeriod: FiniteDuration, recordHandler: RecordHandler): RecordProcessor = new RecordProcessor(recordHandler, graceTerminatePeriod)
}
