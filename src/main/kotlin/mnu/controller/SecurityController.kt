package mnu.controller

import mnu.form.NewEquipmentForm
import mnu.form.NewSearchForm
import mnu.model.enums.RequestStatus
import mnu.model.enums.WeaponType
import mnu.model.request.ChangeEquipmentRequest
import mnu.model.request.NewWeaponRequest
import mnu.model.request.Request
import mnu.repository.DistrictIncidentRepository
import mnu.repository.TransportRepository
import mnu.repository.WeaponRepository
import mnu.repository.employee.SecurityEmployeeRepository
import mnu.repository.request.ChangeEquipmentRequestRepository
import mnu.repository.request.NewWeaponRequestRepository
import mnu.repository.request.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal

@Controller
@RequestMapping("/sec")
class SecurityController (
    val securityEmployeeRepository: SecurityEmployeeRepository,
    val weaponRepository: WeaponRepository,
    val newWeaponRequestRepository: NewWeaponRequestRepository,
    val transportRepository: TransportRepository,
    val requestRepository: RequestRepository,
    val changeEquipmentRequestRepository: ChangeEquipmentRequestRepository,
    val districtIncidentRepository: DistrictIncidentRepository
) : ApplicationController() {

    @GetMapping("/main")
    fun securityMenu() = "security/sec__main.html"

    @PostMapping("/equipment")
    fun requestNewEquipment(
        @ModelAttribute form: NewEquipmentForm, principal: Principal,
        redirect: RedirectAttributes
    ): String {
        val user = userRepository?.findByLogin(principal.name)
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val currentSecurity = securityEmployeeRepository.findById(user?.id!!).get()
        val allChangeRequests = changeEquipmentRequestRepository.findAllByEmployee(currentSecurity)

        allChangeRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "You cannot have more than 1 pending equipment change request.")
                return "redirect:equipment"
            }
        }


        val requestedWeapon = when (form.weaponId) {
            null -> null
            else -> weaponRepository.findById(form.weaponId)
        }
        val requestedTransport = when (form.transportId) {
            null -> null
            else -> transportRepository.findById(form.transportId)
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
            existingWeapon.requiredAccessLvl > currentSecurity.employee!!.level!! -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Requested weapon's access level is higher than yours.")
                return "redirect:equipment"
            }
            existingTransport.requiredAccessLvl > currentSecurity.employee!!.level!! -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Requested transport's access level is higher than yours.")
                return "redirect:equipment"
            }
        }

        requestRepository.save(newRequest)
        changeEquipmentRequestRepository.save(
            ChangeEquipmentRequest(currentSecurity, existingWeapon, existingTransport)
            .apply { this.request = newRequest }
        )

        redirect.addFlashAttribute("status", "Request sent. Wait for supervisor's decision.")
        return "redirect:main"
    }

    @PostMapping("/incident/{id}")
    fun acceptIncidentParticipation(@PathVariable id: Long, redirect: RedirectAttributes, principal: Principal) : String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val curEmployeeLevel = employeeRepository?.findByUserId(curUser.id!!)!!.level!!
        val possibleIncident = districtIncidentRepository.findById(id)
        if (!possibleIncident.isPresent)
            return "Incident with such id does not exist."
        val incident = possibleIncident.get()
        incident.assistants = ArrayList()
        val allSuitableIncidents =
            districtIncidentRepository.findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual(
                0, curEmployeeLevel, curEmployeeLevel)
        when {
            incident.availablePlaces == 0L -> {
                return "The amount of security is already sufficient for this incident."
            }
            !allSuitableIncidents?.contains(incident)!! -> {
                return "You are not suitable for this incident."
            }
        }
        incident.apply {
            this.assistants?.add(securityEmployeeRepository.findById(curUser.id!!).get())
            this.availablePlaces = this.availablePlaces - 1
            if(this.availablePlaces == 0L)
                this.dangerLevel = 0
        }

        districtIncidentRepository.save(incident)
        if (incident.availablePlaces == 0L)
            return "You were appointed to the incident and have successfully resolved it."
        return "You were appointed to the incident."
    }

    fun searchReportAccessError(incidentId: Long, principal: Principal): String? {
        val user = userRepository?.findByLogin(principal.name)
        val possibleSecurity = securityEmployeeRepository.findById(user?.id!!)
        if (!possibleSecurity.isPresent)
            return "You are not a security employee."
        val currentSecurity = possibleSecurity.get()
        val incident = districtIncidentRepository.findById(incidentId)
        if (!incident.isPresent)
            return "Incident with such id does not exist."
        if (!incident.get().assistants!!.contains(currentSecurity))
            return "You are not allowed to write a report on incident you did not participate in."

        return null
    }

    @PostMapping("/report")
    fun addSearchReport(@ModelAttribute form: NewSearchForm, principal: Principal, redirect: RedirectAttributes): String {
        val error = searchReportAccessError(form.incidentId.toLong(), principal)
        if (error == null) {
            val incident = districtIncidentRepository.findById(form.incidentId.toLong())
            when (form.isNew.toInt()) {
                0 -> {
                    districtIncidentRepository.save(incident.get().apply {
                        this.description += "\n\n${form.result}"
                        this.dangerLevel = 0
                        this.levelFrom = 0
                        this.levelTo = 0
                    })
                    redirect.addFlashAttribute("status", "Report submitted.")
                    return "redirect:main"
                }

                1 -> {
                    val possibleWeapon = weaponRepository.findById(form.weaponId.toLong())
                    return if (!possibleWeapon.isPresent) {
                        redirect.addFlashAttribute("form", form)
                        redirect.addFlashAttribute("error", "Such weapon does not exist.")
                        "redirect:report"
                    }
                    else {
                        val weapon = possibleWeapon.get()
                        weapon.quantity += form.weaponQuantity1.toLong()
                        weaponRepository.save(weapon)
                        districtIncidentRepository.save(incident.get().apply {
                            this.description += "\n\n${form.result}"
                            this.dangerLevel = 0
                            this.levelFrom = 0
                            this.levelTo = 0
                        })
                        redirect.addFlashAttribute("status", "Report submitted and weapon added to the arsenal.")
                        "redirect:main"

                    }
                }

                2 -> {
                    val user = userRepository?.findByLogin(principal.name)
                    val newRequest = Request().apply { this.status = RequestStatus.PENDING }
                    val weaponType = when (form.weaponType) {
                        "melee" -> WeaponType.MELEE
                        "pistol" -> WeaponType.PISTOL
                        "submachine_gun" -> WeaponType.SUBMACHINE_GUN
                        "assault_rifle" -> WeaponType.ASSAULT_RIFLE
                        "light_machine_gun" -> WeaponType.LIGHT_MACHINE_GUN
                        "sniper_rifle" -> WeaponType.SNIPER_RIFLE
                        "alien" -> WeaponType.ALIEN
                        else -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Such weapon type does not exist.")
                            return "redirect:report"
                        }
                    }

                    when {
                        form.weaponLevel.toInt() < 1 || form.weaponLevel.toInt() > 10 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter weapon access level between 1-10.")
                            return "redirect:report"
                        }
                        form.weaponQuantity2.toLong() < 1 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter a valid quantity of this weapon.")
                            return "redirect:report"
                        }
                        form.weaponPrice.toDouble() < 1 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter a valid price for this weapon.")
                            return "redirect:report"
                        }
                    }

                    val newWeaponRequest = NewWeaponRequest(
                        form.weaponName, weaponType, form.weaponDescription,
                        form.weaponQuantity2.toLong(), form.weaponLevel.toInt(), form.weaponPrice.toDouble(), user
                    )

                    newWeaponRequestRepository.save(newWeaponRequest.apply { this.request = newRequest })
                    districtIncidentRepository.save(incident.get().apply {
                        this.description += "\n\n${form.result}"
                        this.dangerLevel = 0
                        this.levelFrom = 0
                        this.levelTo = 0
                    })
                    redirect.addFlashAttribute("status", "Report submitted. Await for supervisor's decision.")
                    return "redirect:main"

                }
            }

        } else {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", error)
            return "redirect:report"
        }
        return "redirect:report"
    }

    @PostMapping("/withdrawChange")
    fun withdrawApplication (principal: Principal, redirect: RedirectAttributes) : String {
        val user = userRepository?.findByLogin(principal.name)
        val currentSecurity = securityEmployeeRepository.findById(user?.id!!).get()
        val allChangeRequests = changeEquipmentRequestRepository.findAllByEmployee(currentSecurity)

        allChangeRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                it.request!!.status = RequestStatus.REJECTED
                changeEquipmentRequestRepository.save(it)
                redirect.addFlashAttribute("status", "Request withdrawn.")
                return "redirect:equipment"
            }
        }

        redirect.addFlashAttribute("error", "You have no active equipment change requests.")
        return "redirect:equipment"
    }

}