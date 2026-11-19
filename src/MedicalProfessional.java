import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicalProfessional {

    private static int nextId = 1;

    // Slot policy for Making Availability: each start-end range is split into slots of this length.
    public static final int SLOT_MINUTES = 30;

    private final int professionalId;
    private final String firstName;
    private final String lastName;
    private final String title;
    private final String phone;
    private final String licenseNumber;
    private final Specialty specialty;                             
    private final List<AvailabilitySlot> slots = new ArrayList<>(); 

    public MedicalProfessional(String firstName, String lastName, String title, String phone, String licenseNumber, Specialty specialty) {
        this.professionalId = nextId++;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.specialty = specialty;
    }

    public AvailabilitySlot getSlot(int slotId) {
        for (AvailabilitySlot s : slots) {
            if (s.getSlotId() == slotId) {
                return s;
            }
        }
        return null;
    }

    // Free slots on the preferred day, at or after the preferred time, earliest first.
    // Uses the same SearchCriteria rule as the standalone availability search.
    public List<AvailabilitySlot> getAvailableSlots(LocalDateTime preferredTime) {
        LocalDate day = preferredTime.toLocalDate();
        SearchCriteria criteria = new SearchCriteria(professionalId, null, day, day, preferredTime.toLocalTime(), null);

        List<AvailabilitySlot> result = new ArrayList<>();
        for (AvailabilitySlot s : slots) {
            if (criteria.matches(s)) {
                result.add(s);
            }
        }
        sortByStart(result);
        return result;
    }

    // The next 5 free slots after the preferred time, used as alternatives.
    public List<AvailabilitySlot> getNextAvailableSlots(LocalDateTime preferredTime) {
        List<AvailabilitySlot> result = new ArrayList<>();
        for (AvailabilitySlot s : slots) {
            if (s.isAvailable() && s.getStart().isAfter(preferredTime)) {
                result.add(s);
            }
        }
        sortByStart(result);
        while (result.size() > 5) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    private static void sortByStart(List<AvailabilitySlot> list) {
        list.sort((a, b) -> a.getStart().compareTo(b.getStart()));
    }

    // Making Availability. For every date the pattern gives inside the range, splits
    // startTime-endTime into SLOT_MINUTES slots (e.g. 09:00-12:00 gives six 30-minute slots).
    // All or nothing: if any new slot would overlap an existing one, no slot is created.
    // Returns the number of slots created.
    public int makeAvailability(RecurrencePattern pattern, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        if (pattern == null) {
            throw new IllegalArgumentException("A repetition pattern is required.");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("A start time and an end time are required.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time " + endTime + " must be after start time " + startTime + ".");
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes % SLOT_MINUTES != 0) {
            throw new IllegalArgumentException("The time range " + startTime + "-" + endTime + " must divide into "
                    + SLOT_MINUTES + "-minute slots.");
        }

        List<LocalDate> dates = pattern.generateDates(startDate, endDate);
        if (dates.isEmpty()) {
            throw new IllegalArgumentException("No dates between " + startDate + " and " + endDate + " match the pattern.");
        }

        // Check every new slot first, so nothing is added if there is a conflict.
        for (LocalDate date : dates) {
            for (LocalTime t = startTime; t.isBefore(endTime); t = t.plusMinutes(SLOT_MINUTES)) {
                if (overlapsExisting(date, t, t.plusMinutes(SLOT_MINUTES))) {
                    throw new IllegalArgumentException("Conflict: " + date + " " + t + "-" + t.plusMinutes(SLOT_MINUTES)
                            + " overlaps an existing slot.");
                }
            }
        }

        int created = 0;
        for (LocalDate date : dates) {
            for (LocalTime t = startTime; t.isBefore(endTime); t = t.plusMinutes(SLOT_MINUTES)) {
                addSlot(new AvailabilitySlot(date, t, t.plusMinutes(SLOT_MINUTES)));
                created++;
            }
        }
        return created;
    }

    // True if date start-end overlaps one of this professional's slots.
    private boolean overlapsExisting(LocalDate date, LocalTime start, LocalTime end) {
        for (AvailabilitySlot s : slots) {
            if (s.getDate().equals(date) && s.getStartTime().isBefore(end) && start.isBefore(s.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    // Adds the slot unless it overlaps one this professional already has.
    public boolean addSlot(AvailabilitySlot slot) {
        if (hasOverlap(slot)) {
            return false;
        }
        slots.add(slot);
        slot.setProfessional(this);
        return true;
    }

    public boolean hasOverlap(AvailabilitySlot slot) {
        for (AvailabilitySlot s : slots) {
            if (s.overlaps(slot)) {
                return true;
            }
        }
        return false;
    }

    public int getProfessionalId() {
        return professionalId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTitle() {
        return title;
    }

    public String getPhone() {
        return phone;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public List<AvailabilitySlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }
}
