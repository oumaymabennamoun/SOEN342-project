import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public final class Terminal {

    // Shared in-memory state for one run.
    private static final Scanner input = new Scanner(System.in);
    private static final PatientCatalog patientCatalog = new PatientCatalog();
    private static final MedicalProfessionalCatalog professionalCatalog = new MedicalProfessionalCatalog();
    private static final AppointmentCatalog appointmentCatalog = new AppointmentCatalog();
    private static final AppointmentController appointmentController = new AppointmentController(patientCatalog, professionalCatalog, appointmentCatalog);

    public static void main(String[] args) {

        while (true) {
            System.out.print("""

                    1. Book an appointment
                    2. Search availability
                    3. Create recurring availability
                    4. Import availability from CSV
                    5. Export availability to CSV
                    0. Exit
                    Choice:""");

            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    bookAppointment();
                    break;
                case "2":
                    searchAvailability();
                    break;
                case "3":
                    createRecurringAvailability();
                    break;
                case "4":
                    importCsv();
                    break;
                case "5":
                    exportCsv();
                    break;
                case "0":
                    System.out.println("Goodbye.");
                    input.close();
                    return;
                default:
                    System.out.println("Invalid choice. Enter a number from 0 to 5.");
                    break;
            }
        }
    }

    // UC-01 Book appointment, for a patient at the front desk or on the phone.
    // Follows SD_UC01a (requestAppointment), SD_UC01b (selectSlot) and SD_UC01c (confirmAppointment).
    private static void bookAppointment() {
        try {
            List<MedicalProfessional> professionals = appointmentController.listProfessionals();
            if (professionals.isEmpty()) {
                showError("There are no professionals yet. Import availability from CSV (option 4) first.");
                return;
            }

            int patientId = identifyPatient();
            showProfessionals(professionals);
            int professionalId = readInt("Professional id: ");
            LocalDate date = readDate("Preferred date (YYYY-MM-DD): ");
            LocalTime time = readTime("Preferred time (HH:MM): ");
            String reason = readLine("Reason for visit: ");
            LocalDateTime preferredTime = date.atTime(time);

            // SD_UC01a: requestAppointment
            List<AvailabilitySlot> slots = appointmentController.requestAppointment(patientId, professionalId, preferredTime, reason);

            if (slots.isEmpty()) {
                showError("No available appointments on " + date + " at or after " + time + ".");
                List<AvailabilitySlot> alternatives = appointmentController.requestAlternatives(professionalId, preferredTime);

                if (alternatives.isEmpty()) {
                    System.out.println("This professional has no later availability. No appointment was made.");
                    return;
                }
                System.out.println("Next available slots with this professional:");
                showAvailableSlots(alternatives);
            } else {
                showAvailableSlots(slots);
            }

            String slotText = readLine("Slot id to book (blank to cancel): ");
            if (slotText.isEmpty()) {
                System.out.println("Booking cancelled. No appointment was made.");
                return;
            }
            int slotId = parseInt(slotText);

            // SD_UC01b: selectSlot
            if (!appointmentController.selectSlot(slotId)) {
                showError("Slot " + slotId + " is already booked. No appointment was made.");
                return;
            }

            // SD_UC01c: confirmAppointment (availability is checked again here)
            int appointmentId = appointmentController.confirmAppointment(patientId, professionalId, slotId, reason);
            showAppointmentConfirmation(appointmentController.getAppointment(appointmentId));

        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(e.getMessage() + " No appointment was made.");
        }
    }

    private static void searchAvailability() {
        System.out.println("Availability search is not implemented yet (issue #8). No data changed.");
    }

    private static void createRecurringAvailability() {
        System.out.println("Recurring availability creation is not implemented yet (issue #9). No data changed.");
    }

    private static void importCsv() {
        System.out.println("CSV import is not implemented yet (issue #10). No data changed.");
    }

    private static void exportCsv() {
        System.out.println("CSV export is not implemented yet (issue #11). No data changed.");
    }

    
    // ==================== Helper methods ====================

    // Finds an existing patient by name, phone or RAMQ, or registers a new one. Returns the patient id.
    private static int identifyPatient() {
        String query = readLine("Search patient by name, phone or RAMQ (blank to register a new patient): ");

        if (!query.isEmpty()) {
            List<Patient> matches = appointmentController.findPatients(query);

            if (matches.isEmpty()) {
                System.out.println("No patient matches '" + query + "'.");
            } else {
                for (Patient p : matches) {
                    String ramq = "-";
                    if (p.getRamqNumber() != null) {
                        ramq = p.getRamqNumber();
                    }
                    System.out.println("  [" + p.getPatientId() + "] " + p.getFirstName() + " " + p.getLastName()
                            + "  phone " + p.getPhone() + "  RAMQ " + ramq);
                }
                String idText = readLine("Patient id (blank to register a new patient): ");
                if (!idText.isEmpty()) {
                    return parseInt(idText);
                }
            }
        }

        System.out.println("Register a new patient:");
        String first = readLine("  First name: ");
        String last = readLine("  Last name: ");
        String phone = readLine("  Phone: ");
        String ramq = readLine("  RAMQ number (optional): ");
        int id = appointmentController.registerPatient(first, last, phone, ramq);
        System.out.println("Patient registered with id " + id + ".");
        return id;
    }

    private static void showProfessionals(List<MedicalProfessional> professionals) {
        System.out.println("Professionals:");
        for (MedicalProfessional mp : professionals) {
            System.out.println("  [" + mp.getProfessionalId() + "] " + mp.getTitle() + " " + mp.getFirstName() + " "
                    + mp.getLastName() + " (" + mp.getSpecialty().getName() + ")");
        }
    }

    private static void showAvailableSlots(List<AvailabilitySlot> slots) {
        System.out.println("Available slots:");
        for (AvailabilitySlot s : slots) {
            System.out.println("  [" + s.getSlotId() + "] " + s.getDate() + " " + s.getStartTime() + "-" + s.getEndTime());
        }
    }

    private static void showAppointmentConfirmation(Appointment a) {
        MedicalProfessional mp = a.getProfessional();
        AvailabilitySlot s = a.getSlot();
        System.out.println("Appointment " + a.getAppointmentId() + " " + a.getStatus() + ": "
                + a.getPatient().getFirstName() + " " + a.getPatient().getLastName()
                + " with " + mp.getTitle() + " " + mp.getFirstName() + " " + mp.getLastName()
                + " on " + s.getDate() + " " + s.getStartTime() + "-" + s.getEndTime()
                + " (" + a.getReason() + ")");
    }

    private static void showError(String message) {
        System.out.println("Error: " + message);
    }

    // Input helpers. Each one throws IllegalArgumentException with a clear message on bad input.

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) {
            throw new IllegalStateException("Input ended.");
        }
        return input.nextLine().trim();
    }

    private static int readInt(String prompt) {
        return parseInt(readLine(prompt));
    }

    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a whole number.");
        }
    }

    private static LocalDate readDate(String prompt) {
        String text = readLine(prompt);
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("'" + text + "' is not a valid date. Use YYYY-MM-DD.");
        }
    }

    private static LocalTime readTime(String prompt) {
        String text = readLine(prompt);
        if (text.length() == 4) {
            text = "0" + text; // accept 9:00 as 09:00
        }
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("'" + text + "' is not a valid time. Use HH:MM (24-hour).");
        }
    }
}
