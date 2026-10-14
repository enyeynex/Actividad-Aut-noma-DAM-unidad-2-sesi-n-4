package pe.edu.upeu.pharmamobil.domain.model

data class Cliente(
    val id: Long,
    val nombre:String,
    val correo:String,
    val telefono: String?,
    /** PharmaSoft identifica al cliente por su DNI (8 digitos). */
    val dni: String = ""
){

    init {
        require(nombre.isNotBlank()) {
            "El nombre del cliente no puede estar vacio"
        }
        require(correo.isNotBlank()) {
            "El correo del cliente no puede estar vacio"
        }
        require(telefono == null || telefono.isNotBlank()) {
            "El telefono es opcional, pero no puede ser una cadena vacia"
        }
    }
}
