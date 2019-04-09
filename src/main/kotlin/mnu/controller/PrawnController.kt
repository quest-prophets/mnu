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

    @Autowired
    val districtIncidentRepository: DistrictIncidentRepository? = null

    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null

    @GetMapping("/main")
    fun prawnMain() : String {
        return "prawns/prawn__main.html"
    }
}