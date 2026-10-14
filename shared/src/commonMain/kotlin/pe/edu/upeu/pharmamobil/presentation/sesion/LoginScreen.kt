package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField

/**
 * Pantalla de inicio de sesion. No conoce el token ni el servidor: solo
 * muestra el estado y dispara las acciones del SesionViewModel.
 */
@Composable
fun LoginScreen(
    estado: SesionUiState,
    onUsuarioChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onIngresar: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Column(
                modifier = Modifier.widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(40.dp)
                    )
                }

                Text(
                    text = "PharmaMobil",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Inicia sesión con tu usuario de PharmaSoft",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                estado.aviso?.let { aviso ->
                    Aviso(aviso)
                }

                Card(modifier = Modifier.fillMaxWidth()) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        ValidatedTextField(
                            value = estado.usuario,
                            onValueChange = onUsuarioChange,
                            label = "Usuario",
                            error = null,
                            leadingIcon = Icons.Default.Person,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ValidatedTextField(
                            value = estado.clave,
                            onValueChange = onClaveChange,
                            label = "Contraseña",
                            error = estado.error,
                            leadingIcon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = onIngresar,
                            enabled = !estado.cargando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (estado.cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Ingresar")
                            }
                        }
                    }
                }

                Text(
                    text = "Usuarios de prueba: admin · vendedor · almacen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun Aviso(texto: String) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
