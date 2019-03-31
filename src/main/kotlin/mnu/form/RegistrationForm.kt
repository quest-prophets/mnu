package mnu.form

data class RegistrationForm(
    val username: String = "",
    var password: String = "",
    val email: String = "",
    val name: String = "",
    val type: String = ""
)