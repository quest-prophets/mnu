package mnu.repository

import mnu.model.Experiment
import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentType
import org.springframework.data.jpa.repository.*

interface ExperimentRepository : JpaRepository<Experiment, Long> {
    fun findAllByType(type: ExperimentType) : List<Experiment>

    fun findAllByExaminatorId(examinatorId: Long) : List<Experiment>

    fun findAllByExaminatorAndType(examinator: ScientistEmployee, type: ExperimentType) : List<Experiment>

    fun findAllByAssistant(assistant: ScientistEmployee) : List<Experiment>
}