package mnu.controller

import mnu.form.*
import mnu.model.*
import mnu.model.employee.*
import mnu.model.enums.*
import mnu.repository.*
import mnu.repository.employee.*
import mnu.repository.request.NewWeaponRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import java.time.LocalDateTime

@Controller
@RequestMapping("/admin")
class AdministratorController : ApplicationController() {

    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null
    @Autowired
    val districtIncidentRepository: DistrictIncidentRepository? = null

    @Autowired
    val weaponRepository: WeaponRepository? = null
    @Autowired
    val newWeaponRequestRepository: NewWeaponRequestRepository? = null

    @Autowired
    val articleRepository: ArticleRepository? = null
    @Autowired
    val experimentRepository: ExperimentRepository? = null

    @Autowired
    val managerEmployeeRepository: ManagerEmployeeRepository? = null
    @Autowired
    val securityEmployeeRepository: SecurityEmployeeRepository? = null
    @Autowired
    val scientistEmployeeRepository: ScientistEmployeeRepository? = null
    @Autowired
    val administratorEmployeeRepository: AdministratorEmployeeRepository? = null


    @GetMapping("/employee")
    fun adminEmployees(model: Model, @RequestParam(required = false) q: String?): String {
        model.addAttribute("form_add", EmployeeRegistrationForm())
        model.addAttribute("form_edit", EmployeeEditForm())
        model.addAttribute("form_reward", CashRewardForm())
        if (q != null)
            model.addAttribute("employees", employeeRepository?.findAllByNameIgnoreCaseContainingOrderByIdAsc(q))
        else
            model.addAttribute("employees", employeeRepository?.findAllByOrderByIdAsc())
        return "administrators/admin__employees.html"
    }

    @GetMapping("/prawns")
    fun prawnRegister(model: Model): String {
        model.addAttribute("form", PrawnRegistrationForm())
        return "administrators/admin__prawn-registration.html"
    }

    @GetMapping("/main")
    fun adminMenu() = "administrators/admin__menu.html"

    @GetMapping("/experiments")
    fun adminExperiments(model: Model): String {
        model.addAttribute(
            "experiments",
            experimentRepository?.findAllByStatusAndType(ExperimentStatus.PENDING, ExperimentType.MAJOR)
        )
        return "administrators/admin__experiments.html"
    }

    @GetMapping("/articles")
    fun adminArticles(model: Model): String {
        model.addAttribute("articles", articleRepository?.findAllByOrderByCreationDateDesc())
        return "administrators/admin__articles.html"
    }

