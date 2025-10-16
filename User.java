import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class User {
    private final int id;
    private final String name;
    private final String email;

    public User(int id, String name, String email) { this.id = id; this.name = name == null ? "" : name; this.email = email; }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return String.format("UserID:%d | %s | %s", id, name, email == null ? "" : email);
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"id\": ").append(id).append(",\n");
        sb.append("  \"name\": \"").append(escape(name)).append("\",\n");
        sb.append("  \"email\": ").append(email == null ? "null" : "\"" + escape(email) + "\"").append("\n");
        sb.append("}");
        return sb.toString();
    }

    public static User fromJson(String text) {
        int id = extractInt(text, "\"id\"\\s*:\\s*(\\d+)");
        String name = extractString(text, "\"name\"\\s*:\\s*\"(.*?)\"");
        String email = extractStringOptional(text, "\"email\"\\s*:\\s*(null|\"(.*?)\")");
        if (id == -1) return null;
        if ("null".equals(email)) email = null;
        else email = unescape(email);
        return new User(id, unescape(name), email);
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

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
