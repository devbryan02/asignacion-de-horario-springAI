package elp.edu.pe.horario.application.usecase.docente;

import elp.edu.pe.horario.domain.model.Docente;
import elp.edu.pe.horario.domain.repository.AsignacionHorarioRepository;
import elp.edu.pe.horario.domain.repository.DocenteRepository;
import elp.edu.pe.horario.domain.repository.RestriccionDocenteRepository;
import elp.edu.pe.horario.infrastructure.persistence.repository.DocenteRepositoryImpl;
import elp.edu.pe.horario.shared.exception.BadRequest;
import elp.edu.pe.horario.shared.exception.CustomException;
import elp.edu.pe.horario.shared.exception.DeleteException;
import elp.edu.pe.horario.shared.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EliminarDocenteUseCase {

    private static final Logger log = LoggerFactory.getLogger(EliminarDocenteUseCase.class);
    private final DocenteRepository docenteRepository;
    private final AsignacionHorarioRepository asignacionHorarioRepository;
    private final RestriccionDocenteRepository restriccionDocenteRepository;

    public EliminarDocenteUseCase(DocenteRepositoryImpl docenteRepository,
                                   AsignacionHorarioRepository asignacionHorarioRepository,
                                   RestriccionDocenteRepository restriccionDocenteRepository) {
        this.docenteRepository = docenteRepository;
        this.asignacionHorarioRepository = asignacionHorarioRepository;
        this.restriccionDocenteRepository = restriccionDocenteRepository;
    }

    @Transactional
    public void ejecutar(UUID id) {
        try{
            if(id == null) throw new BadRequest("ID no puede ser nulo");

            Docente docente = docenteRepository
                    .findById(id)
                    .orElseThrow(() -> new NotFoundException("Docente no encontrado"));

            // Verificar si el docente tiene asignaciones de horario y restricciones asociadas
            validarReferencias(id);

            docenteRepository.deleteById(id);

            log.info("Docente eliminado: {}", docente);
        }catch (Exception e){
            log.error("Error al eliminar el docente", e);

            if (e instanceof CustomException || e instanceof BadRequest || e instanceof NotFoundException) {
                throw e;
            }

            throw new DeleteException("Error al eliminar el docente");
        }
    }

    void validarReferencias(UUID docenteId) {
        if (this.asignacionHorarioRepository.existeReferenciaDocente(docenteId)) {
            throw new CustomException("El docente tiene asignaciones asociadas");
        }
        if (this.restriccionDocenteRepository.existsByDocenteId(docenteId)) {
            throw new CustomException("El docente tiene restricciones asociadas");
        }
    }
}
