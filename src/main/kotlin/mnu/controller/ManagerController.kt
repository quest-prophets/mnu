package mnu.controller

import mnu.form.PrawnRegistrationForm
import mnu.model.Prawn
import mnu.model.User
import mnu.model.enums.RequestStatus
import mnu.model.enums.Role
import mnu.repository.DistrictHouseRepository
import mnu.repository.employee.ManagerEmployeeRepository
import mnu.repository.request.NewWeaponRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import java.time.LocalDateTime

@Controller
@RequestMapping("/man")
class ManagerController : ApplicationController() {

    @ModelAttribute("level")
    fun getLevel(principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        return employeeRepository?.findById(curUser.id!!)?.get()!!.level.toString()
    }

    @Autowired
    val managerEmployeeRepository: ManagerEmployeeRepository? = null

    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null

    @Autowired
    val newWeaponRequestRepository: NewWeaponRequestRepository? = null

    @GetMapping("/main")
    fun manMenu() = "managers/manager__main.html"


    @PostMapping("/registerPrawn")
    @ResponseBody
    fun addPrawn(@ModelAttribute form: PrawnRegistrationForm, principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val possibleManager = managerEmployeeRepository?.findById(curUser.id!!)!!
        if (possibleManager.isPresent)
            return "You are not a manager."

        val existingUser = userRepository?.findByLogin(form.username)
        val regex = """[a-zA-Z0-9_.]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            "Only latin letters, numbers, \"_\" and \".\" are supported."
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                "Username '${form.username}' is already taken. Please try again."
            } else {
                val houseIdList = districtHouseRepository?.getAllIds()!!

                val newUser = User(form.username, form.password, Role.PRAWN)
                val newPrawn = Prawn(form.name).apply {
                    this.user = newUser
                    this.districtHouse = districtHouseRepository?.findById(houseIdList.random())?.get()
                    this.manager = possibleManager.get()
                }

                userRepository?.save(newUser)
                prawnRepository?.save(newPrawn)

                "Successfully registered a new prawn."
            }
        }
    }

    fun choiceError(newWeaponRequestId: Long, principal: Principal): String? {
        val request = newWeaponRequestRepository?.findById(newWeaponRequestId)!!
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptNewWeapon/{id}")
    fun acceptNewWeapon(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = choiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:main/requests"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentManager
                }
                newWeaponRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:main/requests"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/requests"
        }
    }


    @PostMapping("/rejectNewWeapon/{id}")
    fun rejectNewWeapon(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = choiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:main/requests"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentManager
                }
                newWeaponRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:main/requests"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/requests"
        }
    }

    @PostMapping("/undoWeaponChoice/{id}")
    fun undoWeapChoice(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = choiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            checkedRequest.request!!.apply {
                this.statusDate = LocalDateTime.now()
                this.status = RequestStatus.PENDING
                this.resolver = currentManager
            }
            newWeaponRequestRepository?.save(checkedRequest)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:main/requests"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/requests"
        }
    }

}