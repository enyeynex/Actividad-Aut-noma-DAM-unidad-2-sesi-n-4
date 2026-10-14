package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

data class ErroresDeLogin(
    val usuario: String? = null,
    val clave: String? = null
) {
    val hayErrores: Boolean
        get() = usuario != null || clave != null
}

class LoginInvalidoException(
    val errores: ErroresDeLogin
) : IllegalArgumentException("Completa el usuario y la contraseña")

/** El servidor exige ambos campos: se validan antes de gastar una peticion. */
class IniciarSesionUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(usuario: String, clave: String): Result<Sesion> {

        val errores = ErroresDeLogin(
            usuario = if (usuario.isBlank()) "El usuario es obligatorio" else null,
            clave = if (clave.isBlank()) "La contraseña es obligatoria" else null
        )

        if (errores.hayErrores) {
            return Result.failure(LoginInvalidoException(errores))
        }

        return resultadoDe {
            authRepository.iniciarSesion(Credenciales(usuario.trim(), clave))
        }
    }
}
