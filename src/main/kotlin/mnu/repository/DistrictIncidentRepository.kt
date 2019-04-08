package mnu.repository

import mnu.model.DistrictHouse
import mnu.model.DistrictIncident
import org.springframework.data.jpa.repository.*

interface DistrictIncidentRepository : JpaRepository<DistrictIncident, Long> {
    fun findAllByDangerLevelGreaterThan(dangerLevel: Short) : List<DistrictIncident>

    fun findAllByHouse(house: DistrictHouse): List<DistrictIncident>

    fun findAllByHouseAndDangerLevel(house: DistrictHouse, dangerLevel: Short): List<DistrictIncident>

    fun findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual
                (availablePlaces: Long, level1: Int, level2: Int) : List<DistrictIncident>

}