package mnu.repository

import mnu.model.Transport
import mnu.model.enums.TransportType
import org.springframework.data.jpa.repository.*

interface TransportRepository : JpaRepository<Transport, Long> {
    fun findAllByType(type: TransportType) : List<Transport>

    fun findAllByRequiredAccessLvlLessThanEqual(accessLevel: Int) : List<Transport>

    fun findAllByQuantityGreaterThanEqual(quantity: Long) : List<Transport>?

    fun findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanEqual(accessLevel: Int, quantity: Long) : List<Transport>?

    fun findAllByTypeOrderByPriceAsc(type: TransportType) : List<Transport>
    fun findAllByTypeOrderByPriceDesc(type: TransportType) : List<Transport>

    fun findAllByTypeOrderByQuantityAsc(type: TransportType) : List<Transport>
    fun findAllByTypeOrderByQuantityDesc(type: TransportType) : List<Transport>

    fun findAllByTypeOrderByNameAsc(type: TransportType) : List<Transport>
    fun findAllByTypeOrderByNameDesc(type: TransportType) : List<Transport>
}