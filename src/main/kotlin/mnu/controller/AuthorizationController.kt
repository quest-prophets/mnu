package mnu.controller

import mnu.form.RegistrationForm
import mnu.model.Client
import mnu.model.User
import mnu.model.enums.ClientType
import mnu.model.enums.Role
import mnu.repository.ClientRepository
import mnu.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.util.*

@Controller
@RequestMapping("/auth")
class AuthorizationController {

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val clientRepository: ClientRepository? = null

    @GetMapping("/login")
    fun login() = "/index.html"

    @GetMapping("/register")
    fun register(model: Model): String {
        model.addAttribute("form", RegistrationForm())
        return "/register.html"
    }

    @GetMapping("/logout")
    fun logout() = "/index.html"

    @PostMapping("/register")
    @ResponseBody
    fun addUser(@ModelAttribute form: RegistrationForm): String {
        val existingUser = userRepository?.findByLogin(form.username)
//        val authResponse = AuthResponse(form.username, AuthType.REGISTER)
        val regex = """[a-zA-Z0-9_]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            "Only latin letters, numbers and underscore are supported."
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                "Username '${form.username}' is already taken. Please try again."
            } else {
                val newUser = User(form.username, form.password, Role.valueOf(form.type))
                
                userRepository?.save(newUser)
                clientRepository?.save(Client(form.name, form.email, ClientType.valueOf(form.type)).apply { this.user = newUser })

                "redirect:index.html"
            }
        }
    }

//    enum class AuthType { LOGIN, REGISTER }
//
//    data class AuthResponse(
//        var login: String = "", var type: AuthType = AuthType.LOGIN,
//        var success: Boolean = false, var message: String = ""
//    )
}