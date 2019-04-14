package mnu.model.enums

enum class TransportType {
    LAND,
    AIR;

    companion object {
        fun fromClient(type: String?): TransportType? = when (type) {
            "land" -> TransportType.LAND
            "air" -> TransportType.AIR
            else -> null
        }
    }
}