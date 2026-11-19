import java.time.LocalDate;
import java.time.LocalTime;

// What to search for. Every field is optional: null means "any".
public class SearchCriteria {

    private final Integer professionalId;
    private final String specialty;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public SearchCriteria(Integer professionalId, String specialty, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date " + endDate + " is before start date " + startDate + ".");
        }
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time " + endTime + " must be after start time " + startTime + ".");
        }

        // A blank specialty means "any".
        String cleanSpecialty = null;
        if (specialty != null && !specialty.isBlank()) {
            cleanSpecialty = specialty.trim();
        }

        this.professionalId = professionalId;
        this.specialty = cleanSpecialty;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // The one search rule, used by both standalone search and booking.
    // A slot matches if it is free and passes every filter that was given.
    public boolean matches(AvailabilitySlot slot) {
        if (!slot.isAvailable()) {
            return false;
        }

        MedicalProfessional mp = slot.getProfessional();
        if (professionalId != null && (mp == null || mp.getProfessionalId() != professionalId)) {
            return false;
        }
        if (specialty != null && (mp == null || !mp.getSpecialty().getName().equalsIgnoreCase(specialty))) {
            return false;
        }
        if (startDate != null && slot.getDate().isBefore(startDate)) {
            return false;
        }
        if (endDate != null && slot.getDate().isAfter(endDate)) {
            return false;
        }
        if (startTime != null && slot.getStartTime().isBefore(startTime)) {
            return false;
        }
        if (endTime != null && slot.getEndTime().isAfter(endTime)) {
            return false;
        }
        return true;
    }

    public Integer getProfessionalId() {
        return professionalId;
    }

    public String getSpecialty() {
        return specialty;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
