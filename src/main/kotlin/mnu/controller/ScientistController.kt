package mnu.controller

import mnu.form.NewArticleForm
import mnu.model.Article
import mnu.form.NewExperimentForm

import mnu.repository.ArticleRepository
import mnu.repository.ExperimentRepository
import mnu.repository.UserRepository
import mnu.repository.employee.EmployeeRepository
import mnu.repository.employee.ScientistEmployeeRepository
import mnu.repository.request.ExperimentRequestRepository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Controller
@RequestMapping("/sci")
class ScientistController {
    @Autowired
    val userRepository: UserRepository? = null

    @Autowired
    val employeeRepository: EmployeeRepository? = null

    @Autowired
    val scientistEmployeeRepository: ScientistEmployeeRepository? = null

    @Autowired
    val articleRepository: ArticleRepository? = null

    @Autowired
    val experimentRepository: ExperimentRepository? = null

    @Autowired
    val experimentRequestRepository: ExperimentRequestRepository? = null

    @GetMapping("/main")
    fun main() = "scientists/sci__main.html"


    @GetMapping("/article")
    fun article(model: Model): String {
        model.addAttribute("form", NewArticleForm())
        return "scientists/sci__new-article.html"
    }

    @GetMapping("/experiment")
    fun experiment(model: Model, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentScientistLvl = employeeRepository?.findById(user?.id!!)?.get()?.level
        model.addAttribute("assistants", scientistEmployeeRepository?.getAssistants(currentScientistLvl!!))
        model.addAttribute("form", NewExperimentForm())
        return "/scientists/sci__new-experiment.html"
    }

    @PostMapping("/article")
    @ResponseBody
    fun addArticle(@ModelAttribute form: NewArticleForm, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentScientist = scientistEmployeeRepository?.findById(user?.id!!)?.get()
        articleRepository?.save(Article(form.title, form.article).apply { this.scientist = currentScientist })
        return "Article added."
    }
}