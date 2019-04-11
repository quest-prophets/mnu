package mnu.controller

import mnu.form.*
import mnu.model.*
import mnu.model.employee.*
import mnu.model.enums.*
import mnu.model.request.*
import mnu.repository.*
import mnu.repository.employee.*
import mnu.repository.request.*
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
    val requestRepository: RequestRepository? = null

    @Autowired
    val weaponRepository: WeaponRepository? = null
    @Autowired
    val newWeaponRequestRepository: NewWeaponRequestRepository? = null

    @Autowired
    val transportRepository: TransportRepository? = null
    @Autowired
    val newTransportRequestRepository: NewTransportRequestRepository? = null

    @Autowired
    val vacancyRepository: VacancyRepository? = null
    @Autowired
    val newVacancyRequestRepository: NewVacancyRequestRepository? = null
    @Autowired
    val vacancyApplicationRequestRepository: VacancyApplicationRequestRepository? = null

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
    fun adminMenu(model: Model) : String {
        val pendingRequests = requestRepository?.findAllByStatus(RequestStatus.PENDING)

        val newWeaponRequests = newWeaponRequestRepository?.findAll()
        val nwPendingRequests = ArrayList<NewWeaponRequest>()
        for (i in 0 until pendingRequests!!.size) {
            for (j in 0 until newWeaponRequests!!.size) {
                if (newWeaponRequests[j].request == pendingRequests[i])
                    nwPendingRequests.add(newWeaponRequests[j])
            }
        }

        val newTransportRequests = newTransportRequestRepository?.findAll()
        val ntPendingRequests = ArrayList<NewTransportRequest>()
        for (i in 0 until pendingRequests.size) {
            for (j in 0 until newTransportRequests!!.size) {
                if (newTransportRequests[j].request == pendingRequests[i])
                    ntPendingRequests.add(newTransportRequests[j])
            }
        }

        val newVacancyRequests = newVacancyRequestRepository?.findAll()
        val nvPendingRequests = ArrayList<NewVacancyRequest>()
        for (i in 0 until pendingRequests.size) {
            for (j in 0 until newVacancyRequests!!.size) {
                if (newVacancyRequests[j].request == pendingRequests[i])
                    nvPendingRequests.add(newVacancyRequests[j])
            }
        }

        val vacApplicationRequests = vacancyApplicationRequestRepository?.findAll()
        val vaPendingRequests = ArrayList<VacancyApplicationRequest>()
        for (i in 0 until pendingRequests.size) {
            for (j in 0 until vacApplicationRequests!!.size) {
                if (vacApplicationRequests[j].request == pendingRequests[i])
                    vaPendingRequests.add(vacApplicationRequests[j])
            }
        }

        model.addAttribute("new_prod_count", nwPendingRequests.size + ntPendingRequests.size)
        model.addAttribute("new_vac_count", nvPendingRequests.size)
        model.addAttribute("vac_appl_count", vaPendingRequests.size)
        model.addAttribute("experiment_count",
            experimentRepository?.countAllByStatusAndType(ExperimentStatus.PENDING, ExperimentType.MAJOR))
        return "administrators/admin__menu.html"
    }

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
        model.addAttribute("experiment_count",
            experimentRepository?.countAllByStatusAndType(ExperimentStatus.PENDING, ExperimentType.MAJOR))
        return "administrators/admin__articles.html"
    }

    @GetMapping("/newWeapons")
    fun newWeapons(principal: Principal, model: Model): String {
        val weaponRequests = newWeaponRequestRepository?.findAll()
        val requestsForAdmin = ArrayList<NewWeaponRequest>()
        weaponRequests!!.forEach {
            if (it.request!!.status == RequestStatus.PENDING)
                requestsForAdmin.add(it)
        }

        val pendingRequests = requestRepository?.findAllByStatus(RequestStatus.PENDING)
        val newTransportRequests = newTransportRequestRepository?.findAll()
        val ntPendingRequests = ArrayList<NewTransportRequest>()
        for (i in 0 until pendingRequests!!.size) {
            for (j in 0 until newTransportRequests!!.size) {
                if (newTransportRequests[j].request == pendingRequests[i])
                    ntPendingRequests.add(newTransportRequests[j])
            }
        }
        model.addAttribute("requests", requestsForAdmin)
        model.addAttribute("new_tran_count", ntPendingRequests.size)
        return "administrators/admin__new-weapons.html"
    }

    @GetMapping("/newTransport")
    fun newTransport(principal: Principal, model: Model): String {
        val transportRequests = newTransportRequestRepository?.findAll()
        val requestsForAdmin = ArrayList<NewTransportRequest>()
        transportRequests!!.forEach {
            if (it.request!!.status == RequestStatus.PENDING)
                requestsForAdmin.add(it)
        }

        val pendingRequests = requestRepository?.findAllByStatus(RequestStatus.PENDING)
        val newWeaponRequests = newWeaponRequestRepository?.findAll()
        val nwPendingRequests = ArrayList<NewWeaponRequest>()
        for (i in 0 until pendingRequests!!.size) {
            for (j in 0 until newWeaponRequests!!.size) {
                if (newWeaponRequests[j].request == pendingRequests[i])
                    nwPendingRequests.add(newWeaponRequests[j])
            }
        }
        model.addAttribute("requests", requestsForAdmin)
        model.addAttribute("new_weap_count", nwPendingRequests.size)
        return "administrators/admin__new-transport.html"
    }

    @GetMapping("/vacancies")
    fun allVacancies(model: Model): String {
        val pendingRequests = requestRepository?.findAllByStatus(RequestStatus.PENDING)
        val newVacancyRequests = newVacancyRequestRepository?.findAll()
        val nvPendingRequests = ArrayList<NewVacancyRequest>()
        for (i in 0 until pendingRequests!!.size) {
            for (j in 0 until newVacancyRequests!!.size) {
                if (newVacancyRequests[j].request == pendingRequests[i])
                    nvPendingRequests.add(newVacancyRequests[j])
            }
        }

        model.addAttribute("vacancies", vacancyRepository?.findAll())
        model.addAttribute("new_vac_count", nvPendingRequests.size)
        return "administrators/admin__vacancies.html"
    }

    @GetMapping("/vacancies/requests")
    fun vacancyRequests(model: Model): String {
        val vacancyRequests = newVacancyRequestRepository?.findAll()
        val requestsForAdmin = ArrayList<NewVacancyRequest>()
        vacancyRequests!!.forEach {
            if (it.request!!.status == RequestStatus.PENDING)
                requestsForAdmin.add(it)
        }
        model.addAttribute("requests", requestsForAdmin)
        return "administrators/admin__vacancy-requests.html"
    }

    @GetMapping("/vacancies/new")
    fun newVacancy(model: Model): String {
        val pendingRequests = requestRepository?.findAllByStatus(RequestStatus.PENDING)
        val newVacancyRequests = newVacancyRequestRepository?.findAll()
        val nvPendingRequests = ArrayList<NewVacancyRequest>()
        for (i in 0 until pendingRequests!!.size) {
            for (j in 0 until newVacancyRequests!!.size) {
                if (newVacancyRequests[j].request == pendingRequests[i])
                    nvPendingRequests.add(newVacancyRequests[j])
            }
        }

        model.addAttribute("form", NewVacancyForm())
        model.addAttribute("new_vac_count", nvPendingRequests.size)
        return "administrators/admin__new-vacancy.html"
    }

    @GetMapping("/jobApplications")
    fun manJobApplications(principal: Principal, model: Model) : String {
        val vacancyApplicationRequests = vacancyApplicationRequestRepository?.findAll()
        val validVacAppRequests = ArrayList<VacancyApplicationRequest>()
        vacancyApplicationRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING)
                validVacAppRequests.add(it)
        }
        model.addAttribute("requests", validVacAppRequests)
        return "administrators/admin__job-applications.html"
    }


    @PostMapping("/registerEmployee")
    fun addEmployee(@ModelAttribute form: EmployeeRegistrationForm, redirect: RedirectAttributes): String {
        val existingUser = userRepository?.findByLogin(form.username)
        val regex = """[a-zA-Z0-9_.]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
            "redirect:/admin/employee"
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username '${form.username}' is already taken. Please try again.")
                return "redirect:/admin/employee"
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
                "redirect:/admin/employee"
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
            return "redirect:/admin/employee"
        }

        val newStatus = when (form.status_edit) {
            "working" -> PersonStatus.WORKING
            "fired" -> PersonStatus.FIRED
            "dead" -> PersonStatus.DEAD
            else -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such status does not exist.")
                return "redirect:/admin/employee"
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
        return "redirect:/admin/employee"

    }

    @PostMapping("/giveReward")
    fun awardCash(@ModelAttribute form: CashRewardForm, redirect: RedirectAttributes): String {
        val existingEmployee = employeeRepository?.findById(form.id_cash.toLong())!!
        if (!existingEmployee.isPresent) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Employee with such id does not exist.")
            return "redirect:/admin/employee"
        }
        val totallyExistingEmployee = existingEmployee.get()
        if (form.reward == "") {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Please fill the reward field.")
            return "redirect:/admin/employee"
        }
        val newReward = CashReward(totallyExistingEmployee, form.reward.toLong())

        cashRewardRepository?.save(newReward)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Reward given.")
        return "redirect:/admin/employee"
    }

    data class ScientistRewardForArticle(var employeeId: String = "", var reward: String = "")
    data class RewardResponse(var isError: Boolean = false, var message: String = "")

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
            "redirect:/admin/prawns"
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username '${form.username}' is already taken. Please try again.")
                "redirect:/admin/prawns"
            } else {
                val houseIdList = districtHouseRepository?.getAllIds()!!

                val managerIdList = managerEmployeeRepository?.getAllIds()!!

                val newUser = User(form.username, form.password, Role.PRAWN)
                val newPrawn = Prawn(form.name).apply {
                    this.user = newUser
                    this.districtHouse = districtHouseRepository?.findById(houseIdList.random())?.get()
                    this.manager = managerEmployeeRepository?.findById(managerIdList.random())?.get()
                    this.karma = 50
                    this.balance = 350
                }

                userRepository?.save(newUser)
                prawnRepository?.save(newPrawn)

                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("status", "Successfully registered a new prawn.")
                "redirect:/admin/main"
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
                "redirect:/admin/experiments"
            } else {
                checkedExperiment.statusDate = LocalDateTime.now()
                checkedExperiment.status = ExperimentStatus.APPROVED
                experimentRepository?.save(checkedExperiment)

                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/admin/experiments"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/experiments"
        }
    }


    @PostMapping("/rejectExperiment/{id}")
    fun rejectExperiment(@PathVariable id: Long, redirect: RedirectAttributes): String {
        val error = experimentChoiceError(id)
        return if (error == null) {
            val checkedExperiment = experimentRepository?.findById(id)!!.get()

            if (checkedExperiment.status != ExperimentStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/experiments"
            } else {
                checkedExperiment.statusDate = LocalDateTime.now()
                checkedExperiment.status = ExperimentStatus.REJECTED
                experimentRepository?.save(checkedExperiment)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/admin/experiments"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/experiments"
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
            "redirect:/admin/experiments"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/experiments"
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
                "redirect:/admin/newWeapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentAdmin
                }
                val newWeapon = Weapon(
                    checkedRequest.name, checkedRequest.type,
                    checkedRequest.description, checkedRequest.price, checkedRequest.requiredAccessLvl
                )
                    .apply { this.quantity = checkedRequest.quantity }
                weaponRepository?.save(newWeapon)

                newWeaponRequestRepository?.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/admin/newWeapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/newWeapons"
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
                "redirect:/admin/newWeapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentAdmin
                }
                newWeaponRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/admin/newWeapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/newWeapons"
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
            for (i in 0 until weapons.size) {
                if (weapons[i].name == checkedRequest.name)
                    weaponRepository?.delete(weapons[i])
                break
            }

            newWeaponRequestRepository?.save(checkedRequest)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:/admin/newWeapons"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/newWeapons"
        }
    }

    fun newTransportChoiceError(newTransportRequestId: Long, principal: Principal): String? {
        val request = newTransportRequestRepository?.findById(newTransportRequestId)!!
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptNewTransport/{id}")
    fun acceptNewTransport(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newTransportChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newTransportRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/newTransport"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentAdmin
                }
                val newTransport = Transport(
                    checkedRequest.name, checkedRequest.type,
                    checkedRequest.description, checkedRequest.price, checkedRequest.requiredAccessLvl
                )
                    .apply { this.quantity = checkedRequest.quantity }
                transportRepository?.save(newTransport)

                newTransportRequestRepository?.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/admin/newTransport"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/newTransport"
        }
    }


    @PostMapping("/rejectNewTransport/{id}")
    fun rejectNewTransport(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newTransportChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newTransportRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/newTransport"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentAdmin
                }
                newTransportRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/admin/newTransport"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/newTransport"
        }
    }

    @PostMapping("/undoTransportChoice/{id}")
    fun undoTranChoice(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newTransportChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newTransportRequestRepository?.findById(id)!!.get()

            checkedRequest.request!!.apply {
                this.statusDate = LocalDateTime.now()
                this.status = RequestStatus.PENDING
                this.resolver = currentAdmin
            }
            val transport = transportRepository?.findAll()!!.asReversed()
            for (i in 0 until transport.size) {
                if (transport[i].name == checkedRequest.name)
                    transportRepository?.delete(transport[i])
                break
            }

            newTransportRequestRepository?.save(checkedRequest)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:/admin/newTransport"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/newTransport"
        }
    }


    @PostMapping("/appointResolvers")
    fun appointResolversForIncident(@ModelAttribute form: AppointResolversForm, redirect: RedirectAttributes): String {
        val incident = districtIncidentRepository?.findById(form.incidentId.toLong())!!
        when {
            form.incidentId == "" || form.securityNeeded == "" || form.levelFrom == "" || form.levelTo == "" -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "One of the fields is empty. Please fill all fields.")
                return "redirect:/admin/district"
            }
            !incident.isPresent -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such incident does not exist.")
                return "redirect:/admin/district"
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
        return "redirect:/admin/district"
    }

    fun jobApplicationChoiceError(jobAppRequestId: Long, principal: Principal): String? {
        val request = vacancyApplicationRequestRepository?.findById(jobAppRequestId)!!
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptJobApplication/{id}")
    fun acceptJobApplication(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = jobApplicationChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = vacancyApplicationRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/jobApplications"
            } else {

                if (checkedRequest.vacancy?.vacantPlaces!! == 0L) {
                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.REJECTED
                        this.resolver = currentAdmin
                    }
                    vacancyApplicationRequestRepository?.save(checkedRequest)
                    redirect.addFlashAttribute("error", "No vacant places left, request cannot be satisfied.")
                    return "redirect:/admin/jobApplications"
                }

                val prawn = checkedRequest.prawn!!

                val prawnLastJob = prawn.job
                if (prawnLastJob != null) {
                    prawnLastJob.vacantPlaces++
                    vacancyRepository?.save(prawnLastJob)
                }

                val prawnNewJob = checkedRequest.vacancy
                if (prawnNewJob != null) {
                    prawnNewJob.vacantPlaces--
                    vacancyRepository?.save(prawnNewJob)
                }

                prawn.job = checkedRequest.vacancy

                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentAdmin
                }
                prawnRepository?.save(prawn)
                vacancyApplicationRequestRepository?.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/admin/jobApplications"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/jobApplications"
        }
    }

    @PostMapping("/rejectJobApplication/{id}")
    fun rejectJobApplication(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = jobApplicationChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = vacancyApplicationRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/jobApplications"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentAdmin
                }
                vacancyApplicationRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/admin/jobApplications"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/jobApplications"
        }
    }

    @PostMapping("/addVacancy")
    fun addVacancy(@ModelAttribute form: NewVacancyForm, redirect: RedirectAttributes): String {

        if (form.title == "" || form.salary == "" || form.requiredKarma == "" || form.workHoursPerWeek == "" || form.vacantPlaces == "") {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "One of the fields isn't filled.")
            return "redirect:/admin/vacancies"
        }
        val newVacancy =
            Vacancy(form.title, form.salary.toLong(), form.requiredKarma.toLong(), form.workHoursPerWeek.toInt())
                .apply { this.vacantPlaces = form.vacantPlaces.toLong() }

        vacancyRepository?.save(newVacancy)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Vacancy added.")
        return "redirect:/admin/vacancies"
    }

    fun newVacancyChoiceError(newVacancyRequestId: Long, principal: Principal): String? {
        val request = newVacancyRequestRepository?.findById(newVacancyRequestId)!!
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }


    @PostMapping("/acceptNewVacancy/{id}")
    fun acceptNewVacancy(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newVacancyChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newVacancyRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/vacancies/requests"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentAdmin
                }
                val newVacancy =
                    Vacancy(checkedRequest.title, checkedRequest.salary, checkedRequest.requiredKarma, checkedRequest.workHoursPerWeek)
                        .apply { this.vacantPlaces = checkedRequest.vacantPlaces }
                vacancyRepository?.save(newVacancy)

                newVacancyRequestRepository?.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/admin/vacancies/requests"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/vacancies/requests"
        }
    }


    @PostMapping("/rejectNewVacancy/{id}")
    fun rejectNewVacancy(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newVacancyChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newVacancyRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/vacancies/requests"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentAdmin
                }

                newVacancyRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/admin/vacancies/requests"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/vacancies/requests"
        }
    }

    @PostMapping("/undoVacancyChoice/{id}")
    fun undoVacChoice(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = newVacancyChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newVacancyRequestRepository?.findById(id)!!.get()

            checkedRequest.request!!.apply {
                this.statusDate = LocalDateTime.now()
                this.status = RequestStatus.PENDING
                this.resolver = currentAdmin
            }
            val vacancies = vacancyRepository?.findAll()!!.asReversed()
            for (i in 0 until vacancies.size) {
                if (vacancies[i].title == checkedRequest.title)
                    vacancyRepository?.delete(vacancies[i])
                break
            }

            newVacancyRequestRepository?.save(checkedRequest)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:/admin/vacancies/requests"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/vacancies/requests"
        }
    }

}