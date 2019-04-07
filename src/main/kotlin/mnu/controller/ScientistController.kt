package mnu.controller

import mnu.form.NewArticleForm
import mnu.form.NewExperimentForm
import mnu.form.NewReportForm
import mnu.model.Article
import mnu.model.Experiment
import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentStatus
import mnu.model.enums.ExperimentType
import mnu.repository.ArticleRepository
import mnu.repository.ExperimentRepository
import mnu.repository.employee.ScientistEmployeeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    fun main(model: Model, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)!!
        model.addAttribute("experiments", experimentRepository?.findAllByExaminatorIdOrderByStatusAsc(user.id!!))
        return "scientists/sci__main.html"
    }

    @GetMapping("/main/articles")
    fun mainArticles(model: Model, principal: Principal) : String {
        val user = userRepository?.findByLogin(principal.name)!!
        model.addAttribute("articles", articleRepository?.findAllByScientistId(user.id!!))
        return "scientists/sci__main_articles.html"
    }

    @GetMapping("/main/requests")
    fun mainRequests(model: Model, principal: Principal) : String {
        val experiments = experimentRepository?.findAllByStatusAndType(ExperimentStatus.PENDING, ExperimentType.MINOR)
        val validExperiments = ArrayList<Experiment>()

        val user = userRepository?.findByLogin(principal.name)
        val currentScientistLvl = employeeRepository?.findById(user?.id!!)?.get()?.level!!

        experiments?.forEach {
            if (currentScientistLvl > it.examinator!!.employee!!.level!!)
                validExperiments.add(it)
        }
        model.addAttribute("experiments", validExperiments)
        return "scientists/sci__main_requests.html"
    }

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
        if (!model.containsAttribute("form"))
            model.addAttribute("form", NewExperimentForm())
        return "/scientists/sci__new-experiment.html"
    }

    @PostMapping("/experiment")
    fun requestExperiment(
        @ModelAttribute form: NewExperimentForm, principal: Principal,
        redirect: RedirectAttributes
    ): String {
        val user = userRepository?.findByLogin(principal.name)

        val experimentType = when (form.type) {
            "minor" -> ExperimentType.MINOR
            "major" -> ExperimentType.MAJOR
            else -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Experiment type does not exist.")
                return "redirect:experiment"
            }
        }

        val currentScientist = scientistEmployeeRepository?.findById(user?.id!!)?.get()
        val assistant = form.assistantId?.let { assistantId ->
            val requestedAssistant = scientistEmployeeRepository?.findById(assistantId)?.orElse(null)
            when {
                requestedAssistant == null -> {
                    redirect.addFlashAttribute("form", form)
                    redirect.addFlashAttribute("error", "Scientist does not exist.")
                    return "redirect:experiment"
                }
                requestedAssistant.employee!!.level!! >= currentScientist!!.employee!!.level!! -> {
                    redirect.addFlashAttribute("form", form)
                    redirect.addFlashAttribute("error", "Requested assistant's level is higher than yours.")
                    return "redirect:main"
                }
                else -> requestedAssistant
            }
        }

        experimentRepository?.save(
            Experiment(
                form.title, experimentType,
                form.description, currentScientist, assistant
            ).apply {
                this.status = ExperimentStatus.PENDING
                this.statusDate = LocalDateTime.now()
            })

        redirect.addFlashAttribute("status", "Request sent. Wait for supervisor's decision.")
        return "redirect:main"
    }

    fun reportAccessError(experimentId: Long, principal: Principal): String? {
        val user = userRepository?.findByLogin(principal.name)
        val possibleScientist = scientistEmployeeRepository?.findById(user?.id!!)!!
        if (possibleScientist.isPresent)
            return "You are not a scientist."
        val currentScientist = possibleScientist.get()
        val experiment = experimentRepository?.findById(experimentId)!!
        if (!experiment.isPresent)
            return "Experiment with such id does not exist."
        if (experiment.get().examinator != currentScientist)
            return "You are not allowed to write a report on experiment you did not conduct."
        if (experiment.get().assistant == currentScientist)
            return "Assistants are not allowed to write a report on experiments."

        return null
    }

    @GetMapping("/report/{id}")
    @ResponseBody
    fun report(@PathVariable id: Long, model: Model, principal: Principal): String {
        val error = reportAccessError(id, principal)
        if (error == null)
            model.addAttribute("form", NewArticleForm())
        else
            model.addAttribute("error", error)

        return "scientists/sci__new-article.html"
    }

    @PostMapping("/report")
    @ResponseBody
    fun addReport(@ModelAttribute form: NewReportForm, principal: Principal): String {
        val error = reportAccessError(form.experimentId, principal)
        return if (error == null) {
            val experiment = experimentRepository?.findById(form.experimentId)!!
            val experimentDate =
                LocalDateTime.parse(form.date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            experimentRepository?.save(experiment.get().apply {
                this.statusDate = experimentDate
                this.result = form.result
                this.status = ExperimentStatus.FINISHED
            })
            "Report submitted."
        } else
            error
    }

    @PostMapping("/article")
    @ResponseBody
    fun addArticle(@ModelAttribute form: NewArticleForm, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentScientist = scientistEmployeeRepository?.findById(user?.id!!)?.get()
        articleRepository?.save(Article(form.title, form.article).apply { this.scientist = currentScientist })
        return "Article added."
    }

    fun choiceError(experimentId: Long, principal: Principal): String? {
        val experiment = experimentRepository?.findById(experimentId)!!
        val user = userRepository?.findByLogin(principal.name)
        val currentScientist = scientistEmployeeRepository?.findById(user?.id!!)?.get()
        if (!experiment.isPresent)
            return "Experiment with such id does not exist."
        val checkedExperiment = experiment.get()
        if (checkedExperiment.type == ExperimentType.MINOR)
            return "Major experiment requests are handled by administrators."
        if (checkedExperiment.examinator!!.employee!!.level!! >= currentScientist!!.employee!!.level!!)
            return "Examinators' level is equal to or higher than yours."

        return null
    }

    @PostMapping("/acceptExperiment/{id}")
    @ResponseBody
    fun acceptExperiment(@PathVariable id: Long, principal: Principal): String {
        val error = choiceError(id, principal)
        return if (error == null) {
            val checkedExperiment = experimentRepository?.findById(id)!!.get()

            if (checkedExperiment.status != ExperimentStatus.PENDING)
                "Request has already been handled."
            else {
                checkedExperiment.statusDate = LocalDateTime.now()
                checkedExperiment.status = ExperimentStatus.APPROVED
                experimentRepository?.save(checkedExperiment)
                "Request accepted."
            }

        } else error
    }


    @PostMapping("/rejectExperiment/{id}")
    @ResponseBody
    fun rejectExperiment(@PathVariable id: Long, principal: Principal): String {
        val error = choiceError(id, principal)
        return if (error == null) {
            val checkedExperiment = experimentRepository?.findById(id)!!.get()

            if (checkedExperiment.status != ExperimentStatus.PENDING)
                "Request has already been handled."
            else {
                checkedExperiment.statusDate = LocalDateTime.now()
                checkedExperiment.status = ExperimentStatus.REJECTED
                experimentRepository?.save(checkedExperiment)
                "Request rejected."
            }

        } else error
    }

    @PostMapping("/undoExperimentChoice/{id}")
    @ResponseBody
    fun undoExpChoice(@PathVariable id: Long, principal: Principal): String {
        val error = choiceError(id, principal)
        return if (error == null) {
            val checkedExperiment = experimentRepository?.findById(id)!!.get()

            checkedExperiment.statusDate = LocalDateTime.now()
            checkedExperiment.status = ExperimentStatus.PENDING
            experimentRepository?.save(checkedExperiment)
            "Undone."

        } else error
    }
}