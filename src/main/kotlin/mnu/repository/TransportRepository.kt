package mnu.repository

import mnu.model.Transport
import mnu.model.enums.TransportType
import org.springframework.data.jpa.repository.*

interface TransportRepository : JpaRepository<Transport, Long> {
    fun findAllByType(type: TransportType) : List<Transport>

    fun findAllByQuantityGreaterThanEqual(quantity: Long) : List<Transport>
}