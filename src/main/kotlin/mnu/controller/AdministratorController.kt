package mnu.controller

import mnu.form.*
import mnu.model.Prawn
import mnu.model.User
import mnu.model.employee.*
import mnu.model.enums.PersonStatus
import mnu.model.enums.Role
import mnu.repository.*
import mnu.repository.employee.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDateTime

@Controller
@RequestMapping("/admin")
class AdministratorController : ApplicationController() {

    @ModelAttribute("level")
    fun getLevel(principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        return employeeRepository?.findById(curUser.id!!)?.get()!!.level.toString()
    }

    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null

    @Autowired
    val cashRewardRepository: CashRewardRepository? = null

    @Autowired
    val managerEmployeeRepository: ManagerEmployeeRepository? = null
    @Autowired
    val securityEmployeeRepository: SecurityEmployeeRepository? = null
    @Autowired
    val scientistEmployeeRepository: ScientistEmployeeRepository? = null
    @Autowired
    val administratorEmployeeRepository: AdministratorEmployeeRepository? = null


    @GetMapping("/employee")
    fun adminEmployees(model: Model, @RequestParam (required = false) q: String?): String {
        model.addAttribute("form", EmployeeRegistrationForm())
        if (q != null)
            model.addAttribute("employees", employeeRepository?.findAllByNameIgnoreCaseContaining(q))
        else
            model.addAttribute("employees", employeeRepository?.findAll())
        return "administrators/admin__employees.html"
    }

    @GetMapping("/main")
    fun adminMenu() = "administrators/admin__menu.html"

    @GetMapping("/experiments")
    fun adminExperiments() = "administrators/admin__experiments.html"

    @GetMapping("/articles")
    fun adminArticles() = "administrators/admin__articles.html"

    @PostMapping("/registerEmployee")
    @ResponseBody
    fun addEmployee(@ModelAttribute form: EmployeeRegistrationForm): String {
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
                val role = when (form.type) {
                    "manager" -> Role.MANAGER
                    "scientist" -> Role.SCIENTIST
                    "security" -> Role.SECURITY
                    "administrator" -> Role.ADMIN
                    else -> return "Error"
                }
                val newUser = User(form.username, form.password, role)
                val newEmployeeUser = Employee(form.name, LocalDateTime.now(),
                    form.level.toInt(), form.salary.toLong(), form.position).apply { this.user = newUser
                    this.status = PersonStatus.WORKING
                }

                userRepository?.save(newUser)
                employeeRepository?.save(newEmployeeUser)
                when (role) {
                    Role.MANAGER -> managerEmployeeRepository?.save(ManagerEmployee().apply { this.employee = newEmployeeUser })
                    Role.SCIENTIST -> scientistEmployeeRepository?.save(ScientistEmployee().apply { this.employee = newEmployeeUser })
                    Role.SECURITY -> securityEmployeeRepository?.save(SecurityEmployee().apply { this.employee = newEmployeeUser })
                    Role.ADMIN -> administratorEmployeeRepository?.save(AdministratorEmployee().apply { this.employee = newEmployeeUser })
                    else -> {}
                }

                "Successfully registered a new employee."
            }
        }
    }

//    @PostMapping("/editEmployee")
//    @ResponseBody
//    fun editEmployee()

    @PostMapping("/registerPrawn")
    @ResponseBody
    fun addPrawn(@ModelAttribute form: PrawnRegistrationForm): String {
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

                val managerIdList = managerEmployeeRepository?.getAllIds()!!

                val newUser = User(form.username, form.password, Role.PRAWN)
                val newPrawn = Prawn(form.name).apply { this.user = newUser
                    this.districtHouse = districtHouseRepository?.findById(houseIdList.random())?.get()
                    this.manager = managerEmployeeRepository?.findById(managerIdList.random())?.get()
                }

                userRepository?.save(newUser)
                prawnRepository?.save(newPrawn)

                "Successfully registered a new prawn."
            }
        }
    }
}