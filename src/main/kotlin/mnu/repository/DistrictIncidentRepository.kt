package mnu.repository

import mnu.model.DistrictHouse
import mnu.model.DistrictIncident
import org.springframework.data.jpa.repository.*
import java.time.LocalDateTime

interface DistrictIncidentRepository : JpaRepository<DistrictIncident, Long> {
    fun findAllByLevelToAndDangerLevelGreaterThan(level1: Int, dangerLevel: Short) : List<DistrictIncident>?

    fun findAllByHouse(house: DistrictHouse): List<DistrictIncident>

    fun findAllByHouseAndDangerLevel(house: DistrictHouse, dangerLevel: Short): List<DistrictIncident>

    fun findAllByOrderByDangerLevelDesc() : List<DistrictIncident>?

    fun findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual
                (availablePlaces: Long, level1: Int, level2: Int) : List<DistrictIncident>?

    fun findAllByAppearanceTimeAfterAndAppearanceTimeBefore
                (appearanceTime1: LocalDateTime, appearanceTime2: LocalDateTime) : List<DistrictIncident>?
}