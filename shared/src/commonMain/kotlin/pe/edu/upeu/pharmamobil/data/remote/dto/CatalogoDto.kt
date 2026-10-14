package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.model.Producto

/** Envoltorio de las listas paginadas de PharmaSoft. */
@Serializable
data class PaginaDto<T>(
    val contenido: List<T>,
    val pagina: Int = 0,
    val totalElementos: Long = 0
)

@Serializable
data class ErrorDto(
    val status: Int? = null,
    val message: String? = null,
    val validationErrors: Map<String, String>? = null
)

@Serializable
data class CategoriaDto(val id: Long, val nombre: String)

@Serializable
data class CategoriaRequestDto(val nombre: String, val descripcion: String?, val estado: Boolean)

@Serializable
data class ProductoDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true
)

@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean,
    val categoriaId: Long
)

@Serializable
data class ClienteDto(
    val id: Long,
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null
)

@Serializable
data class ClienteRequestDto(
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val direccion: String?,
    val estado: Boolean
)

fun ProductoDto.aDominio(): Producto = Producto(id = id, nombre = nombre, precio = precio, stock = stock)

fun ClienteDto.aDominio(): Cliente = Cliente(
    id = id,
    nombre = "$nombres $apellidos".trim(),
    correo = email,
    telefono = telefono?.ifBlank { null },
    dni = dni
)
