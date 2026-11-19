import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityController {

    private final MedicalProfessionalCatalog professionalCatalog;
    private final SpecialtyCatalog specialtyCatalog;
    private final AvailabilityImporter importer;
    private final AvailabilityExporter exporter;

    public AvailabilityController(MedicalProfessionalCatalog professionalCatalog, SpecialtyCatalog specialtyCatalog, AvailabilityImporter importer,AvailabilityExporter exporter) {
        this.professionalCatalog = professionalCatalog;
        this.specialtyCatalog = specialtyCatalog;
        this.importer = importer;
        this.exporter=exporter;
    }

    public int importFrom(String filePath) {
        return importer.importFrom(filePath, professionalCatalog, specialtyCatalog);
    }

    public int exportToCSV(String filePath) {
        return exporter.exportTo(filePath, professionalCatalog);
    }

    // Standalone availability search. Returns the free slots matching the criteria, earliest first.
    public List<AvailabilitySlot> searchAvailability(SearchCriteria criteria) {
        Integer professionalId = criteria.getProfessionalId();
        if (professionalId != null && professionalCatalog.getProfessional(professionalId) == null) {
            throw new IllegalArgumentException("No professional with id " + professionalId + ".");
        }

        String specialty = criteria.getSpecialty();
        if (specialty != null && !specialtyExists(specialty)) {
            throw new IllegalArgumentException("No professional has the specialty '" + specialty + "'.");
        }

        List<AvailabilitySlot> result = new ArrayList<>();
        for (MedicalProfessional mp : professionalCatalog.getAll()) {
            for (AvailabilitySlot s : mp.getSlots()) {
                if (criteria.matches(s)) {
                    result.add(s);
                }
            }
        }
        result.sort((a, b) -> a.getStart().compareTo(b.getStart()));
        return result;
    }

    // Making Availability for one professional. Returns the number of slots created.
    // Throws if the professional does not exist or the input is invalid; nothing is created then.
    public int makeAvailability(int professionalId, LocalDate startDate, LocalDate endDate, RecurrencePattern pattern, LocalTime startTime, LocalTime endTime) {
        MedicalProfessional mp = professionalCatalog.getProfessional(professionalId);
        if (mp == null) {
            throw new IllegalArgumentException("No professional with id " + professionalId + ".");
        }
        return mp.makeAvailability(pattern, startDate, endDate, startTime, endTime);
    }

    public List<MedicalProfessional> listProfessionals() {
        return professionalCatalog.getAll();
    }

    // True if at least one professional has this specialty
    private boolean specialtyExists(String specialty) {
        for (MedicalProfessional mp : professionalCatalog.getAll()) {
            if (mp.getSpecialty().getName().equalsIgnoreCase(specialty)) {
                return true;
            }
        }
        return false;
    }
}
