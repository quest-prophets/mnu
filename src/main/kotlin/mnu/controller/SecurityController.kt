package mnu.controller

import mnu.form.NewEquipmentForm
import mnu.form.NewPasswordForm
import mnu.model.enums.RequestStatus
import mnu.model.request.ChangeEquipmentRequest
import mnu.model.request.Request
import mnu.repository.CashRewardRepository
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
    val cashRewardRepository: CashRewardRepository? = null

    @Autowired
    val weaponRepository: WeaponRepository? = null

    @Autowired
    val transportRepository: TransportRepository? = null

    @Autowired
    val requestRepository: RequestRepository? = null

    @Autowired
    val changeEquipmentRequestRepository: ChangeEquipmentRequestRepository? = null

    @GetMapping("/main")
    fun securityMenu() = "security/sec__main.html"

    @GetMapping("/profile")
    fun secProfile(model: Model, principal: Principal) : String {
        val currentEmployee = employeeRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        val cashRewards = cashRewardRepository?.findAllByEmployee(currentEmployee!!)
        model.addAttribute("user", currentEmployee)
        model.addAttribute("form", NewPasswordForm())
        model.addAttribute("cashRewards", cashRewards)
        return "security/sec__profile.html"
    }

    @PostMapping("/changePass")
    fun changePass(@ModelAttribute form: NewPasswordForm, principal: Principal, redirect: RedirectAttributes) : String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val regex = """[a-zA-Z0-9_.]+""".toRegex()
        val passwordEncoder = BCryptPasswordEncoder()
        when {
            !regex.matches(form.newPass) -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
                return "redirect:profile"
            }
            form.prevPass == "" || form.newPass == "" -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "One of the fields is empty. Please fill all fields.")
                return "redirect:profile"
            }
            !passwordEncoder.matches(form.prevPass, curUser.password) -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Previous password is incorrect. Please try again.")
                return "redirect:profile"
            }
        }
        curUser.password = passwordEncoder.encode(form.newPass)
        userRepository?.save(curUser)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Password changed successfully.")
        return "redirect:profile"
    }

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

}