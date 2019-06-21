package mnu

import mnu.model.DistrictHouse
import mnu.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ScheduledTasks {
  /*  @Autowired
    val prawnRepository: PrawnRepository? = null
    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null
    @Autowired
    val districtIncidentRepository: DistrictIncidentRepository? = null

    //@Scheduled(cron = "0 0 0 ? * MON")
    @Scheduled(fixedDelay = 120000)
    fun paydayTime() {
        val allPrawns = prawnRepository?.findAll()
        val allHouses = districtHouseRepository?.findAll()
        val allIncidentsForPastWeek = districtIncidentRepository?.
                        findAllByAppearanceTimeAfterAndAppearanceTimeBefore(LocalDateTime.now().minusDays(7), LocalDateTime.now())
        val allHousesInIncidents = ArrayList<DistrictHouse>()
        allPrawns?.forEach {
            if (it.job != null) {
                it.balance += it.job!!.salary - 100.0 //taxes for everybody
                it.karma += 75
            } else it.balance -= 100.0
            prawnRepository?.save(it)
        }

        allIncidentsForPastWeek?.forEach {
            allHousesInIncidents.add(it.house!!)
        }

        allHouses?.forEach {
            if (!allHousesInIncidents.contains(it)) {
                it.inhabitants?.forEach { prawn ->
                    prawn.karma += 50
                    prawnRepository?.save(prawn)
                }
            } else {
                it.inhabitants?.forEach { prawn ->
                    prawn.karma -= 25
                    prawnRepository?.save(prawn)
                }
            }
        }
    }
   */
}