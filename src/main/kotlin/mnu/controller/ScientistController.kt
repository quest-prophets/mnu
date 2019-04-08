package mnu.controller

import mnu.form.NewArticleForm
import mnu.form.NewExperimentForm
import mnu.form.NewPasswordForm
import mnu.form.NewReportForm
import mnu.model.Article
import mnu.model.Experiment
import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentStatus
import mnu.model.enums.ExperimentType
import mnu.model.enums.RequestStatus
import mnu.model.enums.WeaponType
import mnu.model.request.NewWeaponRequest
import mnu.model.request.Request
import mnu.repository.ArticleRepository
import mnu.repository.CashRewardRepository
import mnu.repository.ExperimentRepository
import mnu.repository.WeaponRepository
import mnu.repository.employee.ScientistEmployeeRepository
import mnu.repository.request.NewWeaponRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
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

    @Autowired
    val cashRewardRepository: CashRewardRepository? = null

    @Autowired
    val weaponRepository: WeaponRepository? = null

    @Autowired
    val newWeaponRequestRepository: NewWeaponRequestRepository? = null


    @GetMapping("/main")
    fun main(model: Model, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)!!
        model.addAttribute("experiments", experimentRepository?.findAllByExaminatorIdOrderByStatusAsc(user.id!!))
        return "scientists/sci__main.html"
    }

    @GetMapping("/profile")
    fun sciProfile(model: Model, principal: Principal): String {
        val currentEmployee = employeeRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        val cashRewards = cashRewardRepository?.findAllByEmployee(currentEmployee!!)
        model.addAttribute("user", currentEmployee)
        model.addAttribute("form", NewPasswordForm())
        model.addAttribute("cashRewards", cashRewards)
        return "scientists/sci__profile.html"
    }

    @GetMapping("/main/articles")
    fun mainArticles(model: Model, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)!!
        model.addAttribute("articles", articleRepository?.findAllByScientistId(user.id!!))
        return "scientists/sci__main_articles.html"
    }

    @GetMapping("/main/requests")
    fun mainRequests(model: Model, principal: Principal): String {
        val experiments = experimentRepository?.findAllByStatusAndType(ExperimentStatus.PENDING, ExperimentType.MINOR)
        val validExperiments = ArrayList<Experiment>()

        val user = userRepository?.findByLogin(principal.name)
        val currentScientistLvl = employeeRepository?.findById(user?.id!!)?.get()?.level!!

        experiments?.forEach {
            if (currentScientistLvl > it.examinator!!.employee!!.level!!
                || (currentScientistLvl == 10)
            )
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
                    return "redirect:experiment"
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
        if (!possibleScientist.isPresent)
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

    @GetMapping("/report")
    fun report(@RequestParam id: String, model: Model, principal: Principal): String {
        val error = reportAccessError(id.toLong(), principal)
        if (error == null) {
            model.addAttribute("form", NewReportForm())
            model.addAttribute("weapons", weaponRepository?.findAll())
        } else
            model.addAttribute("error", error)

        return "scientists/sci__report.html"
    }

    @PostMapping("/report")
    @ResponseBody
    fun addReport(@ModelAttribute form: NewReportForm, principal: Principal): String {
        val error = reportAccessError(form.experimentId.toLong(), principal)
        if (error == null) {
            val experiment = experimentRepository?.findById(form.experimentId.toLong())!!
            when (form.isSynthesized.toInt()) {
                0 -> {
                    experimentRepository?.save(experiment.get().apply {
                        this.statusDate = LocalDateTime.now()
                        this.result = form.result
                        this.status = ExperimentStatus.FINISHED
                    })
                    return "Report submitted."
                }

                1 -> {
                    val possibleWeapon = weaponRepository?.findById(form.weaponId.toLong())!!
                    return if (!possibleWeapon.isPresent)
                        "Such weapon does not exist."
                    else {
                        val weapon = possibleWeapon.get()
                        weapon.quantity += form.weaponQuantity1.toLong()
                        weaponRepository?.save(weapon)
                        experimentRepository?.save(experiment.get().apply {
                            this.statusDate = LocalDateTime.now()
                            this.result = form.result
                            this.status = ExperimentStatus.FINISHED
                        })
                        "Report submitted and weapon added to the arsenal."
                    }
                }

                2 -> {
                    val user = userRepository?.findByLogin(principal.name)
                    val newRequest = Request().apply { this.status = RequestStatus.PENDING }
                    val weaponType = when (form.weaponType) {
                        "melee" -> WeaponType.MELEE
                        "pistol" -> WeaponType.PISTOL
                        "submachine_gun" -> WeaponType.SUBMACHINE_GUN
                        "assault_rifle" -> WeaponType.ASSAULT_RIFLE
                        "light_machine_gun" -> WeaponType.LIGHT_MACHINE_GUN
                        "sniper_rifle" -> WeaponType.SNIPER_RIFLE
                        "alien" -> WeaponType.ALIEN
                        else -> return "Such weapon type does not exist."
                    }

                    when {
                        form.weaponLevel.toInt() < 1 || form.weaponLevel.toInt() > 10 ->
                            return "Please enter weapon access level between 1-10."
                        form.weaponQuantity2.toLong() < 1 ->
                            return "Please enter a valid quantity of this weapon."
                        form.weaponPrice.toDouble() < 1 ->
                            return "Please enter a valid price for this weapon."
                    }

                    val newWeaponRequest = NewWeaponRequest(
                        form.weaponName, weaponType, form.weaponDescription,
                        form.weaponQuantity2.toLong(), form.weaponLevel.toInt(), form.weaponPrice.toDouble(), user
                    )

                    newWeaponRequestRepository?.save(newWeaponRequest.apply { this.request = newRequest })
                    experimentRepository?.save(experiment.get().apply {
                        this.statusDate = LocalDateTime.now()
                        this.result = form.result
                        this.status = ExperimentStatus.FINISHED
                    })
                    return "Report submitted. Await for supervisor's decision."

                }
            }

        } else
            return error
        return "An error occurred."
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
        if (checkedExperiment.type == ExperimentType.MAJOR)
            return "Major experiment requests are handled by administrators."
        if ((checkedExperiment.examinator!!.employee!!.level!! >= currentScientist!!.employee!!.level!!)
            && checkedExperiment.examinator!!.employee!!.level!! < 10
            && currentScientist.employee!!.level!! < 10
        )
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