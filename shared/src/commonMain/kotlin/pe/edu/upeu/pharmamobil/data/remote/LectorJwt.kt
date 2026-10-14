package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Claims que PharmaSoft pone en el token: sub, authorities, iat y exp.
 * La firma garantiza que nadie altero el contenido, pero no lo oculta: por
 * eso la app puede leer la carga util sin conocer la clave del servidor.
 */
data class ClaimsJwt(
    val usuario: String,
    val autoridades: List<String>,
    val expiraEn: Long
) {
    fun expirado(ahoraEnSegundos: Long): Boolean = expiraEn <= ahoraEnSegundos
}

@OptIn(ExperimentalEncodingApi::class)
fun leerClaims(token: String): ClaimsJwt? = try {
    val carga = token.split(".")[1]
    val json = Base64.UrlSafe
        .withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
        .decode(carga)
        .decodeToString()
    val objeto = JsonPharma.parseToJsonElement(json).jsonObject
    ClaimsJwt(
        usuario = objeto.getValue("sub").jsonPrimitive.content,
        autoridades = objeto["authorities"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty(),
        expiraEn = objeto.getValue("exp").jsonPrimitive.longOrNull ?: 0L
    )
} catch (fallo: Exception) {
    null
}
