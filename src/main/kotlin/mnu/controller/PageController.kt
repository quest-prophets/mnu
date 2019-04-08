package mnu.controller

import mnu.form.LoginForm
import mnu.form.PrawnRegistrationForm
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@Controller
class PageController {

    @GetMapping("/username")
    @ResponseBody
    fun currentUsername(principal: Principal): String {
        return principal.name
    }

    @GetMapping("/")
    fun home(model: Model, session: HttpSession) : String {
        val authentication = SecurityContextHolder.getContext().authentication
        val roles = AuthorityUtils.authorityListToSet(authentication.authorities)
        if (authentication != null && authentication.isAuthenticated) {
            if (roles.contains("ADMIN"))
                return "administrators/admin__menu.html"
            if (roles.contains("MANAGER"))
                return "managers/manager__main.html"
            if (roles.contains("SCIENTIST"))
                return "scientists/sci__main.html"
            if (roles.contains("SECURITY"))
                return "security/sec__main.html"
            if (roles.contains("CUSTOMER") || roles.contains("MANUFACTURER"))
                return "customers/customer__shop.html"
            if (roles.contains("PRAWN"))
                return "prawns/prawn__main.html"
        }
        model.addAttribute("form", LoginForm())
        if (session.getAttribute("loginFailed") == true) {
            session.removeAttribute("loginFailed")
            model.addAttribute("loginFailed", true)
        }
        return "/login.html"
    }

    @GetMapping("/register")
    fun register() = "register.html"

    @GetMapping("/clientsShop")
    fun clientsShop() = "customers/customer__shop.html"

    @GetMapping("/prawnMain")
    fun prawnMain() = "prawns/prawn__main.html"


//    @GetMapping("/prawnRegister")
//    fun prawnRegister(model: Model): String {
//        model.addAttribute("form", PrawnRegistrationForm())
//        return "administrators/admin__prawn-registration.html"
//    }


}