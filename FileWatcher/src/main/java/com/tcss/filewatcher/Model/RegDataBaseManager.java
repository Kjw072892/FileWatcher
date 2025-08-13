package com.tcss.filewatcher.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.SQLiteDataSource;

public class RegDataBaseManager {

    /**
     * The SQLite data source for database connections.
     */
    private SQLiteDataSource myDs;

     /**
     * The JDBC URL for the SQLite database file.
     */
    private static final String DB_URL = "jdbc:sqlite:filewatcher.db";

    /**
     * Constructs a new RegDataBaseManager, initializing the database and creating the table
     * if needed.
     */
    public RegDataBaseManager() {
        initializeDatabase();
        createTable();
    }

    /**
     * Initializes the SQLite data source and sets the database URL.
     * Throws a RuntimeException if initialization fails.
     */
    private void initializeDatabase() {
        try {
            myDs = new SQLiteDataSource();
            myDs.setUrl(DB_URL);
            System.out.println("Database connection established successfully");
        } catch (Exception e) {
            System.out.println("invalid");
            throw new RuntimeException("Failed to initialize database: ", e);
        }
    }

    /**
     * Creates the 'registration' table in the database if it does not already exist.
     * Throws a RuntimeException if table creation fails.
     */
    private void createTable() {
        String query = """
                CREATE TABLE IF NOT EXISTS registration (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL,
                password TEXT NOT NULL,
                email_freq TEXT NOT NULL
                )""";


        try (final Connection conn = myDs.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            System.out.println("Table 'registration' initialized.");
        } catch (SQLException theE) {
            System.out.println("Error creating table: " + theE.getMessage());
            throw new RuntimeException("Failed to create table: ", theE);
        }
    }

    /**
     * Inserts new Registered user into the database.
     *
     * @param theEmail the email address of the new user.
     * @param thePassword the user's password.
     * @param theEmailFrequency the Frequency of which they want to receive and email.
     */
    public final void insertNewUserData(final String theEmail, final String thePassword,
                                        final String theEmailFrequency) {
        final String insertSQL = "INSERT INTO registration (email, password, email_freq) " +
                "VALUES (?,?,?)";

        try(final Connection conn = myDs.getConnection();
            final PreparedStatement prepStmt = conn.prepareStatement(insertSQL)) {

            prepStmt.setString(1, theEmail);
            prepStmt.setString(2, thePassword);
            prepStmt.setString(3, theEmailFrequency);
            prepStmt.executeUpdate();

        } catch (final SQLException theE) {
            System.out.println("Error inserting users information: " + theE.getMessage());
        }
    }

    /**
     * Checks if an email address is already registered.
     *
     * @param theEmail the email address that's being checked
     * @return returns true if an email exists, false otherwise.
     */
    public final boolean isExistingUser(final String theEmail) {
        final String query = "SELECT 1 FROM registration WHERE LOWER(email) = LOWER(?)";

        try (final Connection conn = myDs.getConnection();
        PreparedStatement prepStmt = conn.prepareStatement(query)) {
            prepStmt.setString(1, theEmail);

            try(final ResultSet rs = prepStmt.executeQuery()){
                return rs.next();
            }

        } catch(final SQLException theE) {
            System.out.println("Unable to query Database: " + theE);
        }

        return false;
    }

    /**
     * Gets the users email frequency parameter.
     *
     * @param theEmail the users email address.
     * @return returns the string object of the email frequency set by the user.
     */
    public final String getEmailFrequency(final String theEmail) {

        final String query = "SELECT email_freq FROM registration where email = ? COLLATE " +
                "NOCASE";

        try (final Connection conn = myDs.getConnection();
            final PreparedStatement prepStmt = conn.prepareStatement(query)) {
            
            prepStmt.setString(1, theEmail);

        try (final ResultSet rs = prepStmt.executeQuery()) {
            return rs.next() ? rs.getString("email_freq") : null;
        }

        } catch(final SQLException theE) {
            
            System.err.println("Unable to perform query: " + theE);
            
            return null;
        }
    }

    /**
     * Checks the users password based on info saved under their email
     * 
     * @param theEmail the users email address.
     * @param thePassword the users' password.
     * @return true if the password matches what's in the database, false otherwise.
     */
    public final boolean checkPassword(final String theEmail, final String thePassword) {
        
        final String query = "SELECT password FROM registration WHERE email = ? COLLATE " +
                "NOCASE";
        
        try (final Connection conn = myDs.getConnection();
        final PreparedStatement prepStmt = conn.prepareStatement(query)) {
            prepStmt.setString(1, theEmail);
            
            try (final ResultSet rs = prepStmt.executeQuery()) {
                if(!rs.next()) {
                    return false;
                }

                final String stored = rs.getString("password");

                return stored != null && stored.equals(thePassword);
            }
            
        } catch (final SQLException theE) {
            System.err.println("Unable to perform query: " + theE);
            return false;
        }
    }

    /**
     * Checks if a user is already registered. Only allowed 1 admin user.
     * 
     * @return true if a user is registered, false otherwise.
     */
    public final boolean hasAUserAlreadyRegistered() {
        final String userReg = "SELECT EXISTS(SELECT 1 FROM registration)";
        try (final Connection conn = myDs.getConnection();
        final Statement stmt = conn.createStatement();
        final ResultSet rs = stmt.executeQuery(userReg)) {

            return rs.next() && rs.getInt(1) == 1;

        } catch(final SQLException theE) {
            System.err.println("Unable to perform query: " + theE);

            return false;
        }
    }

}
