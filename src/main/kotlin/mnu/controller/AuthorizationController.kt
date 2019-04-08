package mnu.controller

import mnu.form.ClientRegistrationForm
import mnu.form.LoginForm
import mnu.model.Client
import mnu.model.User
import mnu.model.enums.ClientType
import mnu.model.enums.Role
import mnu.repository.ClientRepository
import mnu.repository.UserRepository
import mnu.repository.employee.ManagerEmployeeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.servlet.http.HttpSession

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
    fun login(model: Model, session: HttpSession, redirect: RedirectAttributes): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val roles = AuthorityUtils.authorityListToSet(authentication.authorities)
        if (authentication != null && authentication.isAuthenticated) {
            if (roles.contains("ADMIN"))
                return "redirect:admin/main"
            if (roles.contains("MANAGER"))
                return "redirect:man/main"
            if (roles.contains("SCIENTIST"))
                return "redirect:sci/main"
            if (roles.contains("SECURITY"))
                return "redirect:sec/main"
            if (roles.contains("CUSTOMER") || roles.contains("MANUFACTURER"))
                return "redirect:clientsShop"
            if (roles.contains("PRAWN"))
                return "redirect:prawnMain"
        }
        model.addAttribute("form", LoginForm())
        if (session.getAttribute("loginFailed") == true) {
            session.removeAttribute("loginFailed")
            model.addAttribute("loginFailed", true)
        }
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

                if (clientType == ClientType.CUSTOMER)
                    newClientUser.apply {
                        this.manager = managerEmployeeRepository.findById(managerIdList.random()).get()
                    }

                userRepository?.save(newUser)
                clientRepository?.save(newClientUser)

                "redirect:index.html"
            }
        }
    }

}