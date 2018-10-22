package io.github.guangwen.kinesis

import scala.concurrent.duration._

class KinesisSourceSettings(
  val applicationName: String,
  val streamName: String,
  val workerId: String,
  val bufferSize: Int,
  val backPressureTimeOut: FiniteDuration,
  val terminateGracePeriod: FiniteDuration)

object KinesisSourceSettings {
  private def getWorkerId: String = s"${
    import scala.sys.process._
    "hostname".!!.trim
  }:${java.util.UUID.randomUUID()}"

  def apply(applicationName: String, streamName: String): KinesisSourceSettings = {
    KinesisSourceSettings(applicationName, streamName, getWorkerId)
  }

  def apply(applicationName: String, streamName: String, workerId: String): KinesisSourceSettings = {
    new KinesisSourceSettings(applicationName, streamName, workerId, 1000, 1.minute, 1.minute)
  }

  def apply(applicationName: String, streamName: String, bufferSize: Int): KinesisSourceSettings = {
    new KinesisSourceSettings(applicationName, streamName, getWorkerId, bufferSize, 1.minute, 1.minute)
  }

  def apply(applicationName: String, streamName: String, bufferSize: Int, backPressureTimeout: FiniteDuration): KinesisSourceSettings = {
    new KinesisSourceSettings(applicationName, streamName, getWorkerId, bufferSize, backPressureTimeout, 1.minute)
  }

  def apply(applicationName: String, streamName: String, bufferSize: Int, backPressureTimeout: FiniteDuration, terminateGraceTimeout: FiniteDuration): KinesisSourceSettings = {
    new KinesisSourceSettings(applicationName, streamName, getWorkerId, bufferSize, backPressureTimeout, terminateGraceTimeout)
  }
}
