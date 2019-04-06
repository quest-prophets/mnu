package mnu.controller

import mnu.form.NewArticleForm
import mnu.form.NewExperimentForm
import mnu.form.NewReportForm
import mnu.model.Article
import mnu.model.Experiment
import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentStatus
import mnu.model.enums.ExperimentType
import mnu.model.enums.RequestStatus
import mnu.model.request.Request
import mnu.repository.ArticleRepository
import mnu.repository.ExperimentRepository
import mnu.repository.employee.ScientistEmployeeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Controller
@RequestMapping("/sci")
class ScientistController : ApplicationController() {

    @ModelAttribute("level")
    fun getLevel(principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        return employeeRepository?.findById(curUser.id!!)?.get()!!.level.toString()
    }

    @Autowired
    val scientistEmployeeRepository: ScientistEmployeeRepository? = null

    @Autowired
    val articleRepository: ArticleRepository? = null

    @Autowired
    val experimentRepository: ExperimentRepository? = null


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

        val experimentType = when (form.type) {
            "minor" -> ExperimentType.MINOR
            "major" -> ExperimentType.MAJOR
            else -> return "Error - such experiment type does not exist."
        }
//        val experimentDate = LocalDateTime.parse(form.date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))

        val currentScientist = scientistEmployeeRepository?.findById(user?.id!!)?.get()
        if (form.assistantId != null) {
            val requestedAssistant: ScientistEmployee? = scientistEmployeeRepository?.findById(form.assistantId)?.orElse(null)
            return if (requestedAssistant == null)
                "Scientist with such id does not exist."
            else {

                if (requestedAssistant.employee!!.level!! < currentScientist!!.employee!!.level!!) {

                    experimentRepository?.save(
                        Experiment(
                            form.title, experimentType,
                            form.description, currentScientist, requestedAssistant
                        ).apply { this.status = ExperimentStatus.PENDING })

                    "Request sent. Wait for supervisor's decision."
                } else "Requested assistant's level is higher than yours."
            }
        } else {
            experimentRepository?.save(
                Experiment(
                    form.title, experimentType,
                    form.description, currentScientist, null
                ).apply { this.status = ExperimentStatus.PENDING })

            return "Request sent. Wait for supervisor's decision."
        }
    }

    @PostMapping("/report")
    @ResponseBody
    fun addReport(@ModelAttribute form: NewReportForm, principal: Principal) : String {
        return "GAVNO"
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