package mnu.controller

import mnu.model.DistrictIncident
import mnu.repository.DistrictHouseRepository
import mnu.repository.DistrictIncidentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Controller("/incident")
class DistrictIncidentController {


    data class NewIncident (var house: String = "", var dangerLevel: String = "", var description: String = "")
//    data class NewIncident(var dangerLevel: Short = 0, var row: Int = 0, var column: Int = 0,
//                           var description: String = "", var appearanceTime: LocalDateTime = LocalDateTime.now())


    @Autowired
    val districtIncidentRepository: DistrictIncidentRepository? = null

    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null

    @PostMapping("/report")
    @ResponseBody
    fun reportOnANewIncident(@RequestBody incident: NewIncident) : String {
//        val house = districtHouseRepository?.findByShelterColumnAndShelterRow(incident.column, incident.row) ?: return "Such house does not exist"
//        val newIncident = DistrictIncident(incident.dangerLevel, house, incident.description, incident.appearanceTime)
//        districtIncidentRepository?.save(newIncident)
        return "Report added. Thank you for your goodwill!"
    }
}