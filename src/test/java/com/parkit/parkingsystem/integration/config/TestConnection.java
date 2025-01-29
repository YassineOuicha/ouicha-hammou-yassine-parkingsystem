import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;

public static void main(String[] args) {
    try {
        Connection con = new DataBaseTestConfig().getConnection();
        if (con != null) {
            System.out.println("Connexion established");
        }
        con.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
