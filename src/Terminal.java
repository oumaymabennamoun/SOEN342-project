import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public final class Terminal {

    // Shared in-memory state for one run.
    private static final Scanner input = new Scanner(System.in);
    private static final PatientCatalog patientCatalog = new PatientCatalog();
    private static final MedicalProfessionalCatalog professionalCatalog = new MedicalProfessionalCatalog();
    private static final AppointmentCatalog appointmentCatalog = new AppointmentCatalog();
    private static final AppointmentController appointmentController = new AppointmentController(patientCatalog, professionalCatalog, appointmentCatalog);
    private static final AvailabilityController availabilityController = new AvailabilityController(professionalCatalog);

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

    // Search Availability (included in UC-01). Uses the same SearchCriteria rule as booking.
    private static void searchAvailability() {
        try {
            List<MedicalProfessional> professionals = availabilityController.listProfessionals();
            if (professionals.isEmpty()) {
                showError("There are no professionals yet. Import availability from CSV (option 4) first.");
                return;
            }

            showProfessionals(professionals);
            Integer professionalId = readOptionalInt("Professional id (blank for all): ");
            String specialty = readLine("Specialty (blank for any): ");
            LocalDate from = readOptionalDate("From date (YYYY-MM-DD, blank for any): ");
            LocalDate to = readOptionalDate("To date (YYYY-MM-DD, blank for same as from date): ");
            if (to == null) {
                to = from;
            }

            SearchCriteria criteria = new SearchCriteria(professionalId, specialty, from, to, null, null);
            List<AvailabilitySlot> slots = availabilityController.searchAvailability(criteria);

            if (slots.isEmpty()) {
                System.out.println("No available slots match your search.");
            } else {
                showAvailableSlots(slots);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    // Making Availability: recurring slots for one professional over a date range.
    // Each start-end range is split into 30-minute slots (MedicalProfessional.SLOT_MINUTES).
    private static void createRecurringAvailability() {
        try {
            List<MedicalProfessional> professionals = availabilityController.listProfessionals();
            if (professionals.isEmpty()) {
                showError("There are no professionals yet. Import availability from CSV (option 4) first.");
                return;
            }

            showProfessionals(professionals);
            int professionalId = readInt("Professional id: ");
            if (!professionalExists(professionals, professionalId)) {
                throw new IllegalArgumentException("No professional with id " + professionalId + ".");
            }

            LocalDate startDate = readDate("Start date (YYYY-MM-DD): ");
            LocalDate endDate = readDate("End date (YYYY-MM-DD): ");
            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date " + endDate + " is before start date " + startDate + ".");
            }

            RecurrencePattern pattern = readPattern();
            LocalTime startTime = readTime("Start time (HH:MM): ");
            LocalTime endTime = readTime("End time (HH:MM): ");

            int created = availabilityController.makeAvailability(professionalId, startDate, endDate, pattern, startTime, endTime);
            System.out.println("Created " + created + " slots of " + MedicalProfessional.SLOT_MINUTES + " minutes. Use option 2 to see them.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(e.getMessage() + " No availability was created.");
        }
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

    // Shows each slot with its professional and specialty. Used by both search and booking.
    private static void showAvailableSlots(List<AvailabilitySlot> slots) {
        System.out.println("Available slots (" + slots.size() + "):");
        for (AvailabilitySlot s : slots) {
            MedicalProfessional mp = s.getProfessional();
            System.out.println("  [" + s.getSlotId() + "] " + mp.getTitle() + " " + mp.getFirstName() + " " + mp.getLastName()
                    + " (" + mp.getSpecialty().getName() + ")  " + s.getDate() + "  " + s.getStartTime() + "-" + s.getEndTime());
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

    private static boolean professionalExists(List<MedicalProfessional> professionals, int professionalId) {
        for (MedicalProfessional mp : professionals) {
            if (mp.getProfessionalId() == professionalId) {
                return true;
            }
        }
        return false;
    }

    // Asks for WEEKLY (with days), MONTHLY (with a day of the month) or DAILY.
    private static RecurrencePattern readPattern() {
        String type = readLine("Repeat (WEEKLY, MONTHLY or DAILY): ").toUpperCase();
        switch (type) {
            case "WEEKLY":
                return RecurrencePattern.weekly(readDays("Days of the week (e.g. MON or MON,WED,FRI): "));
            case "MONTHLY":
                return RecurrencePattern.monthly(readInt("Day of the month (1-31): "));
            case "DAILY":
                return RecurrencePattern.daily();
            default:
                throw new IllegalArgumentException("Repeat must be WEEKLY, MONTHLY or DAILY.");
        }
    }

    // Reads a comma-separated list like MON,WED or Monday,Wednesday.
    private static Set<DayOfWeek> readDays(String prompt) {
        String text = readLine(prompt);
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);

        for (String part : text.split(",")) {
            String name = part.trim().toUpperCase();
            if (name.isEmpty()) {
                continue;
            }
            DayOfWeek match = null;
            for (DayOfWeek d : DayOfWeek.values()) {
                if (name.length() >= 3 && d.name().startsWith(name)) {
                    match = d;
                }
            }
            if (match == null) {
                throw new IllegalArgumentException("'" + part.trim() + "' is not a day. Use MON, TUE, WED, THU, FRI, SAT or SUN.");
            }
            days.add(match);
        }
        return days;
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

    // Returns null if the user leaves it blank.
    private static Integer readOptionalInt(String prompt) {
        String text = readLine(prompt);
        if (text.isEmpty()) {
            return null;
        }
        return parseInt(text);
    }

    // Returns null if the user leaves it blank.
    private static LocalDate readOptionalDate(String prompt) {
        String text = readLine(prompt);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("'" + text + "' is not a valid date. Use YYYY-MM-DD.");
        }
    }

    private static LocalDate readDate(String prompt) {
        LocalDate date = readOptionalDate(prompt);
        if (date == null) {
            throw new IllegalArgumentException("A date is required.");
        }
        return date;
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
