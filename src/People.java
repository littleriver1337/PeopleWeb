import org.h2.command.Prepared;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zach on 10/19/15.
 */
public class People {
    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS people");
        stmt.execute("CREATE TABLE people (id IDENTITY, first_name VARCHAR, last_name VARCHAR," +
                " email VARCHAR, country VARCHAR, ip VARCHAR)");
    }
    public static void insertPerson(Connection conn, String firstName, String lastName, String email,
                                    String country, String ip) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, email);
        stmt.setString(4, country);
        stmt.setString(5, ip);
        stmt.execute();
    }
    public static Person selectPerson(Connection conn, int id) throws SQLException {
        Person person = null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if(results.next()){
            person = new Person();
            person.id = results.getInt("id");
            person.firstName = results.getString("first_name");
            person.lastName = results.getString("last_name");
            person.email = results.getString("email");
            person.country = results.getString("country");
            person.ip = results.getString("ip");
        }
        return person;
    }
    public static void populateDatabase(Connection conn) throws SQLException {
        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");

        for (String line : lines) {
            if (line == lines[0])
                continue;

            String[] columns = line.split(",");
            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL, ?, ?, ?, ?, ?)");
            stmt.setString(1, person.firstName);
            stmt.setString(2, person.lastName);
            stmt.setString(3, person.email);
            stmt.setString(4, person.country);
            stmt.setString(5, person.ip);
            stmt.execute();
        }
    }

    public static ArrayList<Person> selectPeople(Connection conn, int offset) throws SQLException {
        ArrayList<Person> select = new ArrayList();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people LIMIT 20 OFFSET ?");
        stmt.setInt(1, offset);
        ResultSet results = stmt.executeQuery();
        while (results.next()){
            Person person = new Person();
            person.id = results.getInt("id");
            person.firstName = results.getString("first_name");
            person.lastName = results.getString("last_name");
            person.email = results.getString("email");
            person.country = results.getString("country");
            person.ip = results.getString("ip");
            select.add(person);
        }
        return select;
    }


    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);
        populateDatabase(conn);

        final int SHOW_COUNT = 20;


        Spark.get(
                "/",
                ((request, response) -> {//anon function
                    String offset = request.queryParams("offset");
                    int offsetNum;
                    if (offset == null){
                        offsetNum = 0;
                    }else {
                        offsetNum = Integer.valueOf(offset);
                    }
                    ArrayList<Person> tempList = selectPeople(conn, offsetNum);

                    HashMap m = new HashMap();
                    m.put("people", tempList);
                    m.put("oldOffset", offsetNum - SHOW_COUNT);
                    m.put("offset", offsetNum + SHOW_COUNT);

                    m.put("showPrevious", true);

                    m.put("showNext", true);
                    return new ModelAndView(m, "people.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(/*Referred to Alex in terms of People template.  I couldn't figure out
                    why my detailed information button was returning the same list for every name
                    ++1 Alex (credit where credit is due)*/
                "/person",
                (request, response) -> {

                    HashMap newM = new HashMap();

                    try {
                        String id = request.queryParams("id");
                        int idNum = Integer.valueOf(id);
                        Person person = selectPerson(conn, idNum);
                        newM.put("person", person);
                    }
                    catch (Exception e) {


                    }
                    return new ModelAndView(newM, "person.html");

                },
                new MustacheTemplateEngine()
        );
    }

    static String readFile(String fileName) {
        File f = new File(fileName);
        try {
            FileReader fr = new FileReader(f);
            int fileSize = (int) f.length();
            char[] fileContent = new char[fileSize];
            fr.read(fileContent);
            return new String(fileContent);
        } catch (Exception e) {
            return null;
        }
    }
}
