package mnu.repository

import mnu.model.Weapon
import mnu.model.enums.WeaponType
import org.springframework.data.jpa.repository.*

interface WeaponRepository : JpaRepository<Weapon, Long> {
    fun findAllByType(type: WeaponType) : List<Weapon>

    fun findAllByQuantityGreaterThanEqual(quantity: Long) : List<Weapon>
}