package mnu.controller

import mnu.model.User
import mnu.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.*

@Controller
@RequestMapping("/auth")
class AuthorizationController {

    @Autowired
    private val userRepository: UserRepository? = null

    @GetMapping("/login")
    fun login() = "/index.html"

    @GetMapping("/register")
    fun register() = "/index.html"

    @GetMapping("/logout")
    fun logout() = "/index.html"

    @PostMapping("/register")
    @ResponseBody
    fun addUser(@RequestBody user: User): AuthResponse {
        val existingUser = userRepository?.findByLogin(user.login)
        val authResponse = AuthResponse(user.login, AuthType.REGISTER)
        val regex = """[a-zA-Z0-9_]+""".toRegex()

        return if (!regex.matches(user.login) || !regex.matches(user.password)) {
            authResponse.success = false
            authResponse.message = "Only latin letters, numbers and underscore are supported."
            authResponse
        } else {

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(user.password)
            user.password = encodedPassword

            return if (existingUser != null) {
                authResponse.success = false
                authResponse.message = "Username '${user.login}' is already taken. Please try again."
                authResponse
            } else {

//              user.active = true
//              user.roles = Collections.singleton(Role.USER)
                userRepository?.save(user)

                authResponse.success = true
                authResponse.message = "Successfully registered. You can sign in with your username and password."
                authResponse
            }
        }
    }

    enum class AuthType { LOGIN, REGISTER }

    data class AuthResponse(var login: String = "", var type: AuthType = AuthType.LOGIN,
                            var success: Boolean = false, var message: String = "")
}