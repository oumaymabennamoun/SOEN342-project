import java.time.LocalDateTime;

public class Appointment {

    private static int nextId = 1;

    private final int appointmentId;
    private final LocalDateTime dateTime;
    private AppointmentStatus status;
    private final String reason;
    private final Patient patient;                  // 1
    private final MedicalProfessional professional; // 1
    private final AvailabilitySlot slot;            // 1

    public Appointment(Patient patient, MedicalProfessional professional, AvailabilitySlot slot, String reason) {
        this.appointmentId = nextId++;
        this.patient = patient;
        this.professional = professional;
        this.slot = slot;
        this.dateTime = slot.getDate().atTime(slot.getStartTime());
        this.reason = reason;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Patient getPatient() {
        return patient;
    }

    public MedicalProfessional getProfessional() {
        return professional;
    }

    public AvailabilitySlot getSlot() {
        return slot;
    }
}
