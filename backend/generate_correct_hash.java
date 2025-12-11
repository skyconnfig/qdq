import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Generate correct BCrypt hash for admin123
 * Run: javac -cp ~/.m2/repository/org/springframework/security/spring-security-crypto/*/spring-security-crypto-*.jar generate_correct_hash.java
 * Then: java -cp ~/.m2/repository/org/springframework/security/spring-security-crypto/*/spring-security-crypto-*jar:. generate_correct_hash
 */
public class generate_correct_hash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "admin123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("");
        System.out.println("SQL UPDATE statement:");
        System.out.println("UPDATE sys_user SET password_hash = '" + hash + "', password_plain = '" + password + "' WHERE username = 'admin';");
    }
}
