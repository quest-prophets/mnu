package mnu.controller

import mnu.form.LoginForm
import mnu.form.PrawnRegistrationForm
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.support.RedirectAttributes
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
    fun home(model: Model, session: HttpSession, redirect: RedirectAttributes) : String {
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