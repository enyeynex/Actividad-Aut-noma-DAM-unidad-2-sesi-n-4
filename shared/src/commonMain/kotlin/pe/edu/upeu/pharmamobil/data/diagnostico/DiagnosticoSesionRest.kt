package pe.edu.upeu.pharmamobil.data.diagnostico

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.remote.JsonPharma
import pe.edu.upeu.pharmamobil.data.remote.dto.ErrorDto
import pe.edu.upeu.pharmamobil.data.remote.leerClaims
import pe.edu.upeu.pharmamobil.domain.sesion.DiagnosticoSesion
import pe.edu.upeu.pharmamobil.domain.sesion.EstadoToken
import pe.edu.upeu.pharmamobil.domain.sesion.ResultadoDiagnostico
import pe.edu.upeu.pharmamobil.platform.URL_BASE
import kotlin.coroutines.cancellation.CancellationException

class DiagnosticoSesionRest(
    private val cliente: HttpClient,
    private val motor: HttpClientEngine,
    private val almacen: AlmacenSeguro
) : DiagnosticoSesion {

    override fun estadoToken(): EstadoToken {
        val token = almacen.leerToken() ?: return EstadoToken(guardado = false)
        return EstadoToken(guardado = true, expiraEn = leerClaims(token)?.expiraEn)
    }

    /**
     * Se cambia el primer caracter de la firma (la parte despues del ultimo
     * punto): el token conserva su forma pero el servidor ya no puede
     * validarlo. clearToken() obliga al plugin Auth a leer el valor alterado.
     */
    override fun alterarTokenGuardado(): Boolean {
        val token = almacen.leerToken() ?: return false
        val inicioFirma = token.lastIndexOf('.') + 1
        if (inicioFirma <= 0 || inicioFirma >= token.length) return false

        val original = token[inicioFirma]
        val reemplazo = if (original == 'A') 'B' else 'A'
        almacen.guardarToken(token.substring(0, inicioFirma) + reemplazo + token.substring(inicioFirma + 1))
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
        return true
    }

    override suspend fun peticionConElClienteActual(): ResultadoDiagnostico = try {
        val respuesta = cliente.get("productos") { parameter("tamanio", 1) }
        resultadoDe(respuesta)
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (fallo: ResponseException) {
        resultadoDe(fallo.response)
    } catch (fallo: IOException) {
        ResultadoDiagnostico(null, "Sin conexión con PharmaSoft")
    }

    /**
     * El cliente de la app siempre escribe «Bearer» por el plugin Auth. Para
     * provocar el escenario se usa un cliente aparte, sin plugins, que solo
     * existe en esta prueba.
     */
    override suspend fun peticionConPrefijoEnMinuscula(): ResultadoDiagnostico {
        val token = almacen.leerToken()
            ?: return ResultadoDiagnostico(null, "No hay token guardado: inicia sesión primero")

        val clienteDePrueba = HttpClient(motor) {
            install(DefaultRequest) { url(URL_BASE) }
        }
        return try {
            val respuesta = clienteDePrueba.get("productos") {
                parameter("tamanio", 1)
                header(HttpHeaders.Authorization, "bearer $token")
            }
            resultadoDe(respuesta)
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (fallo: IOException) {
            ResultadoDiagnostico(null, "Sin conexión con PharmaSoft")
        } finally {
            clienteDePrueba.close()
        }
    }

    private suspend fun resultadoDe(respuesta: HttpResponse): ResultadoDiagnostico {
        val conCabecera = respuesta.call.request.headers[HttpHeaders.Authorization] != null
        val mensaje = if (respuesta.status.isSuccess()) {
            "La operación se completó"
        } else {
            try {
                JsonPharma.decodeFromString(ErrorDto.serializer(), respuesta.bodyAsText()).message
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (otro: Exception) {
                null
            } ?: respuesta.status.description
        }
        return ResultadoDiagnostico(respuesta.status.value, mensaje, conCabecera)
    }
}
