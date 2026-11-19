import java.time.LocalDateTime;
import java.util.List;

public class AppointmentController {

    private final PatientCatalog patientCatalog;
    private final MedicalProfessionalCatalog professionalCatalog;
    private final AppointmentCatalog appointmentCatalog;

    private MedicalProfessional selectedProfessional;
    private AvailabilitySlot selectedSlot;

    public AppointmentController(PatientCatalog patientCatalog, MedicalProfessionalCatalog professionalCatalog, AppointmentCatalog appointmentCatalog) {
        this.patientCatalog = patientCatalog;
        this.professionalCatalog = professionalCatalog;
        this.appointmentCatalog = appointmentCatalog;
    }

    // SD_UC01a: finds the patient and professional, remembers the professional,
    // and returns the free slots on the preferred day at or after the preferred time.
    public List<AvailabilitySlot> requestAppointment(int patientId, int professionalId, LocalDateTime preferredTime, String reason) {
        requireReason(reason);
        requirePatient(patientId);
        MedicalProfessional mp = requireProfessional(professionalId);

        selectedProfessional = mp;
        selectedSlot = null;
        return mp.getAvailableSlots(preferredTime);
    }

    // SD_UC01a (no slots that day): the next free slots after the preferred time.
    public List<AvailabilitySlot> requestAlternatives(int professionalId, LocalDateTime preferredTime) {
        MedicalProfessional mp = requireProfessional(professionalId);
        return mp.getNextAvailableSlots(preferredTime);
    }

    // SD_UC01b: looks up the slot on the selected professional and remembers it if it is free.
    public boolean selectSlot(int slotId) {
        if (selectedProfessional == null) {
            throw new IllegalStateException("Request an appointment before selecting a slot.");
        }

        AvailabilitySlot s = selectedProfessional.getSlot(slotId);
        if (s == null) {
            throw new IllegalArgumentException("Slot " + slotId + " does not belong to this professional.");
        }

        boolean available = s.isAvailable();
        if (available) {
            selectedSlot = s;
        }
        return available;
    }

    // SD_UC01c: checks availability again, then creates and confirms the appointment.
    // Throws if the slot was booked in the meantime, and nothing is created.
    public int confirmAppointment(int patientId, int professionalId, int slotId, String reason) {
        requireReason(reason);
        Patient p = requirePatient(patientId);
        MedicalProfessional mp = requireProfessional(professionalId);

        AvailabilitySlot s = mp.getSlot(slotId);
        if (s == null) {
            throw new IllegalArgumentException("Slot " + slotId + " does not belong to this professional.");
        }
        if (!s.isAvailable()) {
            throw new IllegalStateException("Slot " + slotId + " is no longer available.");
        }

        Appointment a = appointmentCatalog.makeAppointment(p, mp, s, reason.trim());
        a.confirm();

        selectedProfessional = null;
        selectedSlot = null;
        return a.getAppointmentId();
    }

    // Returns null if there is no appointment with that id.
    public Appointment getAppointment(int appointmentId) {
        return appointmentCatalog.getAppointment(appointmentId);
    }

    public List<MedicalProfessional> listProfessionals() {
        return professionalCatalog.getAll();
    }

    public List<Patient> findPatients(String query) {
        return patientCatalog.search(query);
    }

    // Registers a new patient and returns their id. RAMQ number is optional.
    public int registerPatient(String firstName, String lastName, String phone, String ramqNumber) {
        if (firstName == "" || lastName == "") {
            throw new IllegalArgumentException("First name and last name are required.");
        }
        if (phone == "" || !phone.trim().matches("[0-9 ()+-]{7,20}")) {
            throw new IllegalArgumentException("Phone must be 7 to 20 digits (spaces, dashes and brackets allowed).");
        }

        String ramq = null;
        if (ramqNumber != null && ramqNumber != "") {
            ramq = ramqNumber.trim().toUpperCase();
            if (!ramq.matches("[A-Z]{4}[0-9]{8}")) {
                throw new IllegalArgumentException("RAMQ number must be 4 letters followed by 8 digits, e.g. ABCD12345678.");
            }
        }

        Patient p = new Patient(firstName.trim(), lastName.trim(), null, phone.trim(), null, ramq);
        patientCatalog.add(p);
        return p.getPatientId();
    }

    // Throws if there is no patient with that id.
    private Patient requirePatient(int patientId) {
        Patient p = patientCatalog.getPatient(patientId);
        if (p == null) {
            throw new IllegalArgumentException("No patient with id " + patientId + ".");
        }
        return p;
    }

    // Throws if there is no professional with that id.
    private MedicalProfessional requireProfessional(int professionalId) {
        MedicalProfessional mp = professionalCatalog.getProfessional(professionalId);
        if (mp == null) {
            throw new IllegalArgumentException("No professional with id " + professionalId + ".");
        }
        return mp;
    }

    private static void requireReason(String reason) {
        if (reason == null || reason == "") {
            throw new IllegalArgumentException("A reason for the appointment is required.");
        }
    }
}
