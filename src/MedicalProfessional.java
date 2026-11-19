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
