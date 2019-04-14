package mnu.repository

import mnu.model.Weapon
import mnu.model.enums.WeaponType
import org.springframework.data.jpa.repository.*

interface WeaponRepository : JpaRepository<Weapon, Long> {
    fun findAllByTypeOrderByIdAsc(type: WeaponType) : List<Weapon>?

    fun findAllByTypeAndQuantityGreaterThanOrderByIdAsc(type: WeaponType, quantity: Long) : List<Weapon>?

    fun findAllByQuantityGreaterThanEqual(quantity: Long) : List<Weapon>?

    fun findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(accessLevel: Int, quantity: Long) : List<Weapon>?

    fun findAllByNameIgnoreCaseContainingOrderByIdAsc (name: String) : List<Weapon>?
    fun findAllByNameIgnoreCaseContainingAndTypeOrderByIdAsc (name: String, type: WeaponType) : List<Weapon>?

    fun findAllByNameIgnoreCaseContainingAndQuantityGreaterThanOrderByIdAsc (name: String, quantity: Long) : List<Weapon>?
    fun findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThanOrderByIdAsc (name: String, type: WeaponType, quantity: Long) : List<Weapon>?

    fun findAllByTypeOrderByPriceAsc(type: WeaponType) : List<Weapon>
    fun findAllByTypeOrderByPriceDesc(type: WeaponType) : List<Weapon>

    fun findAllByTypeOrderByQuantityAsc(type: WeaponType) : List<Weapon>
    fun findAllByTypeOrderByQuantityDesc(type: WeaponType) : List<Weapon>

    fun findAllByTypeOrderByNameAsc(type: WeaponType) : List<Weapon>
    fun findAllByTypeOrderByNameDesc(type: WeaponType) : List<Weapon>
}