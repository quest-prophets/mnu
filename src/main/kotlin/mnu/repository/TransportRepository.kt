package mnu.repository

import mnu.model.Transport
import mnu.model.enums.TransportType
import org.springframework.data.jpa.repository.*

interface TransportRepository : JpaRepository<Transport, Long> {
    fun findAllByTypeOrderByIdAsc(type: TransportType) : List<Transport>?

    fun findAllByTypeAndQuantityGreaterThanOrderByIdAsc(type: TransportType, quantity: Long) : List<Transport>?

    fun findAllByQuantityGreaterThanEqual(quantity: Long) : List<Transport>?

    fun findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(accessLevel: Int, quantity: Long) : List<Transport>?

    fun findAllByNameIgnoreCaseContainingOrderByIdAsc (name: String) : List<Transport>?
    fun findAllByNameIgnoreCaseContainingAndTypeOrderByIdAsc (name: String, type: TransportType) : List<Transport>?

    fun findAllByNameIgnoreCaseContainingAndQuantityGreaterThanOrderByIdAsc (name: String, quantity: Long) : List<Transport>?
    fun findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThanOrderByIdAsc (name: String, type: TransportType, quantity: Long) : List<Transport>?

    fun findAllByTypeOrderByPriceAsc(type: TransportType) : List<Transport>
    fun findAllByTypeOrderByPriceDesc(type: TransportType) : List<Transport>

    fun findAllByTypeOrderByQuantityAsc(type: TransportType) : List<Transport>
    fun findAllByTypeOrderByQuantityDesc(type: TransportType) : List<Transport>

    fun findAllByTypeOrderByNameAsc(type: TransportType) : List<Transport>
    fun findAllByTypeOrderByNameDesc(type: TransportType) : List<Transport>
}