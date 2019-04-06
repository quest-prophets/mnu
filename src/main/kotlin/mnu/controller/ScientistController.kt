package mnu.controller

import mnu.form.NewArticleForm
import mnu.form.NewExperimentForm
import mnu.model.Article
import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentType
import mnu.model.enums.RequestStatus
import mnu.model.request.ExperimentRequest
import mnu.model.request.Request
import mnu.repository.ArticleRepository
import mnu.repository.UserRepository
import mnu.repository.employee.EmployeeRepository
import mnu.repository.employee.ScientistEmployeeRepository
import mnu.repository.request.ExperimentRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

//    @Autowired
//    val experimentRepository: ExperimentRepository? = null

    @Autowired
    val experimentRequestRepository: ExperimentRequestRepository? = null

    @GetMapping("/main")
    fun main() = "scientists/sci__main.html"

    @GetMapping("/main/articles")
    fun mainArticles() = "scientists/sci__main_articles.html"


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

    @PostMapping("/experiment")
    @ResponseBody
    fun requestExperiment(@ModelAttribute form: NewExperimentForm, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }

        val experimentType = when (form.type) {
            "minor" -> ExperimentType.MINOR
            "major" -> ExperimentType.MAJOR
            else -> return "Error - such experiment type does not exist."
        }
        val experimentDate = LocalDateTime.parse(form.date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))

        val currentScientist = scientistEmployeeRepository?.findById(user?.id!!)?.get()
        if (form.assistantId != null) {
            val requestedAssistant: ScientistEmployee? = scientistEmployeeRepository?.findById(form.assistantId)?.orElse(null)
            return if (requestedAssistant == null)
                "Scientist with such id does not exist."
            else {

                if (requestedAssistant.employee!!.level!! < currentScientist!!.employee!!.level!!) {

                    experimentRequestRepository?.save(
                        ExperimentRequest(
                            form.title, experimentType,
                            form.description, experimentDate, currentScientist, requestedAssistant
                        ).apply { this.request = newRequest })

                    "Request sent. Wait for supervisor's decision."
                } else "Requested assistant's level is higher than yours."
            }
        } else {
            experimentRequestRepository?.save(
                ExperimentRequest(
                    form.title, experimentType,
                    form.description, experimentDate, currentScientist, null
                ).apply { this.request = newRequest })

            return "Request sent. Wait for supervisor's decision."
        }
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