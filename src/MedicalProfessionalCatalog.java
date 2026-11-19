import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalProfessionalCatalog {

    private final Map<Integer, MedicalProfessional> professionals = new LinkedHashMap<>();

    public void add(MedicalProfessional professional) {
        professionals.put(professional.getProfessionalId(), professional);
    }

    // Returns null if there is no professional with that id.
    public MedicalProfessional getProfessional(int professionalId) {
        return professionals.get(professionalId);
    }

    public List<MedicalProfessional> getAll() {
        return new ArrayList<>(professionals.values());
    }
}
