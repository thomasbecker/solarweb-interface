package de.softwareschmied.solarwebinterface

import com.typesafe.scalalogging.Logger
import org.specs2.mutable.Specification

/**
  * Created by Thomas Becker (thomas.becker00@gmail.com) on 13.11.17.
  */
class SolarWebConnectorSpec extends Specification {
  val logger = Logger[SolarWebConnectorSpec]
  def solarWebConnector = new SolarWebConnector

  "SolarWebConnector should" >> {
    "return some real time inverter data when getInverterRealtimeData is called" >> {
      val energy = solarWebConnector.getInverterRealtimeData().dayEnergy
      energy.unit must beEqualTo("Wh")
      energy.values.value must beGreaterThan(0)
    }

    "return some real time meter data when " >> {
      val meterData = solarWebConnector.getMeterRealtimeData()
      logger.info(s"$meterData")
      meterData.powerRealSum must_!= 0.0
      meterData.powerRealPhase1 must_!= 0.0
      meterData.powerRealPhase2 must_!= 0.0
      meterData.powerRealPhase3 must_!= 0.0
    }

    "return some power flow real time data when" >> {
      val powerFlowSite = solarWebConnector.getPowerFlowRealtimeData()
      logger.info(s"$powerFlowSite")
      powerFlowSite.powerPv must_!= 0.0
      powerFlowSite.powerGrid must_!= 0.0
      powerFlowSite.powerLoad must_!= 0.0
      powerFlowSite.selfConsumption must_!= 0.0
      powerFlowSite.autonomy must_!= 0.0
    }

  }
}
