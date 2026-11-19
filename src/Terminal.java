import java.util.Scanner;

public final class Terminal {

    public static void main(String[] args) {
        
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.print("""

                    1. Book an appointment
                    2. Search availability
                    3. Create recurring availability
                    4. Import availability from CSV
                    5. Export availability to CSV
                    0. Exit
                    Choice:""");

            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    bookAppointment();
                    break;
                case "2":
                    searchAvailability();
                    break;
                case "3":
                    createRecurringAvailability();
                    break;
                case "4":
                    importCsv();
                    break;
                case "5":
                    exportCsv();
                    break;
                case "0":
                    System.out.println("Goodbye.");
                    input.close();
                    return;
                default:
                    System.out.println("Invalid choice. Enter a number from 0 to 5.");
                    break;
            }
        }
    }

    private static void bookAppointment() {
        System.out.println("Appointment booking is not implemented yet (issue #7). No data changed.");
    }

    private static void searchAvailability() {
        System.out.println("Availability search is not implemented yet (issue #8). No data changed.");
    }

    private static void createRecurringAvailability() {
        System.out.println("Recurring availability creation is not implemented yet (issue #9). No data changed.");
    }

    private static void importCsv() {
        System.out.println("CSV import is not implemented yet (issue #10). No data changed.");
    }

    private static void exportCsv() {
        System.out.println("CSV export is not implemented yet (issue #11). No data changed.");
    }
}
