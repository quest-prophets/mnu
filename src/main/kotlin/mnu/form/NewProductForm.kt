package mnu.form

data class NewProductForm(
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val price: String = "",
    val accessLvl: String = ""
)