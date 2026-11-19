import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicalProfessional {

    private static int nextId = 1;

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
    public List<AvailabilitySlot> getAvailableSlots(LocalDateTime preferredTime) {
        List<AvailabilitySlot> result = new ArrayList<>();
        for (AvailabilitySlot s : slots) {
            if (s.isAvailableOn(preferredTime)) {
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

    // Adds the slot unless it overlaps one this professional already has.
    public boolean addSlot(AvailabilitySlot slot) {
        if (hasOverlap(slot)) {
            return false;
        }
        slots.add(slot);
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
