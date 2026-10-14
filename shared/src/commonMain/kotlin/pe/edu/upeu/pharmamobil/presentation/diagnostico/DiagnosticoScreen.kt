package pe.edu.upeu.pharmamobil.presentation.diagnostico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Pantalla de pruebas de la actividad autonoma (solo en depuracion). Sirve
 * para provocar los escenarios de token de la bitacora desde la propia app.
 */
@Composable
fun DiagnosticoScreen(
    viewModel: DiagnosticoViewModel,
    modifier: Modifier = Modifier
) {

    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refrescarEstado() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Herramienta de pruebas de la actividad autónoma. Solo aparece en una compilación de depuración.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Token en el almacén seguro", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = if (estado.estadoToken.guardado) "Guardado: sí" else "Guardado: no",
                    style = MaterialTheme.typography.bodyMedium
                )
                estado.estadoToken.expiraEn?.let { expira ->
                    Text(textoVencimiento(expira), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Button(
            onClick = viewModel::consultarInventario,
            enabled = !estado.ejecutando,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Consultar inventario (operación real)") }

        OutlinedButton(
            onClick = viewModel::alterarToken,
            enabled = !estado.ejecutando,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Alterar token guardado") }

        OutlinedButton(
            onClick = viewModel::prefijoEnMinuscula,
            enabled = !estado.ejecutando,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Enviar token con prefijo «bearer»") }

        estado.resultado?.let { resultado ->
            ResultadoCard(titulo = estado.ultimaPrueba.orEmpty(), texto = resultado)
        }
    }
}


@Composable
fun ResultadoCard(titulo: String, texto: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@OptIn(ExperimentalTime::class)
private fun textoVencimiento(expiraEn: Long): String {
    val restantes = expiraEn - Clock.System.now().epochSeconds
    return if (restantes > 0) {
        "Vence en ${restantes / 60} min ${restantes % 60} s (claim exp)"
    } else {
        "Venció hace ${-restantes / 60} min ${-restantes % 60} s (claim exp)"
    }
}
