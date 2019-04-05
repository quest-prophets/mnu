package mnu.form

data class NewExperimentForm(
    val title: String = "",
    val type: String = "",
    val assistantId: Long? = null,
    val date: String = "",
    val description: String? = ""
)