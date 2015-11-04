import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by MattBrown on 11/4/15.
 */
public class PeopleTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        People.createTables(conn);
        return conn;
    }
    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE people");
        conn.close();
    }
    @Test
    public void testPerson() throws SQLException {
        Connection conn = startConnection();
        People.insertPerson(conn, "Matt", "Brown", "littlriver1337@gmail.com", "Alabama", "91000");
        Person person = People.selectPerson(conn, 1);

        endConnection(conn);

        assertTrue(person != null);
    }
    @Test
    public void testPeople() throws SQLException {
        Connection conn = startConnection();
        People.insertPerson(conn, "Matt", "Brown", "littleriver1337@gmail.com", "Alabama", "91000");
        People.insertPerson(conn, "Lyynard", "Skyynard", "thereckoning@gmail.com", "Arizona", "2900");
        ArrayList<Person> selectPeople = People.selectPeople(conn, 0);
        endConnection(conn);

        assertTrue(selectPeople != null);
    }
}