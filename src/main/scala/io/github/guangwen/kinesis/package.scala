package io.github.guangwen

import scala.collection.JavaConverters._
import com.amazonaws.services.kinesis.clientlibrary.types.{InitializationInput, ProcessRecordsInput, UserRecord}
import com.amazonaws.services.kinesis.model.Record

package object kinesis {
  type RecordHandler = ProcessRecordsInputWithInitializationInput => Unit

  final case class ProcessRecordsInputWithInitializationInput(
                                                               processRecordsInput: ProcessRecordsInput,
                                                               initializationInput: InitializationInput)

  implicit val orderingRecord: Ordering[Record] = Ordering[Long].on {
    case ur: UserRecord => ur.getSubSequenceNumber
    case _ => 0
  }
  implicit val orderingBySequenceNumber: Ordering[ProcessRecordsInputWithInitializationInput] = Ordering[(String, Long)].on { input =>
    val record = input.processRecordsInput.getRecords.asScala.max
    (input.initializationInput.getShardId, record match {
      case ur: UserRecord => ur.getSubSequenceNumber
      case _ => 0
    })
  }
}
