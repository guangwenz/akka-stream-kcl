package io.github.guangwen.kinesis

import java.util.concurrent.Semaphore

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Source}
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.{IRecordProcessor, IRecordProcessorFactory}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{KinesisClientLibConfiguration, Worker}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.Exception

object KinesisSource {

  /**
   * create a kinesis source to consume records from Kinesis with KCL library version 2.
   * @param settings
   * @param ec
   * @return
   */
  def source(settings: KinesisSourceSettings)(implicit ec: ExecutionContext): Source[ProcessRecordsInputWithInitializationInput, Worker] = {
    val credentialProvider = DefaultAWSCredentialsProviderChain.getInstance()
    import settings._

    Source
      .queue[ProcessRecordsInputWithInitializationInput](settings.bufferSize, OverflowStrategy.backpressure)
      .watchTermination()(Keep.both)
      .mapMaterializedValue {
        case (queue, watch) =>
          val semaphore = new Semaphore(1, true)
          val worker: Worker = new Worker.Builder()
            .config(new KinesisClientLibConfiguration(applicationName, streamName, credentialProvider, workerId))
            .recordProcessorFactory(new IRecordProcessorFactory {
              override def createProcessor(): IRecordProcessor = RecordProcessor(terminateGracePeriod, { input =>
                semaphore.acquire(1)
                (Exception.nonFatalCatch either Await.result(queue.offer(input), backPressureTimeOut) left).foreach(exp => queue.fail(exp))
                semaphore.release()
              })
            })
            .build()

          Future(worker.run()).onComplete {
            case scala.util.Failure(exception) =>
              queue.fail(exception)
            case scala.util.Success(_) =>
              queue.complete()
          }
          watch.onComplete(_ => worker.shutdown())
          worker
      }
  }
}
