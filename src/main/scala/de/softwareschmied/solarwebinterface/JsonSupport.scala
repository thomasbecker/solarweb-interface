package de.softwareschmied.solarwebinterface

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

/**
  * Created by Thomas Becker (thomas.becker00@gmail.com) on 15.11.17.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol with NullOptions {
  implicit val valuesFormat = jsonFormat(Values, "1")
  implicit val dayEnergyFormat = jsonFormat(DayEnergy, "Unit", "Values")
  implicit val dataFormat = jsonFormat(InverterData, "DAY_ENERGY")
  implicit val bodyFormat = jsonFormat(InverterBody, "Data")
  implicit val inverterResponseFormat = jsonFormat(InverterResponse, "Body")
  implicit val meterDataFormat = jsonFormat(MeterData, "PowerReal_P_Sum", "PowerReal_P_Phase_1", "PowerReal_P_Phase_2", "PowerReal_P_Phase_3", "timestamp")
  implicit val meterBodyFormat = jsonFormat(MeterBody, "Data")
  implicit val meterResponseFormat = jsonFormat(MeterResponse, "Body")
  implicit val powerFlowSiteFormat = jsonFormat(PowerFlowSite, "P_Grid", "P_Load", "P_PV", "rel_SelfConsumption", "rel_Autonomy", "timestamp")
  implicit val powerFlowDataFormat = jsonFormat(PowerFlowData, "Site")
  implicit val powerFlowBodyFormat = jsonFormat(PowerFlowBody, "Data")
  implicit val powerFlowResponseFormat = jsonFormat(PowerFlowResponse, "Body")
}
