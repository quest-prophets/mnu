package mnu.controller

import mnu.model.DistrictIncident
import mnu.repository.DistrictHouseRepository
import mnu.repository.DistrictIncidentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Controller("/prawn")
class PrawnController {

    data class NewIncident(var dangerLevel: Short = 0, var row: Int = 0, var column: Int = 0,
                           var description: String = "", var appearanceTime: LocalDateTime = LocalDateTime.now())


    @Autowired
    val districtIncidentRepository: DistrictIncidentRepository? = null

    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null


}