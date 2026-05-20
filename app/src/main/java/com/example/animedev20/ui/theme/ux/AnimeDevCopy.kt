package com.example.animedev20.ui.theme.ux

object AnimeDevCopy {

    object Actions {
        const val goBack = "Volver a la pantalla anterior"
        const val login = "Iniciar sesión"
        const val register = "Crear cuenta"
        const val send = "Enviar"
        const val cancel = "Cancelar"
        const val continueAction = "Continuar"
        const val tryAgain = "Volver a intentar"
        const val goToLogin = "Ir a iniciar sesión"
        const val goToRegister = "Crear una cuenta"
        const val saveChanges = "Guardar cambios"
        const val continueAsGuest = "Explorar como invitado"
    }

    object Auth {
        const val welcomeTitle = "AnimeDev"
        const val welcomeSubtitle = "Descubre animes, guarda favoritos y pon a prueba tu nivel otaku."
        const val welcomeHelper =
            "Crea una cuenta para guardar tu progreso o entra como invitado para explorar primero."

        const val guestHelper =
            "Como invitado puedes explorar la app. Para guardar favoritos y progreso, crea una cuenta después."

        const val loginTitle = "Bienvenido de nuevo"
        const val loginSubtitle = "Inicia sesión para continuar tu experiencia anime."

        const val registerTitle = "Crear cuenta"
        const val registerSubtitle = "Únete a AnimeDev y guarda tus animes, trivias y progreso."

        const val forgotPasswordTitle = "Recuperar contraseña"
        const val forgotPasswordSubtitle =
            "Escribe el correo de tu cuenta. Te mostraremos un token temporal para restablecer tu contraseña."

        const val resetPasswordTitle = "Nueva contraseña"
        const val resetPasswordSubtitle =
            "Ingresa tu correo, el token de recuperación y una nueva contraseña segura."

        const val emailLabel = "Correo electrónico"
        const val emailPlaceholder = "nombre@correo.com"

        const val passwordLabel = "Contraseña"
        const val newPasswordLabel = "Nueva contraseña"
        const val confirmPasswordLabel = "Confirmar contraseña"

        const val displayNameLabel = "Nombre"
        const val displayNamePlaceholder = "Ejemplo: Daniel"

        const val tokenLabel = "Token de recuperación"
        const val tokenPlaceholder = "Pega aquí el token generado"

        const val forgotPasswordAction = "¿Olvidaste tu contraseña?"
        const val generateToken = "Generar token"
        const val goToResetPassword = "Restablecer contraseña"
        const val backToLogin = "Volver al inicio de sesión"

        const val noAccount = "¿No tienes cuenta?"
        const val alreadyHaveAccount = "¿Ya tienes cuenta?"

        const val passwordUpdatedTitle = "¡Contraseña actualizada!"
        const val passwordUpdatedMessage = "Ya puedes iniciar sesión con tu nueva contraseña."

        const val demoTokenLabel = "Token temporal"
        const val demoTokenExpirationLabel = "Vence"
    }

    object Validation {
        const val requiredEmail = "Escribe tu correo electrónico."
        const val invalidEmail = "El correo debe tener un formato válido, por ejemplo nombre@correo.com."

        const val requiredPassword = "Escribe tu contraseña."
        const val shortPassword = "La contraseña debe tener al menos 6 caracteres."

        const val requiredDisplayName = "Escribe el nombre que quieres mostrar en tu perfil."
        const val shortDisplayName = "El nombre debe tener al menos 2 caracteres."

        const val requiredToken = "Ingresa el token de recuperación."
        const val shortToken = "El token parece incompleto. Revisa que lo hayas copiado completo."

        const val requiredNewPassword = "Escribe una nueva contraseña."
        const val passwordsDoNotMatch = "Las contraseñas no coinciden."
    }

    object Errors {
        const val generic = "Algo salió mal. Inténtalo de nuevo."
        const val authGeneric = "No pudimos completar la autenticación. Revisa tus datos e inténtalo de nuevo."
        const val network = "No pudimos conectar con AnimeDev. Revisa tu conexión a internet."
        const val server = "AnimeDev está tardando más de lo normal. Inténtalo de nuevo en unos minutos."
    }

    object Success {
        const val tokenGenerated =
            "Token generado correctamente. Úsalo para crear una nueva contraseña."
        const val accountCreated =
            "Cuenta creada correctamente. Ahora personaliza tu experiencia."
        const val loginCompleted =
            "Sesión iniciada correctamente."
    }

    object Accessibility {
        const val appLogo = "Logo de AnimeDev"
        const val showPassword = "Mostrar contraseña"
        const val hidePassword = "Ocultar contraseña"
        const val emailIcon = "Campo de correo electrónico"
        const val passwordIcon = "Campo de contraseña"
        const val userIcon = "Campo de nombre de usuario"
        const val tokenIcon = "Campo de token de recuperación"
    }
}