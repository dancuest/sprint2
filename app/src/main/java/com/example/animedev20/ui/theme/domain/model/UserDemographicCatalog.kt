package com.example.animedev20.ui.theme.domain.model

data class CodedOption(
    val code: Int,
    val label: String
)

object UserDemographicCatalog {
    const val UNSPECIFIED_CODE = 0

    val ageRanges: List<CodedOption> = listOf(
        CodedOption(UNSPECIFIED_CODE, "No especificado"),
        CodedOption(1, "13-17"),
        CodedOption(2, "18-24"),
        CodedOption(3, "25-34"),
        CodedOption(4, "35-44"),
        CodedOption(5, "45+")
    )

    val genders: List<CodedOption> = listOf(
        CodedOption(UNSPECIFIED_CODE, "No especificado"),
        CodedOption(1, "Femenino"),
        CodedOption(2, "Masculino"),
        CodedOption(3, "No binario"),
        CodedOption(4, "Prefiero no decir")
    )

    val regions: List<CodedOption> = listOf(
        CodedOption(UNSPECIFIED_CODE, "No especificado"),
        CodedOption(1, "Latinoamérica"),
        CodedOption(2, "Norteamérica"),
        CodedOption(3, "Europa"),
        CodedOption(4, "Asia"),
        CodedOption(5, "África"),
        CodedOption(6, "Oceanía")
    )
}
