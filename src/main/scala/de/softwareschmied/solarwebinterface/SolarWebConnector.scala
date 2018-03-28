package de.softwareschmied.solarwebinterface

import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, TimeoutException}
import scala.concurrent.duration._

/**
  * Created by Thomas Becker (thomas.becker00@gmail.com) on 11.11.17.
  */
case class InverterResponse(body: InverterBody)

case class InverterBody(data: InverterData)

case class InverterData(dayEnergy: DayEnergy)

case class DayEnergy(unit: String, values: Values)

case class Values(value: Int)

case class MeterResponse(body: MeterBody)

case class MeterBody(data: MeterData)

case class MeterData(powerRealSum: Double, powerRealPhase1: Double, powerRealPhase2: Double, powerRealPhase3: Double, timestamp: Option[Long])

case class PowerFlowResponse(body: PowerFlowBody)

case class PowerFlowBody(data: PowerFlowData)

case class PowerFlowData(site: PowerFlowSite)

case class PowerFlowSite(powerGrid: Double, powerLoad: Double, powerPv: Option[Double], selfConsumption: Option[Double], autonomy: Option[Double],
                         timestamp: Option[Long])

class SolarWebConnector extends JsonSupport {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val logger = Logger[SolarWebConnector]
  val httpClient = Http().outgoingConnection(host = "192.168.188.26", port = 80)

  def getInverterRealtimeData(): InverterData = {
    val inverterRealtimeUrlPath = s"""/solar_api/v1/GetInverterRealtimeData.cgi?scope=System"""
    val flowGet: Future[InverterResponse] = sendRequest[InverterResponse](inverterRealtimeUrlPath)
    awaitResult(flowGet, { () => InverterResponse(InverterBody(InverterData(DayEnergy("n/a", Values(0))))) }).body.data
  }

  def getMeterRealtimeData(): MeterData = {
    val meterRealtimeUrlPath = s"""/solar_api/v1/GetMeterRealtimeData.cgi?scope=Device&deviceid=0"""
    val flowGet: Future[MeterResponse] = sendRequest[MeterResponse](meterRealtimeUrlPath)
    awaitResult(flowGet, { () => null }).body.data
  }

  def getPowerFlowRealtimeData(): PowerFlowSite = {
    val powerFlowRealtimeDataUrlPath = s"""/solar_api/v1/GetPowerFlowRealtimeData.fcgi"""
    val flowGet: Future[PowerFlowResponse] = sendRequest[PowerFlowResponse](powerFlowRealtimeDataUrlPath)
    awaitResult(flowGet, { () => PowerFlowResponse(PowerFlowBody(PowerFlowData(PowerFlowSite(0, 0, None, None, None, None)))) }).body.data.site
  }

  private def awaitResult[T](futureToWaitFor: Future[T], factory: () => T): T = {
    var result: T = null.asInstanceOf[T]
    try {
      val start = System.currentTimeMillis()
      result = Await.result(futureToWaitFor, 30 seconds)
      val end = System.currentTimeMillis()
      logger.debug(s"Result in ${end - start} millis: $result")
    } catch {
      case e: TimeoutException => logger.info("timeout waiting for result"); result = factory.apply()
      case e: Exception => logger.info("Exception: " + e.getMessage); logger.info("Exception: ", e); result = factory.apply()
    }
    result
  }

  private def sendRequest[T](inverterRealtimeURLPath: String)(implicit m: Unmarshaller[ResponseEntity, T]): Future[T] = {
    val flowGet: Future[T] =
      Source.single(
        HttpRequest(
          method = HttpMethods.GET,
          uri = Uri(inverterRealtimeURLPath))
      )
        .via(httpClient)
        .mapAsync(1)(response => {
          logger.info(response.toString())
          Unmarshal(response.entity).to[T]
        })
        .runWith(Sink.head)
    flowGet
  }
}
