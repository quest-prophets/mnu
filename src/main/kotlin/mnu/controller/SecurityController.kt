package mnu.controller

import mnu.form.NewEquipmentForm
import mnu.model.request.ChangeEquipmentRequest
import mnu.model.request.Request
import mnu.repository.TransportRepository
import mnu.repository.WeaponRepository
import mnu.repository.employee.SecurityEmployeeRepository
import mnu.repository.request.ChangeEquipmentRequestRepository
import mnu.repository.request.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
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

    @PostMapping("/equipment")
    fun requestNewEquipment(
        @ModelAttribute form: NewEquipmentForm, principal: Principal,
        redirect: RedirectAttributes
    ): String {
        val user = userRepository?.findByLogin(principal.name)
        val newRequest = Request()
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

}