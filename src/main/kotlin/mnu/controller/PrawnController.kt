package mnu.controller

import mnu.form.NewPasswordForm
import mnu.model.enums.RequestStatus
import mnu.model.request.Request
import mnu.model.request.VacancyApplicationRequest
import mnu.repository.DistrictHouseRepository
import mnu.repository.DistrictIncidentRepository
import mnu.repository.VacancyRepository
import mnu.repository.request.RequestRepository
import mnu.repository.request.VacancyApplicationRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal

@Controller
@RequestMapping("/prawn")
class PrawnController : ApplicationController() {

    @Autowired
    val districtIncidentRepository: DistrictIncidentRepository? = null

    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null

    @Autowired
    val requestRepository: RequestRepository? = null

    @Autowired
    val vacancyRepository: VacancyRepository? = null
    @Autowired
    val vacancyApplicationRequestRepository: VacancyApplicationRequestRepository? = null


    @GetMapping("/main")
    fun prawnMain(): String {
        return "prawns/prawn__main.html"
    }

    @GetMapping("/profile")
    fun prawnProfile(model: Model, principal: Principal): String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentPrawn)
        model.addAttribute("form", NewPasswordForm())
        return "prawns/prawn__profile.html"
    }

    @PostMapping("/vacancyApplication/{id}")
    fun applyToVacancy(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val allVacancyRequests = vacancyApplicationRequestRepository?.findAllByPrawn(currentPrawn)

        allVacancyRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "You cannot have more than 1 pending vacancy application request.")
                return "redirect:vacancies"
            }
        }

        val applicationVacancy = vacancyRepository?.findById(id)!!
        if (!vacancyRepository?.findById(id)!!.isPresent) {
            redirect.addFlashAttribute("error", "Such vacancy does not exist.")
            return "redirect:vacancies"
        }

        val existingVacancy = applicationVacancy.get()

        when {
            existingVacancy.vacantPlaces == 0L -> {
                redirect.addFlashAttribute("error", "No vacant places left. Check back later or choose another vacancy.")
                return "redirect:vacancies"
            }
            existingVacancy.requiredKarma > currentPrawn.karma -> {
                redirect.addFlashAttribute("error", "Requested vacancy's required karma is higher than yours.")
                return "redirect:vacancies"
            }
        }

        requestRepository?.save(newRequest)
        vacancyApplicationRequestRepository?.save(
            VacancyApplicationRequest(currentPrawn, existingVacancy)
                .apply { this.request = newRequest }
        )

        redirect.addFlashAttribute("status", "Request sent. Wait for supervisor's decision.")
        return "redirect:main"
    }
}