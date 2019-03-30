package mnu.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal

@Controller
class PageController {

    @GetMapping("/username")
    @ResponseBody
    fun currentUsername(principal: Principal): String {
        return principal.name
    }

    @GetMapping("/")
    fun home() = "index.html"

    @GetMapping("/register")
    fun register() = "register.html"

    @GetMapping("/clientsShop")
    fun clientsShop() = "clientsShop.html"

    @GetMapping("/employeeDB")
    fun adminEmployees() = "administratorsEmployees.html"

    @GetMapping("/adminMenu")
    fun adminMenu() = "administratorsMenu.html"

    @GetMapping("/managersMenu")
    fun manMenu() = "managersMenu.html"

    @GetMapping("/prawnRegister")
    fun prawnRegister() = "prawnRegistration.html"

    @GetMapping("/sciMain")
    fun sciMain() = "scientistsMain.html"

    @GetMapping("/sciExperiment")
    fun sciExperiment() = "scientistsNewExperiment.html"

    @GetMapping("/secMain")
    fun secMain() = "securityMain.html"

}