package model;

/** The password rules, shared by sign-up and Change Password. */
public final class PasswordRules {
    public static final int MIN_LENGTH = 8;

    private static final String[] PROBLEMS = {
            "Password must be at least 8 characters.",
            "Password needs at least one uppercase letter.",
            "Password needs at least one lowercase letter.",
            "Password needs at least one number."};
    private static final String[] HINTS = {"8 or more characters", "an uppercase letter", "a lowercase letter", "a number"};

    private PasswordRules() { }

    /** Index of the first rule that is not met, or -1 if the password is acceptable. */
    private static int firstMissing(String p) {
        if (p == null || p.length() < MIN_LENGTH) return 0;
        if (p.chars().noneMatch(Character::isUpperCase)) return 1;
        if (p.chars().noneMatch(Character::isLowerCase)) return 2;
        if (p.chars().noneMatch(Character::isDigit)) return 3;
        return -1;
    }

    /** An error message for a password that is too weak, or null if it is acceptable. */
    public static String problem(String password) {
        int m = firstMissing(password);
        return m < 0 ? null : PROBLEMS[m];
    }

    /** What to add next, e.g. "a number", or null if the password is acceptable. */
    public static String hint(String password) {
        int m = firstMissing(password);
        return m < 0 ? null : HINTS[m];
    }

    /** "", "Weak", "Fair", "Good" or "Strong". */
    public static String strength(String p) {
        if (p == null || p.isEmpty()) return "";
        int points = 0;
        if (p.length() >= MIN_LENGTH) points++;
        if (p.chars().anyMatch(Character::isUpperCase)) points++;
        if (p.chars().anyMatch(Character::isLowerCase)) points++;
        if (p.chars().anyMatch(Character::isDigit)) points++;
        if (p.chars().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))) points++;
        if (points <= 2) return "Weak";
        if (points == 3) return "Fair";
        return points == 4 ? "Good" : "Strong";
    }
}
