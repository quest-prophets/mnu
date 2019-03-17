package mnu.repository

import mnu.model.DistrictHouse
import org.springframework.data.jpa.repository.*

interface DistrictHouseRepository : JpaRepository<DistrictHouse, Long> {
}