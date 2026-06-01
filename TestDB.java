import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/montforterp", "root", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("DESCRIBE erp_students");
            while(rs.next()) {
                System.out.println(rs.getString("Field"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