    @PostMapping("/registerEmployee")
    fun addEmployee(@ModelAttribute form: EmployeeRegistrationForm, redirect: RedirectAttributes): String {
        val existingUser = userRepository?.findByLogin(form.username)
        val regex = """[a-zA-Z0-9_.]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
            "redirect:employee"
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username '${form.username}' is already taken. Please try again.")
                return "redirect:employee"
            } else {
                val role = when (form.type) {
                    "manager" -> Role.MANAGER
                    "scientist" -> Role.SCIENTIST
                    "security" -> Role.SECURITY
                    "administrator" -> Role.ADMIN
                    else -> return "Error"
                }
                val newUser = User(form.username, form.password, role)
                val newEmployeeUser = Employee(
                    form.name, LocalDateTime.now(),
                    form.level.toInt(), form.salary.toLong(), form.position
                ).apply {
                    this.user = newUser
                    this.status = PersonStatus.WORKING
                }

                userRepository?.save(newUser)
                employeeRepository?.save(newEmployeeUser)
                when (role) {
                    Role.MANAGER -> managerEmployeeRepository?.save(ManagerEmployee().apply {
                        this.employee = newEmployeeUser
                    })
                    Role.SCIENTIST -> scientistEmployeeRepository?.save(ScientistEmployee().apply {
                        this.employee = newEmployeeUser
                    })
                    Role.SECURITY -> securityEmployeeRepository?.save(SecurityEmployee().apply {
                        this.employee = newEmployeeUser
                    })
                    Role.ADMIN -> administratorEmployeeRepository?.save(AdministratorEmployee().apply {
                        this.employee = newEmployeeUser
                    })
                    else -> {
                    }
                }

                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("status", "Successfully registered a new employee.")
                "redirect:employee"
            }
        }
    }

    @PostMapping("/editEmployee")
    fun editEmployee(@ModelAttribute form: EmployeeEditForm, redirect: RedirectAttributes): String {
        val existingEmployee = employeeRepository?.findById(form.id_edit.toLong())!!
        if (!existingEmployee.isPresent)
            return "Employee with such id does not exist."
        val totallyExistingEmployee = existingEmployee.get()
        if (form.name_edit == "" || form.level_edit == ""
            || form.position_edit == ""
            || form.salary_edit == ""
            || form.status_edit == ""
        ) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "One of the fields is empty. Please fill all fields.")
            return "redirect:employee"
        }

        val newStatus = when (form.status_edit) {
            "working" -> PersonStatus.WORKING
            "fired" -> PersonStatus.FIRED
            "dead" -> PersonStatus.DEAD
            else -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such status does not exist.")
                return "redirect:employee"
            }
        }
        totallyExistingEmployee.name = form.name_edit
        totallyExistingEmployee.level = form.level_edit.toInt()
        totallyExistingEmployee.position = form.position_edit
        totallyExistingEmployee.salary = form.salary_edit.toLong()
        totallyExistingEmployee.status = newStatus

        employeeRepository?.save(totallyExistingEmployee)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Successfully edited.")
        return "redirect:employee"

    }

    @PostMapping("/giveReward")
    fun awardCash(@ModelAttribute form: CashRewardForm, redirect: RedirectAttributes): String {
        val existingEmployee = employeeRepository?.findById(form.id_cash.toLong())!!
        if (!existingEmployee.isPresent) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Employee with such id does not exist.")
            return "redirect:employee"
        }
        val totallyExistingEmployee = existingEmployee.get()
        if (form.reward == "") {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Please fill the reward field.")
            return "redirect:employee"
        }
        val newReward = CashReward(totallyExistingEmployee, form.reward.toLong())

        cashRewardRepository?.save(newReward)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Reward given.")
        return "redirect:employee"
    }

    data class ScientistRewardForArticle (var employeeId: String = "", var reward: String = "")
    data class RewardResponse (var isError: Boolean = false, var message: String = "")

    @PostMapping("/giveRewardAjax")
    @ResponseBody
    fun awardCashForArticle(@RequestBody data: ScientistRewardForArticle): RewardResponse {
        val existingEmployee = employeeRepository?.findById(data.employeeId.toLong())!!
        if (!existingEmployee.isPresent) {
            return RewardResponse(true, "Employee with such id does not exist.")
        }
        val totallyExistingEmployee = existingEmployee.get()
        if (data.reward == "") {
            return RewardResponse(true, "Please fill the reward field.")
        }
        val newReward = CashReward(totallyExistingEmployee, data.reward.toLong())

        cashRewardRepository?.save(newReward)

        return RewardResponse(false, "Reward given.")
    }

    @PostMapping("/registerPrawn")
    fun addPrawn(@ModelAttribute form: PrawnRegistrationForm, redirect: RedirectAttributes): String {
        val existingUser = userRepository?.findByLogin(form.username)
        val regex = """[a-zA-Z0-9_.]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
            "redirect:prawns"
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username '${form.username}' is already taken. Please try again.")
                "redirect:prawns"
            } else {
                val houseIdList = districtHouseRepository?.getAllIds()!!

                val managerIdList = managerEmployeeRepository?.getAllIds()!!

                val newUser = User(form.username, form.password, Role.PRAWN)
                val newPrawn = Prawn(form.name).apply {
                    this.user = newUser
                    this.districtHouse = districtHouseRepository?.findById(houseIdList.random())?.get()
                    this.manager = managerEmployeeRepository?.findById(managerIdList.random())?.get()
                }

                userRepository?.save(newUser)
                prawnRepository?.save(newPrawn)

                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("status", "Successfully registered a new prawn.")
                "redirect:main"
            }
        }
    }

    fun experimentChoiceError(experimentId: Long): String? {
        val experiment = experimentRepository?.findById(experimentId)!!
        if (!experiment.isPresent)
            return "Experiment with such id does not exist."
        val checkedExperiment = experiment.get()
        if (checkedExperiment.type == ExperimentType.MINOR)
            return "Minor experiment requests are handled by high-level scientists."

        return null
    }

    @PostMapping("/acceptExperiment/{id}")
    fun acceptExperiment(@PathVariable id: Long, redirect: RedirectAttributes): String {
        val error = experimentChoiceError(id)
        return if (error == null) {
            val checkedExperiment = experimentRepository?.findById(id)!!.get()

            if (checkedExperiment.status != ExperimentStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:main/requests"
            }
            else {
                checkedExperiment.statusDate = LocalDateTime.now()
                checkedExperiment.status = ExperimentStatus.APPROVED
                experimentRepository?.save(checkedExperiment)

                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:main/requests"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/requests"
        }
    }


    @PostMapping("/rejectExperiment/{id}")
    fun rejectExperiment(@PathVariable id: Long, redirect: RedirectAttributes): String {
        val error = experimentChoiceError(id)
        return if (error == null) {
            val checkedExperiment = experimentRepository?.findById(id)!!.get()

            if (checkedExperiment.status != ExperimentStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:main/requests"
            }
            else {
                checkedExperiment.statusDate = LocalDateTime.now()
                checkedExperiment.status = ExperimentStatus.REJECTED
                experimentRepository?.save(checkedExperiment)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:main/requests"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/requests"
        }
    }

    @PostMapping("/undoExperimentChoice/{id}")
    @ResponseBody
    fun undoExpChoice(@PathVariable id: Long, redirect: RedirectAttributes): String {
        val error = experimentChoiceError(id)
        return if (error == null) {
            val checkedExperiment = experimentRepository?.findById(id)!!.get()

            checkedExperiment.statusDate = LocalDateTime.now()
            checkedExperiment.status = ExperimentStatus.PENDING
            experimentRepository?.save(checkedExperiment)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:main/requests"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/requests"
        }
    }

    fun newWeaponChoiceError(newWeaponRequestId: Long, principal: Principal): String? {
        val request = newWeaponRequestRepository?.findById(newWeaponRequestId)!!
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptNewWeapon/{id}")
    fun acceptNewWeapon(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:main/weapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentAdmin
                }
                val newWeapon = Weapon(checkedRequest.name, checkedRequest.type,
                    checkedRequest.description, checkedRequest.price, checkedRequest.requiredAccessLvl)
                    .apply { this.quantity = checkedRequest.quantity }
                weaponRepository?.save(newWeapon)

                newWeaponRequestRepository?.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:main/weapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/weapons"
        }
    }


    @PostMapping("/rejectNewWeapon/{id}")
    fun rejectNewWeapon(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:main/weapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentAdmin
                }
                newWeaponRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:main/weapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/weapons"
        }
    }

    @PostMapping("/undoWeaponChoice/{id}")
    fun undoWeapChoice(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            checkedRequest.request!!.apply {
                this.statusDate = LocalDateTime.now()
                this.status = RequestStatus.PENDING
                this.resolver = currentAdmin
            }
            val weapons = weaponRepository?.findAll()!!.asReversed()
            for(i in 0 until weapons.size) {
                if (weapons[i].name == checkedRequest.name)
                    weaponRepository?.delete(weapons[i])
                break
            }

            newWeaponRequestRepository?.save(checkedRequest)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:main/weapons"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/weapons"
        }
    }

    @PostMapping("/appointResolvers")
    fun appointResolversForIncident(@ModelAttribute form: AppointResolversForm, redirect: RedirectAttributes): String {
        val incident = districtIncidentRepository?.findById(form.incidentId.toLong())!!
        when {
            form.incidentId == "" || form.securityNeeded == "" || form.levelFrom == "" || form.levelTo == "" -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "One of the fields is empty. Please fill all fields.")
                return "redirect:district"
            }
            !incident.isPresent -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such incident does not exist.")
                return "redirect:district"
            }
        }

        val newIncident = incident.get().apply {
            this.availablePlaces = form.securityNeeded.toLong()
            this.levelFrom = form.levelFrom.toInt()
            this.levelTo = form.levelTo.toInt()
        }

        districtIncidentRepository?.save(newIncident)
        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute(
            "status",
            "Success. All security employees will be notified of the occurred incident."
        )
        return "redirect:district"
    }

}