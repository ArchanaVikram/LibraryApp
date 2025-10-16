import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Book {
    private final int id;
    private final String title;
    private final String author;
    private int availableCopies;
    private final int totalCopies;

    public Book(int id, String title, String author, int availableCopies, int totalCopies) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.author = author == null ? "" : author;
        this.availableCopies = availableCopies;
        this.totalCopies = totalCopies;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getAvailableCopies() { return availableCopies; }
    public int getTotalCopies() { return totalCopies; }

    public void decrementAvailable() { if (availableCopies > 0) availableCopies--; }
    public void incrementAvailable() { if (availableCopies < totalCopies) availableCopies++; }

    @Override
    public String toString() {
        return String.format("ID:%d | %s by %s | Avail:%d/%d", id, title, author, availableCopies, totalCopies);
    }

    // JSON
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"id\": ").append(id).append(",\n");
        sb.append("  \"title\": \"").append(escape(title)).append("\",\n");
        sb.append("  \"author\": \"").append(escape(author)).append("\",\n");
        sb.append("  \"availableCopies\": ").append(availableCopies).append(",\n");
        sb.append("  \"totalCopies\": ").append(totalCopies).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public static Book fromJson(String text) {
        int id = extractInt(text, "\"id\"\\s*:\\s*(\\d+)");
        String title = extractString(text, "\"title\"\\s*:\\s*\"(.*?)\"");
        String author = extractString(text, "\"author\"\\s*:\\s*\"(.*?)\"");
        int avail = extractInt(text, "\"availableCopies\"\\s*:\\s*(\\d+)");
        int total = extractInt(text, "\"totalCopies\"\\s*:\\s*(\\d+)");
        if (id == -1) return null;
        return new Book(id, unescape(title), unescape(author), avail, total);
    }

    // helpers
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

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
