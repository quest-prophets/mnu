package mnu.controller

import mnu.form.LoginForm
import mnu.form.NewPasswordForm
import mnu.form.PrawnRegistrationForm
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@Controller
class PageController : ApplicationController() {

    @GetMapping("/username")
    @ResponseBody
    fun currentUsername(principal: Principal): String {
        return principal.name
    }

    @GetMapping("/")
    fun home(model: Model, session: HttpSession, redirect: RedirectAttributes): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val roles = AuthorityUtils.authorityListToSet(authentication.authorities)
        if (authentication != null && authentication.isAuthenticated) {
            if (roles.contains("ADMIN"))
                return "redirect:/admin/main"
            if (roles.contains("MANAGER"))
                return "redirect:/man/main"
            if (roles.contains("SCIENTIST"))
                return "redirect:/sci/main"
            if (roles.contains("SECURITY"))
                return "redirect:/sec/main"
            if (roles.contains("CUSTOMER") || roles.contains("MANUFACTURER"))
                return "redirect:/client/Shop"
            if (roles.contains("PRAWN"))
                return "redirect:/prawn/main"
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


    @GetMapping("/profile")
    fun profile(model: Model, principal: Principal): String {
        val currentEmployee = employeeRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        val cashRewards = cashRewardRepository?.findAllByEmployeeOrderByIssueDateDesc(currentEmployee!!)
        model.addAttribute("user", currentEmployee)
        model.addAttribute("form", NewPasswordForm())
        model.addAttribute("cashRewards", cashRewards)
        return "profile.html"
    }

    @PostMapping("/changePass")
    fun changePass(@ModelAttribute form: NewPasswordForm, principal: Principal, redirect: RedirectAttributes): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val regex = """[a-zA-Z0-9_.]+""".toRegex()
        val passwordEncoder = BCryptPasswordEncoder()
        when {
            !regex.matches(form.newPass) -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
                return "redirect:profile"
            }
            form.prevPass == "" || form.newPass == "" -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "One of the fields is empty. Please fill all fields.")
                return "redirect:profile"
            }
            !passwordEncoder.matches(form.prevPass, curUser.password) -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Previous password is incorrect. Please try again.")
                return "redirect:profile"
            }
        }
        curUser.password = passwordEncoder.encode(form.newPass)
        userRepository?.save(curUser)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Password changed successfully.")
        return "redirect:profile"
    }


}