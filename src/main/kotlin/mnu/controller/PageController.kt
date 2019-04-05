package mnu.controller

import mnu.form.PrawnRegistrationForm
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
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
    fun clientsShop() = "customers/customer__shop.html"

    @GetMapping("/prawnMain")
    fun prawnMain() = "prawns/prawn__main.html"

    @GetMapping("/employeeDB")
    fun adminEmployees() = "administrators/admin__employees.html"

    @GetMapping("/adminMenu")
    fun adminMenu() = "administrators/admin__menu.html"

    @GetMapping("/managersMenu")
    fun manMenu() = "managers/manager__menu.html"

    @GetMapping("/prawnRegister")
    fun prawnRegister(model: Model): String {
        model.addAttribute("form", PrawnRegistrationForm())
        return "administrators/admin__prawn-registration.html"
    }

    @GetMapping("/sciMain")
    fun sciMain() = "scientists/sci__main.html"

    @GetMapping("/sciExperiment")
    fun sciExperiment() = "scientists/sci__new-experiment.html"

    @GetMapping("/secMain")
    fun secMain() = "security/sec__main.html"

}