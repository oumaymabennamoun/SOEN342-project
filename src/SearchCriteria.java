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
        this.professionalId = professionalId;
        this.specialty = specialty;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
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
