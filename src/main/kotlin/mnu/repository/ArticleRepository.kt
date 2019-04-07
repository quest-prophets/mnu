package mnu.repository

import mnu.model.Article
import mnu.model.employee.ScientistEmployee
import org.springframework.data.jpa.repository.*
import java.time.LocalDateTime

interface ArticleRepository : JpaRepository<Article, Long> {
    fun findAllByScientistId(scientistId: Long) : List<Article>

    fun findAllByScientist(scientist: ScientistEmployee) : List<Article>

    fun findAllByCreationDate(creationDate: LocalDateTime) : List<Article>
}