package io.github.guangwen.kinesis

import scala.concurrent.duration._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Zip}
import akka.stream.{ActorAttributes, FlowShape, Supervision}
import akka.{Done, NotUsed}
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

object CheckpointFlows {
  private val LOGGER = LoggerFactory.getLogger(getClass)
  private val MAX_KINESIS_SHARDS = 500

  final case class CheckpointSettings(batchSize: Int, batchWait: FiniteDuration)

  object CheckpointSettings {
    def default: CheckpointSettings = CheckpointSettings(1000, 10.seconds)
  }

  def checkpointSink(checkpointSettings: CheckpointSettings = CheckpointSettings.default)(implicit ec: ExecutionContext): Sink[ProcessRecordsInputWithInitializationInput, NotUsed] = {
    checkpointFlow(checkpointSettings).to(Sink.ignore)
  }
  /**
   * add checkpoint call.
   *
   * @param checkpointSettings, settings
   * @param ec, execution context
   * @return
   */
  def checkpointFlow(checkpointSettings: CheckpointSettings = CheckpointSettings.default)(implicit ec: ExecutionContext): Flow[ProcessRecordsInputWithInitializationInput, ProcessRecordsInputWithInitializationInput, NotUsed] = {
    Flow[ProcessRecordsInputWithInitializationInput]
      .groupBy(MAX_KINESIS_SHARDS, _.initializationInput.getShardId)
      .groupedWithin(checkpointSettings.batchSize, checkpointSettings.batchWait)
      .via(GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._
        val broadcast = b.add(Broadcast[Seq[ProcessRecordsInputWithInitializationInput]](2))
        val zip = b.add(Zip[Done, Seq[ProcessRecordsInputWithInitializationInput]])
        val flow = b.add(Flow[ProcessRecordsInputWithInitializationInput])

        broadcast
          .out(0)
          .map(_.max)
          .mapAsync(1) { r =>
            val f = Future {
              r.processRecordsInput.getCheckpointer.checkpoint()
              Done
            }
            f.failed.foreach(exp => LOGGER.error("can not checkpoint", exp))
            f
          } ~> zip.in0
        broadcast.out(1) ~> zip.in1
        zip.out.map(_._2).mapConcat(_.toList) ~> flow.in

        FlowShape(broadcast.in, flow.out)
      })
      .mergeSubstreams
      .withAttributes(ActorAttributes.supervisionStrategy {
        case _: ShutdownException =>
          Supervision.Resume
        case _ =>
          Supervision.Stop
      })

  }
}
