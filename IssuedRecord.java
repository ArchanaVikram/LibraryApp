import java.time.LocalDate;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class IssuedRecord {
    private final int id;
    private final int bookId;
    private final int userId;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate; // nullable

    public IssuedRecord(int id, int bookId, int userId, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public int getUserId() { return userId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate d) { this.returnDate = d; }

    @Override
    public String toString() {
        return String.format("IssuedID:%d | BookID:%d | UserID:%d | Issued:%s | Due:%s | Returned:%s",
                id, bookId, userId, issueDate, dueDate, returnDate == null ? "-" : returnDate.toString());
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"id\": ").append(id).append(",\n");
        sb.append("  \"bookId\": ").append(bookId).append(",\n");
        sb.append("  \"userId\": ").append(userId).append(",\n");
        sb.append("  \"issueDate\": \"").append(issueDate.toString()).append("\",\n");
        sb.append("  \"dueDate\": \"").append(dueDate.toString()).append("\",\n");
        sb.append("  \"returnDate\": ").append(returnDate == null ? "null" : "\"" + returnDate.toString() + "\"").append("\n");
        sb.append("}");
        return sb.toString();
    }

    public static IssuedRecord fromJson(String text) {
        int id = extractInt(text, "\"id\"\\s*:\\s*(\\d+)");
        int bookId = extractInt(text, "\"bookId\"\\s*:\\s*(\\d+)");
        int userId = extractInt(text, "\"userId\"\\s*:\\s*(\\d+)");
        String issue = extractString(text, "\"issueDate\"\\s*:\\s*\"(.*?)\"");
        String due = extractString(text, "\"dueDate\"\\s*:\\s*\"(.*?)\"");
        String ret = extractStringOptional(text, "\"returnDate\"\\s*:\\s*(null|\"(.*?)\")");
        if (id == -1) return null;
        LocalDate issueDate = LocalDate.parse(issue);
        LocalDate dueDate = LocalDate.parse(due);
        LocalDate returnDate = "null".equals(ret) ? null : LocalDate.parse(ret);
        return new IssuedRecord(id, bookId, userId, issueDate, dueDate, returnDate);
    }

    private static int extractInt(String t, String regex) {
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(t);
        if (m.find()) try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        return -1;
    }
    private static String extractString(String t, String regex) {
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(t);
        if (m.find()) return m.group(1);
        return "";
    }
    private static String extractStringOptional(String t, String regex) {
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(t);
        if (m.find()) {
            if (m.group(1).equals("null")) return "null";
            if (m.groupCount() >= 2) return m.group(2) == null ? "" : m.group(2);
        }
        return "";
    }
}
