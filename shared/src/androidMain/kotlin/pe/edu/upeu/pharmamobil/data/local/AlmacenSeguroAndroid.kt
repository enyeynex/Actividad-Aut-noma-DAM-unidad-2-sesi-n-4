package pe.edu.upeu.pharmamobil.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * EncryptedSharedPreferences cifra clave y valor con una llave que custodia
 * el Android Keystore: el token no queda en texto plano en el dispositivo.
 */
@Suppress("DEPRECATION") // security-crypto 1.1.0 marca la API como deprecada, pero sigue siendo la que pide la guia.
class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {

    private val llave = MasterKey.Builder(contexto)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        contexto, "sesion_segura", llave,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun guardarToken(token: String) =
        prefs.edit().putString(CLAVE, token).apply()

    override fun leerToken(): String? = prefs.getString(CLAVE, null)

    override fun borrarToken() =
        prefs.edit().remove(CLAVE).apply()

    private companion object { const val CLAVE = "jwt" }
}
