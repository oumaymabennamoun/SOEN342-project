import java.time.LocalDate;

public class Patient {

    private static int nextId = 1;

    private final int patientId;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth; 
    private final String phone;
    private final String address;        
    private final String ramqNumber;     

    public Patient(String firstName, String lastName, LocalDate dateOfBirth, String phone, String address, String ramqNumber) {
        this.patientId = nextId++;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.address = address;
        this.ramqNumber = ramqNumber;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getRamqNumber() {
        return ramqNumber;
    }
}
