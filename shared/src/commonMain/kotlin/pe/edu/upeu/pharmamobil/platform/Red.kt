package pe.edu.upeu.pharmamobil.platform

/**
 * URL base de PharmaSoft. Comparte firma en las dos plataformas y solo cambia
 * el valor, por eso es un expect: el emulador de Android llega al equipo por
 * 10.0.2.2 y el simulador de iOS por localhost.
 */
expect val URL_BASE: String
