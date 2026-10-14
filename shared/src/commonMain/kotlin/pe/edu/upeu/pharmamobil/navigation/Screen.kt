package pe.edu.upeu.pharmamobil.navigation

sealed class Screen {
    data object Inicio : Screen()

    data object Productos : Screen()

    data object Clientes : Screen()

    data object Pedidos : Screen()

    /** Solo en depuracion: pruebas de la actividad autonoma de la sesion 10. */
    data object Diagnostico : Screen()
}