public interface AvailabilityImporter {
    int importFrom(String filePath, MedicalProfessionalCatalog professionalCatalog, SpecialtyCatalog specialtyCatalog);
}