package models.DAO.conversion

import models.DAO._
import models.DAO.UndefElement
import models.DAO.BProcessDTO


/**
 * Database Config
 */

object DatabaseCred {
  import scala.slick.driver.PostgresDriver.simple._
  val database = Database.forURL("jdbc:postgresql://localhost/minority", driver = "org.postgresql.Driver", user = "postgres", password = "12344321")

}

object BPInitiator {
  import main.scala.bprocesses._
  import main.scala.resources.scopes._
  // caster
  def BPRun(id: Int) {

    val process_dto = BPDAO.get(id).get     // find process
    val target = ProcElemDAO.findByBPId(id) // find elements of process

    val spaces =  1                         //****** find spaces
    val sp_elem = 1                         //****** find space elems

// instanciate
 val process = instance(process_dto, target)

// invoke
    //InvokeTracer.run_proc(process)
// save
    //save(process, process_dto)


  
}


  def BPRunFrom(id: Int) {
    // find
    // find elems
    // find spaces
    // find space elems

    // find station
    // find logger

  }
  def instance(bpdto: BProcessDTO, els: List[UndefElement]):BProcess = {

    val process = new BProcess(new Managment)  // !!!!!!!DCO!!!!!!!!!!!

   val arrays = els.map(c => c.cast(process)).flatten.toArray
   println(arrays)
   process.push {
    arrays
   }   
    /**
     * push space
     * add to space
     */
  process
  }

  def dirt_instance() = {
// loggerdb -> logger(bp)
/*
  val process1 = new BProcess(new Managment)
  process1.variety = process.variety
  println(process.logger.logs)
  println("process1.variety" + process1.variety.length)

  println("ssss"+process1.findObjectByOrder(1))
  println(process1.findObjectByOrder(3).get)
  println(process1.variety.map(elem => elem.order).last)
  println(process1.variety.find(elem => elem.order == 3))

 // !!!!!!!DCO!!!!!!!!!!!
  // !!!!!!!DCO!!!!!!!!!!!
   // !!!!!!!DCO!!!!!!!!!!!
   // !!!!!!!DCO!!!!!!!!!!!
  println(dblogger.get.map(log => println(log.order)))
  val logger_results = dblogger.get.map(log => BPLoggerResult(process1.findObjectByOrder(log.order).get, log.order, None, //log.space
     process1.station, log.invoked, log.expanded, log.container, new java.util.Date(log.date.getTime()))
) 
  println(logger_results)
  
 // !!!!!!!DCO!!!!!!!!!!! // !!!!!!!DCO!!!!!!!!!!! // !!!!!!!DCO!!!!!!!!!!!
  // stationdb -> station(bp)
  val dbstation = BPStationDTO.from_origin_station(process.station, process_dto)
  process1.station.update_variables(
                                dbstation.state,
                                dbstation.step,
                                dbstation.space,
                                dbstation.container_step.toArray,
                                dbstation.expand_step.toArray,
                                dbstation.started,
                                dbstation.finished,
                                dbstation.inspace,
                                dbstation.incontainer,
                                dbstation.inexpands,
                                dbstation.paused
                                )

    println("is work?", process1.station.represent)
    // invoke runfrom
    // save
*/
  }

  def save(process: BProcess, process_dto: BProcessDTO):Boolean = {
    // station -> stationdb
    val dbstation = BPStationDAO.from_origin_station(process.station, process_dto)  // !!!!!!!DCO!!!!!!!!!!!
    val station_id = BPStationDAO.pull_object(dbstation)

    // logger -> loggerdb
    val dblogger = BPLoggerDAO.from_origin_lgr(process.logger, process_dto, station_id)

    dblogger.foreach(log => BPLoggerDAO.pull_object(log))

    true
  }
}