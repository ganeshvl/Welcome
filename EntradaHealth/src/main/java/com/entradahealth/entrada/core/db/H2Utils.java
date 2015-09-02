package com.entradahealth.entrada.core.db;

import java.io.File;
import java.sql.*;
import java.util.Random;

/**
 * Helper class for managing database access using H2. This uses the BoneCP
 * connection pool because it'll keep the H2 database alive and unencrypted in
 * memory (to avoid blocking on the database creation for ~200ms every time we
 * need a new connection).
 *
 * @author edr
 * @since 28 Aug 2012
 */
public class H2Utils
{
    private H2Utils() { }

    private static Random _random = new Random();

    /**
     * Opens a database connection to the passworded H2 database stored at the
     * given path.
     *
     * @param path The file path to the database.
     * @param password The password used to decrypt the database.
     * @return A connection pool to the requested database.
     */
    public static Connection openEncryptedDatabase(File path, String password)
    {
        try
        {
            Class.forName("org.h2.Driver");
            StringBuilder sb = new StringBuilder("jdbc:h2:file:")
                    .append(path)
                    .append(";CIPHER=AES")
                    .append(";FILE_LOCK=NO");

            String userPass = password != null ? password + " nopasswd" : "nopasswd";

            return DriverManager.getConnection(sb.toString(), "sa", userPass);
        }
        catch (Exception e)
        {
            throw new H2SetupException(e);
        }
    }

    /**
     * Opens a connection to an unpassworded H2 database.
     *
     * @param path The path to the H2 database.
     * @return A JDBC connection pool to the requested database.
     */
    public static Connection openDatabase(File path)
    {
        return openEncryptedDatabase(path, null);
    }

    /**
     * Creates a connection to an in-memory H2 database. If the requested
     * database name is currently in use by any other connection, the database
     * is re-used.
     * @param databaseName The name of the memory database.
     * @return A connection pool representing the requested database.
     */
    public static Connection createMemoryDatabasePool(String databaseName)
    {
        try
        {
            Class.forName("org.h2.Driver");

            return DriverManager.getConnection("jdbc:h2:mem:" + databaseName,
                                               "SA", "nopasswd");
        }
        catch (Exception e)
        {
            throw new H2SetupException(e);
        }
    }


	public static void close(Statement stmt) 
	{ try 
        {
			if (stmt != null && !stmt.isClosed()) 
				stmt.close(); 
			} catch (SQLException e) { // swallowingthe exception here is safe 
				
        }
    }

    public static void close(ResultSet rs)
    {
		try { 
			if (rs != null && !rs.isClosed()) 
				rs.close(); 
			} catch (SQLException e) { // swallowing the exception here is safe 

        }
        }
	

	/*
	 * public static void close(Connection conn) { try { if (conn != null &&
	 * !conn.isClosed()) conn.close(); } catch (SQLException e) { // swallowing
	 * the exception here is safe } }
	 * 
	 * public static void close(ResultSet rs) { try { if (rs != null &&
	 * !rs.isClosed()) rs.close(); } catch (SQLException e) { // swallowing the
	 * exception here is safe } }
	 * 
	 * public static void close(Statement stmt) { try { if (stmt != null &&
	 * !stmt.isClosed()) stmt.close(); } catch (SQLException e) { // swallowing
	 * the exception here is safe } }
	 */
}
