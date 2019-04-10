package mnu.controller

import mnu.form.PrawnRegistrationForm
import mnu.model.Prawn
import mnu.model.User
import mnu.model.Weapon
import mnu.model.enums.RequestStatus
import mnu.model.enums.Role
import mnu.model.request.NewWeaponRequest
import mnu.repository.*
import mnu.repository.employee.ManagerEmployeeRepository
import mnu.repository.employee.SecurityEmployeeRepository
import mnu.repository.request.ChangeEquipmentRequestRepository
import mnu.repository.request.NewWeaponRequestRepository
import mnu.repository.request.VacancyApplicationRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import java.time.LocalDateTime

@Controller
@RequestMapping("/man")
class ManagerController : ApplicationController() {

    @Autowired
    val managerEmployeeRepository: ManagerEmployeeRepository? = null
    @Autowired
    val securityEmployeeRepository: SecurityEmployeeRepository? = null

    @Autowired
    val weaponRepository: WeaponRepository? = null
    @Autowired
    val transportRepository: TransportRepository? = null

    @Autowired
    val districtHouseRepository: DistrictHouseRepository? = null

    @Autowired
    val newWeaponRequestRepository: NewWeaponRequestRepository? = null

    @Autowired
    val changeEquipmentRequestRepository: ChangeEquipmentRequestRepository? = null

    @Autowired
    val vacancyRepository: VacancyRepository? = null
    @Autowired
    val vacancyApplicationRequestRepository: VacancyApplicationRequestRepository? = null

    @GetMapping("/main")
    fun manMenu() = "managers/manager__main.html"

    @GetMapping("/clients")
    fun manClients(principal: Principal, model: Model) : String {
        val user = userRepository?.findByLogin(principal.name)!!
        val curManager = managerEmployeeRepository?.findById(user.id!!)!!.get()
        model.addAttribute("clients", clientRepository?.findAllByManager(curManager))
        return "managers/manager__client-list.html"
    }

    @GetMapping("/prawns")
    fun manPrawns(principal: Principal, model: Model) : String {
        val user = userRepository?.findByLogin(principal.name)!!
        val curManager = managerEmployeeRepository?.findById(user.id!!)!!.get()
        model.addAttribute("clients", prawnRepository?.findAllByManager(curManager))
        return "managers/manager__prawn-list.html"
    }

    @GetMapping("/newWeapons")
    fun manNewWeapons(principal: Principal, model: Model) : String {
        val weaponRequests = newWeaponRequestRepository?.findAll()
        val requestsForManager = ArrayList<NewWeaponRequest>()
        weaponRequests!!.forEach {
            if ((it.user!!.role == Role.SECURITY || it.user!!.role == Role.SCIENTIST) && it.request!!.status == RequestStatus.PENDING)
                requestsForManager.add(it)
        }
        model.addAttribute("requests", requestsForManager)
        return "managers/manager__new-weapons.html"
    }


