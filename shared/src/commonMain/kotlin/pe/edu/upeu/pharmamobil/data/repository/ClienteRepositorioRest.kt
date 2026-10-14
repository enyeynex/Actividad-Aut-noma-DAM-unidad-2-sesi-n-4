package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobil.data.remote.dto.ClienteDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ClienteRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaDto
import pe.edu.upeu.pharmamobil.data.remote.dto.aDominio
import pe.edu.upeu.pharmamobil.data.remote.llamadaApi
import pe.edu.upeu.pharmamobil.domain.model.Cliente
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.sesion.AvisosDeSesion

class ClienteRepositorioRest(
    private val cliente: HttpClient,
    private val avisos: AvisosDeSesion
) : ClienteRepository {

    override suspend fun listar(): List<Cliente> = llamadaApi(avisos) {
        cliente.get("clientes") {
            parameter("pagina", 0)
            parameter("tamanio", 100)
        }.body<PaginaDto<ClienteDto>>().contenido.map { it.aDominio() }
    }

    /**
     * La app guarda el nombre completo en un solo campo y PharmaSoft lo pide
     * separado: la primera palabra va a nombres y el resto a apellidos.
     */
    override suspend fun registrar(cliente: Cliente): Cliente = llamadaApi(avisos) {
        val partes = cliente.nombre.trim().split(Regex("\\s+"), limit = 2)
        this.cliente.post("clientes") {
            setBody(
                ClienteRequestDto(
                    dni = cliente.dni,
                    nombres = partes.first(),
                    apellidos = partes.getOrElse(1) { "" },
                    email = cliente.correo,
                    telefono = cliente.telefono,
                    direccion = null,
                    estado = true
                )
            )
        }.body<ClienteDto>().aDominio()
    }
}
