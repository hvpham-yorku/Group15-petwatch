package com.petwatch.petwatch.DAO;
import com.petwatch.petwatch.Model.PetOwner;
import com.petwatch.petwatch.Model.User;
import java.sql.*;

public class UserDAO {

    private static final String URL = "jdbc:sqlite:petwatch.db";
    private Connection connection;
    Statement statement;

    //Establishes connection to the SQLite database
    public UserDAO() {
        try {
            connection = DriverManager.getConnection(URL);
            statement = connection.createStatement();
            statement.execute("PRAGMA foreign_keys = ON;");
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {

        }
    }

    /**
     * Adds a User to the User table in the database
     *
     * @param user
     * @return userId
     */
    public int addUser(User user) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return -1;
        }

        String sql = "INSERT INTO users (email, password, role) VALUES ('" +
                user.getEmail() + "', '" + user.getPassword() + "', '" + user.getRole().name() + "')";
        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            // Retrieve the generated ID and assign it to the user object
            if (affectedRows > 0) {
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    user.setId(rs.getInt(1));  // Assign generated ID
                    System.out.println("User added successfully with ID: " + user.getId());
                }

            }
            statement.close();
        } catch (SQLException e) {

        }

        return user.getId();

    }

    /**
     * Removes User from the database using Id
     *
     * @param userId
     */
    public void removeUser(int userId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return;
        }

        String sql = "DELETE FROM users WHERE id = " + userId;

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows > 0) {
                System.out.println("User removed successfully.");
            } else {
                System.out.println("No User found with ID " + userId);
            }
            statement.close();
        } catch (SQLException e) {

        }
    }

    /**
     * Searches the table in the database for a specific user matching the ID
     *
     * @param userId
     * @return User
     */
    public User getUserById(int userId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }

        String sql = "SELECT * FROM users WHERE id = " + userId;
        User user = null;

        try {

            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);


            if (rs.next()) {
                 user = new User(
                         rs.getInt("id"),
                         rs.getString("email"),
                         rs.getString("password"),
                         User.Role.valueOf(rs.getString("role"))
                 );
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving User: " + e.getMessage());
        }

        return user; // Returns null if not found
    }

    public User getUserByEmail(String email) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }

        String sql = "SELECT * FROM users WHERE email = '" + email + "'";
        User user = null;

        try {

            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);


            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        User.Role.valueOf(rs.getString("role"))
                );
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving User: " + e.getMessage());
        }

        return user; // Returns null if not found
    }

    public String getPasswordByEmail(String email){
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }

        String password = null;

        String sql = "SELECT * FROM users WHERE email = '" + email + "'";

        try {

            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                password = rs.getString("password");
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving User: " + e.getMessage());
        }

        return password;
    }


    // closes the connection when we are done with it
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {

        }
    }

}