    @PostMapping("/registerPrawn")
    @ResponseBody
    fun addPrawn(@ModelAttribute form: PrawnRegistrationForm, principal: Principal, redirect: RedirectAttributes): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val possibleManager = managerEmployeeRepository?.findById(curUser.id!!)!!
        if (possibleManager.isPresent) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "You are not a manager.")
            return "redirect:/"
        }

        val existingUser = userRepository?.findByLogin(form.username)
        val regex = """[a-zA-Z0-9_.]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
            "redirect:/man/prawns"
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username '${form.username}' is already taken. Please try again.")
                "redirect:/man/prawns"
            } else {
                val houseIdList = districtHouseRepository?.getAllIds()!!

                val newUser = User(form.username, form.password, Role.PRAWN)
                val newPrawn = Prawn(form.name).apply {
                    this.user = newUser
                    this.districtHouse = districtHouseRepository?.findById(houseIdList.random())?.get()
                    this.manager = possibleManager.get()
                    this.karma = 50
                    this.balance = 350
                }

                userRepository?.save(newUser)
                prawnRepository?.save(newPrawn)

                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("status", "Successfully registered a new prawn.")
                "redirect:/man/main"
            }
        }
    }

    fun newEquipmentChoiceError(newEquipmentRequestId: Long, principal: Principal) : String? {
        val request = changeEquipmentRequestRepository?.findById(newEquipmentRequestId)!!
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptNewEquipment/{id}")
    fun acceptNewEquipment(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes) : String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newEquipmentChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = changeEquipmentRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/newEquipment" //todo
            } else {
                when {
                    checkedRequest.weapon?.quantity!! == 0L -> {
                        checkedRequest.request!!.apply {
                            this.statusDate = LocalDateTime.now()
                            this.status = RequestStatus.REJECTED
                            this.resolver = currentManager
                        }
                        changeEquipmentRequestRepository?.save(checkedRequest)
                        redirect.addFlashAttribute("error", "Weapon is out of stock, request cannot be satisfied.")
                        return "redirect:/man/newEquipment" //todo
                    }
                    checkedRequest.transport?.quantity!! == 0L -> {
                        checkedRequest.request!!.apply {
                            this.statusDate = LocalDateTime.now()
                            this.status = RequestStatus.REJECTED
                            this.resolver = currentManager
                        }
                        changeEquipmentRequestRepository?.save(checkedRequest)
                        redirect.addFlashAttribute("error", "Transport is out of stock, request cannot be satisfied.")
                        return "redirect:/man/newEquipment" //todo
                    }
                }

                val securityEmployee = checkedRequest.employee!!

                val employeeLastWeapon = securityEmployee.weapon
                val employeeLastTransport = securityEmployee.transport
                if (employeeLastWeapon != null) {
                    employeeLastWeapon.quantity++
                    weaponRepository?.save(employeeLastWeapon)
                }
                if (employeeLastTransport != null) {
                    employeeLastTransport.quantity++
                    transportRepository?.save(employeeLastTransport)
                }

                val employeeNewWeapon = checkedRequest.weapon
                val employeeNewTransport = checkedRequest.transport
                if (employeeNewWeapon != null) {
                    employeeNewWeapon.quantity--
                    weaponRepository?.save(employeeNewWeapon)
                }
                if (employeeNewTransport != null) {
                    employeeNewTransport.quantity--
                    transportRepository?.save(employeeNewTransport)
                }

                securityEmployee.weapon = checkedRequest.weapon
                securityEmployee.transport = checkedRequest.transport

                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentManager
                }
                securityEmployeeRepository?.save(securityEmployee)
                changeEquipmentRequestRepository?.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/man/newEquipment"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/newEquipment" //todo
        }
    }

    @PostMapping("/rejectNewEquipment/{id}")
    fun rejectNewEquipment(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes) : String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newEquipmentChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = changeEquipmentRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/newEquipment"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentManager
                }
                changeEquipmentRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/man/newEquipment"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/newEquipment"
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
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/newWeapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentManager
                }
                val newWeapon = Weapon(checkedRequest.name, checkedRequest.type,
                    checkedRequest.description, checkedRequest.price, checkedRequest.requiredAccessLvl)
                    .apply { this.quantity = checkedRequest.quantity }
                weaponRepository?.save(newWeapon)

                newWeaponRequestRepository?.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/man/newWeapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/newWeapons"
        }
    }


    @PostMapping("/rejectNewWeapon/{id}")
    fun rejectNewWeapon(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/newWeapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentManager
                }
                newWeaponRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/man/newWeapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/newWeapons"
        }
    }

    @PostMapping("/undoWeaponChoice/{id}")
    fun undoWeapChoice(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            checkedRequest.request!!.apply {
                this.statusDate = LocalDateTime.now()
                this.status = RequestStatus.PENDING
                this.resolver = currentManager
            }
            val weapons = weaponRepository?.findAll()!!.asReversed()
            for(i in 0 until weapons.size) {
                if (weapons[i].name == checkedRequest.name)
                    weaponRepository?.delete(weapons[i])
                break
            }

            newWeaponRequestRepository?.save(checkedRequest)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:/man/newWeapons"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/newWeapons"
        }
    }

    fun jobApplicationChoiceError(jobAppRequestId: Long, principal: Principal): String? {
        val request = vacancyApplicationRequestRepository?.findById(jobAppRequestId)!!
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptJobApplication/{id}")
    fun acceptJobApplication(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes) : String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = jobApplicationChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = vacancyApplicationRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/jobApplications"
            } else {
                if (checkedRequest.prawn!!.manager != managerEmployeeRepository?.findById(currentManager!!.id!!)!!.get()) {
                    redirect.addFlashAttribute("error", "You are not this prawn's supervising manager.")
                    return "redirect:/man/jobApplications"
                }

                if (checkedRequest.vacancy?.vacantPlaces!! == 0L) {
                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.REJECTED
                        this.resolver = currentManager
                    }
                    vacancyApplicationRequestRepository?.save(checkedRequest)
                    redirect.addFlashAttribute("error", "No vacant places left, request cannot be satisfied.")
                    return "redirect:/man/jobApplications"
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
                    this.resolver = currentManager
                }
                prawnRepository?.save(prawn)
                vacancyApplicationRequestRepository?.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/man/jobApplications"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/jobApplications"
        }
    }

    @PostMapping("/rejectJobApplication/{id}")
    fun rejectJobApplication(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes) : String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = jobApplicationChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = vacancyApplicationRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/jobApplications"
            } else {
                if (checkedRequest.prawn!!.manager != managerEmployeeRepository?.findById(currentManager!!.id!!)!!.get()) {
                    redirect.addFlashAttribute("error", "You are not this prawn's supervising manager.")
                    return "redirect:/man/jobApplications"
                }

                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentManager
                }
                vacancyApplicationRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/man/jobApplications"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/jobApplications"
        }
    }

}