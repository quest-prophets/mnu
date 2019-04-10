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
                "redirect:main/requests" //todo
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
                        return "redirect:equipment" //todo
                    }
                    checkedRequest.transport?.quantity!! == 0L -> {
                        checkedRequest.request!!.apply {
                            this.statusDate = LocalDateTime.now()
                            this.status = RequestStatus.REJECTED
                            this.resolver = currentManager
                        }
                        changeEquipmentRequestRepository?.save(checkedRequest)
                        redirect.addFlashAttribute("error", "Transport is out of stock, request cannot be satisfied.")
                        return "redirect:equipment" //todo
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
                "redirect:main/requests"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/requests" //todo
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
                "redirect:main/requests"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentManager
                }
                changeEquipmentRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:main/requests"
            }

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
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository?.findById(id)!!.get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:main/newWeapons"
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
                "redirect:main/newWeapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/newWeapons"
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
                "redirect:main/newWeapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentManager
                }
                newWeaponRequestRepository?.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:main/newWeapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/newWeapons"
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
            "redirect:main/newWeapons"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:main/newWeapons"
        }
    }

}