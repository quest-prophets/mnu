package mnu.controller

import mnu.form.NewEquipmentForm
import mnu.form.NewPasswordForm
import mnu.model.employee.SecurityEmployee
import mnu.model.enums.RequestStatus
import mnu.model.request.ChangeEquipmentRequest
import mnu.model.request.Request
import mnu.repository.CashRewardRepository
import mnu.repository.DistrictIncidentRepository
import mnu.repository.TransportRepository
import mnu.repository.WeaponRepository
import mnu.repository.employee.SecurityEmployeeRepository
import mnu.repository.request.ChangeEquipmentRequestRepository
import mnu.repository.request.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal

@Controller
@RequestMapping("/sec")
class SecurityController : ApplicationController() {
    @ModelAttribute("level")
    fun getLevel(principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        return employeeRepository?.findById(curUser.id!!)?.get()!!.level.toString()
    }

    @Autowired
    val securityEmployeeRepository: SecurityEmployeeRepository? = null

    @Autowired
    val weaponRepository: WeaponRepository? = null

    @Autowired
    val transportRepository: TransportRepository? = null

    @Autowired
    val requestRepository: RequestRepository? = null

    @Autowired
    val changeEquipmentRequestRepository: ChangeEquipmentRequestRepository? = null

    @Autowired
    val districtIncidentRepository: DistrictIncidentRepository? = null

    @GetMapping("/main")
    fun securityMenu() = "security/sec__main.html"

    @PostMapping("/equipment")
    fun requestNewEquipment(
        @ModelAttribute form: NewEquipmentForm, principal: Principal,
        redirect: RedirectAttributes
    ): String {
        val user = userRepository?.findByLogin(principal.name)
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val currentSecurity = securityEmployeeRepository?.findById(user?.id!!)?.get()!!

        val requestedWeapon = when (form.weaponId) {
            null -> null
            else -> weaponRepository?.findById(form.weaponId)
        }
        val requestedTransport = when (form.transportId) {
            null -> null
            else -> transportRepository?.findById(form.transportId)
        }

        when {
            !requestedWeapon!!.isPresent -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such weapon does not exist.")
                return "redirect:equipment"
            }
            !requestedTransport!!.isPresent -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such transport does not exist.")
                return "redirect:equipment"
            }
            !requestedTransport.isPresent && !requestedWeapon.isPresent -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such weapon and transport do not exist.")
                return "redirect:equipment"
            }
        }

        val existingWeapon = requestedWeapon?.get()
        val existingTransport = requestedTransport?.get()

        when {
            existingWeapon?.quantity!! == 0L -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another weapon.")
                return "redirect:equipment"
            }
            existingTransport?.quantity!! == 0L -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another transport.")
                return "redirect:equipment"
            }
        }

        requestRepository?.save(newRequest)
        changeEquipmentRequestRepository?.save(
            ChangeEquipmentRequest(currentSecurity, existingWeapon, existingTransport)
            .apply { this.request = newRequest }
        )


        redirect.addFlashAttribute("status", "Request sent. Wait for supervisor's decision.")
        return "redirect:main"
    }

//    @PostMapping("/incident/{id}")
//    fun acceptIncidentParticipation(@PathVariable id: Long, redirect: RedirectAttributes, principal: Principal) : String {
//        val curUser = userRepository?.findByLogin(principal.name)!!
//        val curEmployeeLevel = employeeRepository?.findByUserId(curUser.id!!)!!.level!!
//        val possibleIncident = districtIncidentRepository?.findById(id)!!
//        if (!possibleIncident.isPresent)
//            return "Incident with such id does not exist."
//        val incident = possibleIncident.get()
//        incident.assistants = ArrayList()
//        val allSuitableIncidents =
//            districtIncidentRepository?.findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual(
//                0, curEmployeeLevel, curEmployeeLevel)
//        when {
//            incident.availablePlaces == 0L -> {
//                return "The amount of security is already sufficient for this incident."
//            }
//            !allSuitableIncidents?.contains(incident)!! -> {
//                return "You are not suitable for this incident."
//            }
//        }
//        incident.apply {
//            this.assistants?.add(securityEmployeeRepository?.findById(curUser.id!!)!!.get())
//            this.availablePlaces = this.availablePlaces - 1
//            if(this.availablePlaces == 0L)
//                this.dangerLevel = 0
//        }
//
//        districtIncidentRepository?.save(incident)
//        if (incident.availablePlaces == 0L)
//            return "You were appointed to the incident and have successfully resolved it."
//        return "You were appointed to the incident."
//    }

}