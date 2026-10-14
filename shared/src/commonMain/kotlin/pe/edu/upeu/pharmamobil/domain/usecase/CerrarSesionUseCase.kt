package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class CerrarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = resultadoDe {
        authRepository.cerrarSesion()
    }
}
