package org.jcr.entidades;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Builder
@AllArgsConstructor
@Getter
@ToString(exclude = {"observaciones"})
public class Cita implements Serializable {
    @NonNull
    private final Paciente paciente;
    @NonNull
    private final Medico medico;
    @NonNull
    private final Sala sala;
    @NonNull
    private final LocalDateTime fechaHora;
    @NonNull
    private final BigDecimal costo;
    @Builder.Default
    @NonNull
    @Setter
    private EstadoCita estado = EstadoCita.PROGRAMADA;
    @Builder.Default
    private String observaciones = "";

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones != null ? observaciones : "";
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                paciente.getDni(),
                medico.getDni(),
                sala.getNumero(),
                fechaHora.toString(),
                costo.toString(),
                estado.name(),
                observaciones.replaceAll(",", ";"));
    }

    public static Cita fromCsvString(String csvString,
                                     Map<String, Paciente> pacientes,
                                     Map<String, Medico> medicos,
                                     Map<String, Sala> salas) throws CitaException {
        String[] values = csvString.split(",");
        if (values.length != 7) {
            throw new CitaException("Formato de CSV inválido para Cita: " + csvString);
        }

        String dniPaciente = values[0];
        String dniMedico = values[1];
        String numeroSala = values[2];
        LocalDateTime fechaHora = LocalDateTime.parse(values[3]);
        BigDecimal costo = new BigDecimal(values[4]);
        EstadoCita estado = EstadoCita.valueOf(values[5]);
        String observaciones = values[6].replaceAll(";", ",");

        Paciente paciente = pacientes.get(dniPaciente);
        Medico medico = medicos.get(dniMedico);
        Sala sala = salas.get(numeroSala);

        if (paciente == null) {
            throw new CitaException("Paciente no encontrado: " + dniPaciente);
        }
        if (medico == null) {
            throw new CitaException("Médico no encontrado: " + dniMedico);
        }
        if (sala == null) {
            throw new CitaException("Sala no encontrada: " + numeroSala);
        }

        Cita cita = Cita.builder().paciente(paciente)
                .medico(medico)
                .sala(sala)
                .fechaHora(fechaHora)
                .costo(costo)
                .build();
        cita.setEstado(estado);
        cita.setObservaciones(observaciones);

        return cita;
    }
}
