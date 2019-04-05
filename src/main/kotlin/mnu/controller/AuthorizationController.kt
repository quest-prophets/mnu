package mnu.controller

import mnu.form.LoginForm
import mnu.form.ClientRegistrationForm
import mnu.model.Client
import mnu.model.User
import mnu.model.enums.ClientType
import mnu.model.enums.Role
import mnu.repository.ClientRepository
import mnu.repository.UserRepository
import mnu.repository.employee.ManagerEmployeeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/auth")
class AuthorizationController {

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val clientRepository: ClientRepository? = null

    @Autowired
    private val managerEmployeeRepository: ManagerEmployeeRepository? = null

    @GetMapping("/login")
    fun login(model: Model): String {
        model.addAttribute("form", LoginForm())
        return "/login.html"
    }

    @GetMapping("/register")
    fun register(model: Model): String {
        model.addAttribute("form", ClientRegistrationForm())
        return "/register.html"
    }

    @GetMapping("/logout")
    fun logout() = "/index.html"

    @PostMapping("/register")
    @ResponseBody
    fun addUser(@ModelAttribute form: ClientRegistrationForm): String {
        val existingUser = userRepository?.findByLogin(form.username)
        val regex = """[a-zA-Z0-9_.]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            "Only latin letters, numbers and underscore are supported."
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                "Username '${form.username}' is already taken. Please try again."
            } else {
                val role = when (form.type) {
                    "customer" -> Role.CUSTOMER
                    "manufacturer" -> Role.MANUFACTURER
                    else -> return "Error"
                }
                val newUser = User(form.username, form.password, role)

                val clientType = when (form.type) {
                    "customer" -> ClientType.CUSTOMER
                    "manufacturer" -> ClientType.MANUFACTURER
                    else -> return "Error"
                }
                val newClientUser = Client(form.name, form.email, clientType).apply { this.user = newUser }

                val managerIdList = managerEmployeeRepository?.getAllIds()!!
                val managerIdNormalList = ArrayList<Long>()
                for (i in 0 until managerIdList.size) {
                    managerIdNormalList.add(managerIdList[i].longValueExact())
                }

                if (clientType == ClientType.CUSTOMER)
                    newClientUser.apply { this.manager = managerEmployeeRepository.findById(managerIdNormalList.random()).get() }

                userRepository?.save(newUser)
                clientRepository?.save(newClientUser)

                "redirect:index.html"
            }
        }
    }

}