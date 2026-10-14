package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository

private val CORREO_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val TELEFONO_REGEX = Regex("^[0-9]{9}$")
private val DNI_REGEX = Regex("^[0-9]{8}$")


data class ErroresDeCliente(
    val dni: String? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val telefono: String? = null
) {

    val hayErrores: Boolean
        get() = dni != null || nombre != null || correo != null || telefono != null
}

class ClienteInvalidoException(
    val errores: ErroresDeCliente
) : IllegalArgumentException("Los datos del cliente no cumplen las reglas del negocio")

/**
 * Registra un cliente en la cartera. Concentra las reglas que antes vivian en
 * ClienteValidator, dentro de la capa de presentacion: que correo es valido y
 * que telefono se acepta lo decide el negocio, no la pantalla.
 */
class RegistrarClienteUseCase(
    private val clienteRepository: ClienteRepository
) {

    suspend operator fun invoke(
        dni: String,
        nombre: String,
        correo: String,
        telefono: String
    ): Result<Cliente> {

        val errores = ErroresDeCliente(
            dni = validarDni(dni),
            nombre = validarNombre(nombre),
            correo = validarCorreo(correo),
            telefono = validarTelefono(telefono)
        )

        if (errores.hayErrores) {
            return Result.failure(ClienteInvalidoException(errores))
        }

        return resultadoDe {
            clienteRepository.registrar(
                Cliente(
                    id = 0L,
                    nombre = nombre.trim(),
                    correo = correo.trim(),
                    telefono = telefono.trim().ifBlank { null },
                    dni = dni.trim()
                )
            )
        }
    }

    /** PharmaSoft identifica al cliente por un DNI de 8 digitos. */
    private fun validarDni(dni: String): String? {
        return when {
            dni.isBlank() -> "El DNI es obligatorio"
            !DNI_REGEX.matches(dni.trim()) -> "El DNI debe tener 8 dígitos"
            else -> null
        }
    }

    /** PharmaSoft guarda nombres y apellidos por separado: se piden ambos. */
    private fun validarNombre(nombre: String): String? {
        return when {
            nombre.isBlank() -> "El nombre es obligatorio"
            nombre.trim().split(Regex("\\s+")).size < 2 -> "Escribe nombre y apellido"
            else -> null
        }
    }

    private fun validarCorreo(correo: String): String? {
        return when {
            correo.isBlank() -> "El correo es obligatorio"
            !CORREO_REGEX.matches(correo.trim()) -> "El correo no tiene un formato válido"
            else -> null
        }
    }

    /** El telefono es opcional: solo se valida el formato si viene escrito. */
    private fun validarTelefono(telefono: String): String? {
        return if (telefono.isNotBlank() && !TELEFONO_REGEX.matches(telefono.trim())) {
            "El teléfono debe tener 9 dígitos"
        } else {
            null
        }
    }
}
