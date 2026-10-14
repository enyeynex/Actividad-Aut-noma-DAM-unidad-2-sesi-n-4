package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobil.domain.model.Sesion

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val username: String,
    val nombreCompleto: String,
    val roles: List<String>,
    val permisos: List<String>
)

/** Respuesta de GET /auth/me: se usa para reconstruir la sesion guardada. */
@Serializable
data class UsuarioDto(
    val username: String,
    val empleadoNombre: String? = null,
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)

fun LoginResponseDto.aSesion(): Sesion = Sesion(
    token = token,
    usuario = username,
    nombreCompleto = nombreCompleto,
    permisos = permisos.toSet()
)

fun UsuarioDto.aSesion(token: String): Sesion = Sesion(
    token = token,
    usuario = username,
    nombreCompleto = empleadoNombre ?: username,
    permisos = permisos.toSet()
)
