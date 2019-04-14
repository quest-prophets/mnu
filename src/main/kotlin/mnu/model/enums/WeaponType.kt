package mnu.model.enums

enum class WeaponType {
    MELEE,
    PISTOL,
    SUBMACHINE_GUN,
    ASSAULT_RIFLE,
    LIGHT_MACHINE_GUN,
    SNIPER_RIFLE,
    ALIEN;

    companion object {
        fun fromClient(type: String?): WeaponType? = when (type) {
            "melee" -> WeaponType.MELEE
            "pistol" -> WeaponType.PISTOL
            "submachine_gun" -> WeaponType.SUBMACHINE_GUN
            "assault_rifle" -> WeaponType.ASSAULT_RIFLE
            "light_machine_gun" -> WeaponType.LIGHT_MACHINE_GUN
            "sniper_rifle" -> WeaponType.SNIPER_RIFLE
            "alien" -> WeaponType.ALIEN
            else -> null
        }
    }
}