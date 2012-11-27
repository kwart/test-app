package org.jboss.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Hello world!
 * 
 * @author Josef Cacek
 */
public class App {

    // Constructors ----------------------------------------------------------

    // Public methods --------------------------------------------------------

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        //        final Server server = Server.createTcpServer("-tcpAllowOthers").start();
        Class.forName("org.h2.Driver");
        //        final String dbUrl = "jdbc:h2:mem:PicketLinkTest;DB_CLOSE_DELAY=-1";
        final String dbUrl = "jdbc:h2:tcp://localhost/mem:PicketLinkTest";

        final Connection conn = DriverManager.getConnection(dbUrl, "sa", "sa");
        PreparedStatement ps = conn.prepareStatement("SELECT ? FROM VALUES('1') V");
        ps.setString(1, "Hello");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
        rs.close();
        ps.close();
        conn.close();
        //        server.stop();
    }

    // Protected methods -----------------------------------------------------

    // Private methods -------------------------------------------------------

    // Embedded classes ------------------------------------------------------
}
