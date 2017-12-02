package de.softwareschmied.solarwebinterface

import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
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

case class MeterData(powerRealSum: BigDecimal, powerRealPhase1: BigDecimal, powerRealPhase2: BigDecimal, powerRealPhase3: BigDecimal, timestamp: Long = Instant.now.getEpochSecond)

case class PowerFlowResponse(body: PowerFlowBody)

case class PowerFlowBody(data: PowerFlowData)

case class PowerFlowData(site: PowerFlowSite)

case class PowerFlowSite(powerGrid: BigDecimal, powerLoad: BigDecimal, powerPv: Option[BigDecimal], selfConsumption: Option[BigDecimal],
                         autonomy: Option[BigDecimal], var timestamp: Option[Long])

class SolarWebConnector extends JsonSupport {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val httpClient = Http().outgoingConnection(host = "192.168.178.20", port = 80)

  def getInverterRealtimeData(): InverterData = {
    val inverterRealtimeUrlPath = s"""/solar_api/v1/GetInverterRealtimeData.cgi?scope=System"""
    val flowGet: Future[InverterResponse] = sendRequest[InverterResponse](inverterRealtimeUrlPath)
    val start = System.currentTimeMillis()
    val result = Await.result(flowGet, 30 seconds)
    val end = System.currentTimeMillis()
    println(s"Result in ${end - start} millis: $result")
    result.body.data
  }

  def getMeterRealtimeData(): MeterData = {
    val meterRealtimeUrlPath = s"""/solar_api/v1/GetMeterRealtimeData.cgi?scope=Device&deviceid=0"""
    val flowGet: Future[MeterResponse] = sendRequest[MeterResponse](meterRealtimeUrlPath)
    val start = System.currentTimeMillis()
    val result = Await.result(flowGet, 30 seconds)
    val end = System.currentTimeMillis()
    println(s"Result in ${end - start} millis: $result")
    result.body.data
  }

  def getPowerFlowRealtimeData(): PowerFlowSite = {
    val meterRealtimeUrlPath = s"""/solar_api/v1/GetPowerFlowRealtimeData.fcgi"""
    val flowGet: Future[PowerFlowResponse] = sendRequest[PowerFlowResponse](meterRealtimeUrlPath)
    val start = System.currentTimeMillis()
    val result = Await.result(flowGet, 30 seconds)
    val end = System.currentTimeMillis()
    println(s"Result in ${end - start} millis: $result")
    val site = result.body.data.site
    site.timestamp = Some(Instant.now.getEpochSecond)
    site
  }

  private def sendRequest[T](inverterRealtimeURLPath: String)(implicit m: Unmarshaller[ResponseEntity, T]): Future[T] = {
    val flowGet: Future[T] =
      Source.single(
        HttpRequest(
          method = HttpMethods.GET,
          uri = Uri(inverterRealtimeURLPath))
      )
        .via(httpClient)
        .mapAsync(1)(response => Unmarshal(response.entity).to[T])
        .runWith(Sink.head)
    flowGet
  }


}
