package mnu.controller

import mnu.form.NewPasswordForm
import mnu.form.PrawnRegistrationForm
import mnu.model.Prawn
import mnu.model.User
import mnu.model.enums.Role
import mnu.repository.CashRewardRepository
import mnu.repository.DistrictHouseRepository
import mnu.repository.employee.ManagerEmployeeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal

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
    val cashRewardRepository: CashRewardRepository? = null

    @GetMapping("/main")
    fun manMenu() = "managers/manager__main.html"

    @GetMapping("/profile")
    fun sciProfile(model: Model, principal: Principal) : String {
        val currentEmployee = employeeRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        val cashRewards = cashRewardRepository?.findAllByEmployee(currentEmployee!!)
        model.addAttribute("user", currentEmployee)
        model.addAttribute("cashRewards", cashRewards)
        return "managers/manager__profile.html"
    }

    @PostMapping("/profile/changePass")
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
            passwordEncoder.encode(form.prevPass) != curUser.password -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Previous password is incorrect. Please try again.")
                return "redirect:profile"
            }
        }
        curUser.password = passwordEncoder.encode(form.newPass)
        userRepository?.save(curUser)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Password changed successfully.")
        return "redirect:main"
    }

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
}