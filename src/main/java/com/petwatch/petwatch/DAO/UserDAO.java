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

<<<<<<< Updated upstream
=======
    /**
     * Gets the pet owner ID for a given user ID
     *
     * @param userId The user ID to look up
     * @return The pet owner ID, or -1 if not found or on error
     */
    public int getPetOwnerIdForUser(int userId) {
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return -1;
        }

        String sql = "SELECT id FROM pet_owners WHERE user_id = " + userId;
        int petOwnerId = -1;

        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                petOwnerId = rs.getInt("id");
                System.out.println("Found pet owner with ID: " + petOwnerId + " for user ID: " + userId);
            } else {
                System.out.println("No pet owner found for user ID: " + userId);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving pet owner: " + e.getMessage());
        }

        return petOwnerId;
    }

    /**
     * Creates a pet owner profile for a given user
     *
     * @param userId The user ID to create the pet owner for
     * @param name The name for the pet owner (can be email if no real name available)
     * @param phone The phone number for the pet owner
     * @param address The address for the pet owner
     * @return The new pet owner ID, or -1 on error
     */
    public int createPetOwnerForUser(int userId, String name, String phone, String address) {
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return -1;
        }

        String sql = "INSERT INTO pet_owners (user_id, name, phone, address) VALUES (" +
                userId + ", '" + name + "', '" + phone + "', '" + address + "')";

        int petOwnerId = -1;

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            if (affectedRows > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    petOwnerId = rs.getInt(1);
                    System.out.println("Created pet owner profile with ID: " + petOwnerId);
                }
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error creating pet owner: " + e.getMessage());
            System.err.println("SQL: " + sql);
            e.printStackTrace();
        }

        return petOwnerId;
    }
>>>>>>> Stashed changes

    public void updateEmail(int userId, String newEmail) {
        String sql = "UPDATE users SET email = '" + newEmail + "' WHERE id = " + userId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = '" + newPassword + "' WHERE id = " + userId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
