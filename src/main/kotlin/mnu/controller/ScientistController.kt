package mnu.controller

import mnu.form.NewArticleForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/sci")
class ScientistController {
    @GetMapping("/main")
    fun main() = "scientists/sci__main.html"

    @GetMapping("/experiment")
    fun experiment() = "scientists/sci__new-experiment.html"

    @GetMapping("/article")
    fun article(model: Model): String {
        model.addAttribute("form", NewArticleForm())
        return "scientists/sci__new-article.html"
    }
}