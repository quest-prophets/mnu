package mnu.form

data class NewExperimentForm(
    val title: String = "",
    val type: String = "",
    val assistantId: Long? = null,
    val description: String = ""
)