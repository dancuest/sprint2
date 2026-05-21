package com.example.animedev20.ui.theme.ux

data class FieldValidation(
    val isValid: Boolean,
    val message: String? = null
)

object AnimeDevFormValidators {

    private val emailRegex = Regex(
        pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    fun validateEmail(email: String): FieldValidation {
        val cleanEmail = email.trim()

        return when {
            cleanEmail.isBlank() -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.requiredEmail
            )

            !emailRegex.matches(cleanEmail) -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.invalidEmail
            )

            else -> FieldValidation(isValid = true)
        }
    }

    fun validatePassword(password: String): FieldValidation {
        return when {
            password.isBlank() -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.requiredPassword
            )

            password.length < 6 -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.shortPassword
            )

            else -> FieldValidation(isValid = true)
        }
    }

    fun validateNewPassword(password: String): FieldValidation {
        return when {
            password.isBlank() -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.requiredNewPassword
            )

            password.length < 6 -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.shortPassword
            )

            else -> FieldValidation(isValid = true)
        }
    }

    fun validateConfirmPassword(
        password: String,
        confirmPassword: String
    ): FieldValidation {
        return when {
            confirmPassword.isBlank() -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.passwordsDoNotMatch
            )

            password != confirmPassword -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.passwordsDoNotMatch
            )

            else -> FieldValidation(isValid = true)
        }
    }

    fun validateDisplayName(displayName: String): FieldValidation {
        val cleanName = displayName.trim()

        return when {
            cleanName.isBlank() -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.requiredDisplayName
            )

            cleanName.length < 2 -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.shortDisplayName
            )

            else -> FieldValidation(isValid = true)
        }
    }

    fun validateResetToken(token: String): FieldValidation {
        val cleanToken = token.trim()

        return when {
            cleanToken.isBlank() -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.requiredToken
            )

            cleanToken.length < 4 -> FieldValidation(
                isValid = false,
                message = AnimeDevCopy.Validation.shortToken
            )

            else -> FieldValidation(isValid = true)
        }
    }

    fun loginError(
        email: String,
        password: String
    ): String? {
        val emailValidation = validateEmail(email)
        if (!emailValidation.isValid) return emailValidation.message

        val passwordValidation = validatePassword(password)
        if (!passwordValidation.isValid) return passwordValidation.message

        return null
    }

    fun registerError(
        displayName: String,
        email: String,
        password: String
    ): String? {
        val nameValidation = validateDisplayName(displayName)
        if (!nameValidation.isValid) return nameValidation.message

        val emailValidation = validateEmail(email)
        if (!emailValidation.isValid) return emailValidation.message

        val passwordValidation = validatePassword(password)
        if (!passwordValidation.isValid) return passwordValidation.message

        return null
    }

    fun forgotPasswordError(email: String): String? {
        val emailValidation = validateEmail(email)
        return if (emailValidation.isValid) null else emailValidation.message
    }

    fun resetPasswordError(
        email: String,
        token: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        val emailValidation = validateEmail(email)
        if (!emailValidation.isValid) return emailValidation.message

        val tokenValidation = validateResetToken(token)
        if (!tokenValidation.isValid) return tokenValidation.message

        val passwordValidation = validateNewPassword(newPassword)
        if (!passwordValidation.isValid) return passwordValidation.message

        val confirmValidation = validateConfirmPassword(
            password = newPassword,
            confirmPassword = confirmPassword
        )
        if (!confirmValidation.isValid) return confirmValidation.message

        return null
    }

    fun canSubmitLogin(
        email: String,
        password: String,
        isLoading: Boolean
    ): Boolean {
        return !isLoading &&
                validateEmail(email).isValid &&
                validatePassword(password).isValid
    }

    fun canSubmitRegister(
        displayName: String,
        email: String,
        password: String,
        isLoading: Boolean
    ): Boolean {
        return !isLoading &&
                validateDisplayName(displayName).isValid &&
                validateEmail(email).isValid &&
                validatePassword(password).isValid
    }

    fun canSubmitForgotPassword(
        email: String,
        isLoading: Boolean
    ): Boolean {
        return !isLoading && validateEmail(email).isValid
    }

    fun canSubmitResetPassword(
        email: String,
        token: String,
        newPassword: String,
        confirmPassword: String,
        isLoading: Boolean
    ): Boolean {
        return !isLoading &&
                resetPasswordError(
                    email = email,
                    token = token,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                ) == null
    }
}