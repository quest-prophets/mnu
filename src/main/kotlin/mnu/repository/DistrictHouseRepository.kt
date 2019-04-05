package mnu.repository

import mnu.model.DistrictHouse
import org.springframework.data.jpa.repository.*
import java.math.BigInteger
import java.util.*

interface DistrictHouseRepository : JpaRepository<DistrictHouse, Long> {
    @Query("select dt.id from district_houses dt;", nativeQuery = true)
    fun getAllIds(): List<Long>
}