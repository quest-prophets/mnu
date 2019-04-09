package mnu.repository

import mnu.model.Prawn
import mnu.model.Vacancy
import mnu.model.employee.ManagerEmployee
import org.springframework.data.jpa.repository.*

interface PrawnRepository : JpaRepository<Prawn, Long> {
    fun findAllByManager(manager: ManagerEmployee): List<Prawn>

    fun findByUserId(id: Long): Prawn

    fun findAllByKarmaGreaterThanEqual(karma: Long): List<Prawn>

    fun findAllByKarmaLessThanEqual(karma: Long): List<Prawn>

    fun findAllByJob(job: Vacancy): List<Prawn>

}