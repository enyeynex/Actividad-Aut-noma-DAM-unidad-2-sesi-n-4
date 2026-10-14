package pe.edu.upeu.pharmamobil.data.remote

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Arma un token con la misma forma que los de PharmaSoft. La firma es falsa:
 * la app nunca la verifica, eso lo hace el servidor.
 */
@OptIn(ExperimentalEncodingApi::class)
fun jwtDePrueba(usuario: String, autoridades: List<String>, expiraEn: Long): String {
    val codificar = { texto: String ->
        Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(texto.encodeToByteArray())
    }
    val cabecera = codificar("""{"alg":"HS512"}""")
    val lista = autoridades.joinToString(",") { "\"$it\"" }
    val carga = codificar("""{"sub":"$usuario","authorities":[$lista],"iat":0,"exp":$expiraEn}""")
    return "$cabecera.$carga.firma"
}
