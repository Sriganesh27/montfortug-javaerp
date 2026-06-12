import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://68.178.237.26:3306/montfortug?useSSL=false&serverTimezone=UTC";
        String username = "montfortu";
        String password = "montfort@123";
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username, role, is_active FROM erp_users")) {
            
            System.out.println("--- USERS IN DATABASE ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Username: " + rs.getString("username") + 
                                   " | Role: " + rs.getString("role") + 
                                   " | Active: " + rs.getInt("is_active"));
            }
            if (!found) {
                System.out.println("NO USERS FOUND IN DATABASE!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
