import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import java.time.LocalDate;

public class Library {
    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<IssuedRecord> issued = new ArrayList<>();
    private int nextBookId = 1;
    private int nextUserId = 1;
    private int nextIssuedId = 1;

    // runtime hold for last issue due date (for friendly display)
    private LocalDate lastIssueDue = null;

    public static Library loadFromFile(String path) {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            return new Library();
        }
        try {
            String text = Files.readString(p);
            return fromJson(text);
        } catch (IOException e) {
            System.out.println("Failed to read data file: " + e.getMessage());
            return new Library();
        }
    }

    public void saveToFile(String path) {
        try {
            Files.writeString(Paths.get(path), toJson());
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    // --- domain operations ---
    public Book addBook(String title, String author, int copies) {
        Book b = new Book(nextBookId++, title, author, copies, copies);
        books.add(b);
        return b;
    }

    public User addUser(String name, String email) {
        User u = new User(nextUserId++, name, email);
        users.add(u);
        return u;
    }

    public boolean issueBook(int bookId, int userId, int days) {
        Book b = findBookById(bookId);
        User u = findUserById(userId);
        if (b == null || u == null) return false;
        if (b.getAvailableCopies() <= 0) return false;
        b.decrementAvailable();
        LocalDate issueDate = LocalDate.now();
        LocalDate due = issueDate.plusDays(days);
        IssuedRecord r = new IssuedRecord(nextIssuedId++, bookId, userId, issueDate, due, null);
        issued.add(r);
        lastIssueDue = due;
        return true;
    }

    public boolean returnBook(int issuedId) {
        for (IssuedRecord r : issued) {
            if (r.getId() == issuedId) {
                if (r.getReturnDate() != null) return false; // already returned
                r.setReturnDate(LocalDate.now());
                Book b = findBookById(r.getBookId());
                if (b != null) b.incrementAvailable();
                return true;
            }
        }
        return false;
    }

    public List<Book> getBooks() { return Collections.unmodifiableList(books); }
    public List<User> getUsers() { return Collections.unmodifiableList(users); }
    public List<IssuedRecord> getIssuedRecords() { return Collections.unmodifiableList(issued); }

    public Book findBookById(int id) {
        return books.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }
    public User findUserById(int id) {
        return users.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }

    public List<Book> searchByTitle(String kw) {
        String k = kw.toLowerCase();
        List<Book> out = new ArrayList<>();
        for (Book b : books) if (b.getTitle().toLowerCase().contains(k)) out.add(b);
        return out;
    }

    public List<IssuedRecord> getOverdueRecords() {
        LocalDate today = LocalDate.now();
        List<IssuedRecord> out = new ArrayList<>();
        for (IssuedRecord r : issued) {
            if (r.getReturnDate() == null && r.getDueDate().isBefore(today)) out.add(r);
        }
        return out;
    }

    public LocalDate getLastIssueDueDate() { return lastIssueDue; }

    // ----- JSON serialization (custom, simple, safe for our shapes) -----
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"nextBookId\": ").append(nextBookId).append(",\n");
        sb.append("  \"nextUserId\": ").append(nextUserId).append(",\n");
        sb.append("  \"nextIssuedId\": ").append(nextIssuedId).append(",\n");

        sb.append("  \"books\": [\n");
        for (int i = 0; i < books.size(); i++) {
            sb.append(indent(books.get(i).toJson(), 4));
            if (i < books.size() - 1) sb.append(",\n");
            else sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"users\": [\n");
        for (int i = 0; i < users.size(); i++) {
            sb.append(indent(users.get(i).toJson(), 4));
            if (i < users.size() - 1) sb.append(",\n");
            else sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"issued\": [\n");
        for (int i = 0; i < issued.size(); i++) {
            sb.append(indent(issued.get(i).toJson(), 4));
            if (i < issued.size() - 1) sb.append(",\n");
            else sb.append("\n");
        }
        sb.append("  ]\n");

        sb.append("}\n");
        return sb.toString();
    }

    private static String indent(String s, int spaces) {
        String pad = " ".repeat(spaces);
        String[] lines = s.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(pad).append(lines[i]);
            if (i < lines.length - 1) sb.append("\n");
        }
        return sb.toString();
    }

    // Very simple JSON parser tuned to our format. Not a general-purpose JSON library.
    public static Library fromJson(String text) {
        Library lib = new Library();
        // extract integers
        lib.nextBookId = getIntField(text, "\"nextBookId\"\\s*:\\s*(\\d+)", lib.nextBookId);
        lib.nextUserId = getIntField(text, "\"nextUserId\"\\s*:\\s*(\\d+)", lib.nextUserId);
        lib.nextIssuedId = getIntField(text, "\"nextIssuedId\"\\s*:\\s*(\\d+)", lib.nextIssuedId);

        String booksArray = getArrayBody(text, "\"books\"\\s*:\\s*\\[", "]");
        if (booksArray != null) {
            List<String> objs = splitObjects(booksArray);
            for (String o : objs) {
                Book b = Book.fromJson(o);
                if (b != null) lib.books.add(b);
            }
        }

        String usersArray = getArrayBody(text, "\"users\"\\s*:\\s*\\[", "]");
        if (usersArray != null) {
            List<String> objs = splitObjects(usersArray);
            for (String o : objs) {
                User u = User.fromJson(o);
                if (u != null) lib.users.add(u);
            }
        }

        String issuedArray = getArrayBody(text, "\"issued\"\\s*:\\s*\\[", "]");
        if (issuedArray != null) {
            List<String> objs = splitObjects(issuedArray);
            for (String o : objs) {
                IssuedRecord r = IssuedRecord.fromJson(o);
                if (r != null) lib.issued.add(r);
            }
        }

        // adjust next ids if parsed elements have larger ids (safety)
        lib.nextBookId = Math.max(lib.nextBookId, lib.books.stream().mapToInt(Book::getId).max().orElse(0) + 1);
        lib.nextUserId = Math.max(lib.nextUserId, lib.users.stream().mapToInt(User::getId).max().orElse(0) + 1);
        lib.nextIssuedId = Math.max(lib.nextIssuedId, lib.issued.stream().mapToInt(IssuedRecord::getId).max().orElse(0) + 1);

        return lib;
    }

    // helpers for parsing
    private static int getIntField(String text, String regex, int fallback) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        }
        return fallback;
    }

    private static String getArrayBody(String text, String startRegex, String endToken) {
        Pattern p = Pattern.compile(startRegex, Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (!m.find()) return null;
        int start = m.end();
        // find matching closing bracket for this array (simple approach)
        int depth = 0;
        int i = start;
        for (; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                if (depth == 0) {
                    return text.substring(start, i).trim();
                } else depth--;
            }
        }
        return null;
    }

    // Splits top-level objects in an array body (handles nested braces)
    private static List<String> splitObjects(String arrBody) {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < arrBody.length()) {
            // skip whitespace and commas
            while (i < arrBody.length() && (Character.isWhitespace(arrBody.charAt(i)) || arrBody.charAt(i) == ',')) i++;
            if (i >= arrBody.length()) break;
            if (arrBody.charAt(i) != '{') break;
            int start = i;
            int depth = 0;
            for (; i < arrBody.length(); i++) {
                char c = arrBody.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) { i++; break; }
                }
            }
            String obj = arrBody.substring(start, i).trim();
            out.add(obj);
        }
        return out;
    }
}
