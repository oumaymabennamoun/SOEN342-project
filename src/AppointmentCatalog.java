import java.util.LinkedHashMap;
import java.util.Map;

public class AppointmentCatalog {

    private final Map<Integer, Appointment> appointments = new LinkedHashMap<>();

    // SD_UC01c: creates the appointment, marks the slot unavailable, then stores it.
    public Appointment makeAppointment(Patient patient, MedicalProfessional professional, AvailabilitySlot slot, String reason) {
        slot.markUnavailable();
        Appointment a = new Appointment(patient, professional, slot, reason);
        appointments.put(a.getAppointmentId(), a);
        return a;
    }

    // Returns null if there is no appointment with that id.
    public Appointment getAppointment(int appointmentId) {
        return appointments.get(appointmentId);
    }
}
