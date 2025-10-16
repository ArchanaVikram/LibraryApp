import java.util.Scanner;
import java.time.LocalDate;
import java.util.List;

public class Main {
    private static final String DATA_FILE = "library.json";
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Library lib = Library.loadFromFile(DATA_FILE);
        System.out.println("=== Library (JSON) ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addBookFlow(lib);
                    case "2" -> listBooks(lib);
                    case "3" -> searchBookFlow(lib);
                    case "4" -> registerUserFlow(lib);
                    case "5" -> listUsers(lib);
                    case "6" -> issueBookFlow(lib);
                    case "7" -> returnBookFlow(lib);
                    case "8" -> listIssued(lib);
                    case "9" -> listOverdue(lib);
                    case "0" -> {
                        lib.saveToFile(DATA_FILE);
                        System.out.println("Saved. Exiting.");
                        running = false;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
        sc.close();
    }

    private static void printMenu() {
        System.out.println("Menu:");
        System.out.println("1) Add book");
        System.out.println("2) List books");
        System.out.println("3) Search book (id/title)");
        System.out.println("4) Register user");
        System.out.println("5) List users");
        System.out.println("6) Issue book");
        System.out.println("7) Return book");
        System.out.println("8) List issued records");
        System.out.println("9) List overdue books");
        System.out.println("0) Save & Exit");
        System.out.print("Choose: ");
    }

    // flows
    private static void addBookFlow(Library lib) {
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Author: ");
        String author = sc.nextLine().trim();
        int copies = readInt("Copies: ", 1);
        Book b = lib.addBook(title, author, copies);
        System.out.println("Added: " + b);
        lib.saveToFile(DATA_FILE);
    }

    private static void listBooks(Library lib) {
        List<Book> books = lib.getBooks();
        if (books.isEmpty()) {
            System.out.println("No books.");
            return;
        }
        for (Book b : books) System.out.println(b);
    }

    private static void searchBookFlow(Library lib) {
        System.out.print("Search by (1) id or (2) title? ");
        String opt = sc.nextLine().trim();
        if ("1".equals(opt)) {
            int id = readInt("Book ID: ", 1);
            Book b = lib.findBookById(id);
            System.out.println(b == null ? "Not found." : b);
        } else {
            System.out.print("Keyword: ");
            String kw = sc.nextLine().trim();
            List<Book> res = lib.searchByTitle(kw);
            if (res.isEmpty()) System.out.println("No matches.");
            else res.forEach(System.out::println);
        }
    }

    private static void registerUserFlow(Library lib) {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Email (optional): ");
        String email = sc.nextLine().trim();
        User u = lib.addUser(name, email.isEmpty() ? null : email);
        System.out.println("Registered: " + u);
        lib.saveToFile(DATA_FILE);
    }

    private static void listUsers(Library lib) {
        List<User> users = lib.getUsers();
        if (users.isEmpty()) System.out.println("No users.");
        else users.forEach(System.out::println);
    }

    private static void issueBookFlow(Library lib) {
        int bookId = readInt("Book ID: ", 1);
        int userId = readInt("User ID: ", 1);
        int days = readInt("Loan days (e.g., 14): ", 1);
        boolean ok = lib.issueBook(bookId, userId, days);
        System.out.println(ok ? "Issued. Due: " + lib.getLastIssueDueDate() : "Failed to issue.");
        lib.saveToFile(DATA_FILE);
    }

    private static void returnBookFlow(Library lib) {
        int issuedId = readInt("Issued record ID: ", 1);
        boolean ok = lib.returnBook(issuedId);
        System.out.println(ok ? "Returned." : "Return failed or already returned.");
        lib.saveToFile(DATA_FILE);
    }

    private static void listIssued(Library lib) {
        List<IssuedRecord> list = lib.getIssuedRecords();
        if (list.isEmpty()) System.out.println("No issued records.");
        else list.forEach(System.out::println);
    }

    private static void listOverdue(Library lib) {
        List<IssuedRecord> overdue = lib.getOverdueRecords();
        if (overdue.isEmpty()) System.out.println("No overdue books.");
        else overdue.forEach(r -> {
            Book b = lib.findBookById(r.getBookId());
            User u = lib.findUserById(r.getUserId());
            System.out.printf("IssuedID:%d | Book:%s | Borrower:%s | Due:%s%n",
                    r.getId(), b == null ? "?" : b.getTitle(), u == null ? "?" : u.getName(), r.getDueDate());
        });
    }

    private static int readInt(String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < min) System.out.println("Enter >= " + min);
                else return v;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }
}
