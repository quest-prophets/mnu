package mnu.repository

import mnu.model.Vacancy
import org.springframework.data.jpa.repository.*

interface VacancyRepository : JpaRepository<Vacancy, Long> {
    fun findAllByRequiredKarmaLessThanEqual(karma: Long) : List<Vacancy>

    fun findAllByVacantPlacesGreaterThanEqual(places: Long) : List<Vacancy>

    fun findAllByOrderBySalaryAsc() : List<Vacancy>

    fun findAllByOrderBySalaryDesc() : List<Vacancy>
}