public class AvailabilityController {

    private final MedicalProfessionalCatalog professionalCatalog;
    private final SpecialtyCatalog specialtyCatalog;
    private final AvailabilityImporter importer;

    public AvailabilityController(MedicalProfessionalCatalog professionalCatalog, SpecialtyCatalog specialtyCatalog, AvailabilityImporter importer) {
        this.professionalCatalog = professionalCatalog;
        this.specialtyCatalog = specialtyCatalog;
        this.importer = importer;
    }

    public int importFromCSV(String filePath) {
        return importer.importFrom(filePath, professionalCatalog, specialtyCatalog);
    }
}