package mnu.controller

import mnu.model.enums.Role
import mnu.repository.ClientRepository
import mnu.repository.PrawnRepository
import mnu.repository.UserRepository
import mnu.repository.employee.EmployeeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import java.security.Principal

@Controller
class ApplicationController {
    @Autowired
    val userRepository: UserRepository? = null

    @Autowired
    val clientRepository: ClientRepository? = null

    @Autowired
    val prawnRepository: PrawnRepository? = null

    @Autowired
    val employeeRepository: EmployeeRepository? = null

    @ModelAttribute("fullname")
    fun getFullname(principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        return if (curUser.role == Role.PRAWN) {
            val name = prawnRepository?.findById(curUser.id!!)?.get()!!.name
            name
        } else {
            if (curUser.role == Role.CUSTOMER || curUser.role == Role.MANUFACTURER) {
                val name = clientRepository?.findById(curUser.id!!)?.get()!!.name
                name
            } else {
                val name = employeeRepository?.findById(curUser.id!!)?.get()!!.name
                name
            }
        }
    }

    @ModelAttribute("level")
    fun getLevel(principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        return employeeRepository?.findById(curUser.id!!)?.get()!!.level.toString()
    }

    @ModelAttribute("karma")
    fun getKarma(principal: Principal) : String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        return prawnRepository?.findById(curUser.id!!)?.get()!!.karma.toString()
    }

    @ModelAttribute("balance")
    fun getBalance(principal: Principal) : String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        return prawnRepository?.findById(curUser.id!!)?.get()!!.balance.toString()
    }
}