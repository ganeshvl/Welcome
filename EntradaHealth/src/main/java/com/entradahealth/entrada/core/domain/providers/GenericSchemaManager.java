package com.entradahealth.entrada.core.domain.providers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import android.content.res.AssetManager;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.core.domain.exceptions.SchemaException;
import com.google.common.io.CharStreams;

public class GenericSchemaManager {
    public static final String LOG_TITLE = "Entrada.SchemaManager";
    public static final long SCHEMA_REVISION = 0;

    Connection _conn;
    public GenericSchemaManager(Connection connection) {
        _conn = connection;
    }

    void updateSchema() throws SchemaException, SQLException {
        AssetManager assets = EntradaApplication.getAppContext().getAssets();

        long rev = 0;
        Savepoint schemaBegin = null;
        boolean autocommit = true;
        try {
            autocommit = _conn.getAutoCommit();
            _conn.setAutoCommit(false);
            schemaBegin = _conn.setSavepoint();

            PreparedStatement stmt = null;
            long currentRev = -1;

            try {
                stmt = _conn.prepareStatement("SELECT * FROM schemaversion;");
                ResultSet res = stmt.executeQuery();
                res.next();
                currentRev = res.getLong(1);
                stmt.close();
            }
            catch (SQLException ex) {
                // Schema isn't initialized. Leave currentRev at -1.
                // This will also happen for pre-SchemaManager databases.
                // V0000 is safe to run on these databases, and will upgrade them to
                // SchemaManager databases.
                Log.d(LOG_TITLE, "Initializing new schema.");
            }

            for (rev = currentRev + 1; rev <= SCHEMA_REVISION; ++rev) {
                String resourcePath = String.format("db/migration/VER%04d.sql", rev);
                InputStream resource = assets.open(resourcePath);
                String query;

                if (resource == null) {
                    throw new SchemaException(rev, String.format("No such resource %s", resourcePath));
                }

                try {
                    query = CharStreams.toString(new InputStreamReader(resource));
                } catch (IOException e) {
                    throw new SchemaException(rev, String.format("Unable to read from resource %s", resourcePath), e);
                }

                Log.d(LOG_TITLE, String.format("Updating schema to revision %d", rev));
                stmt = _conn.prepareStatement(query);
                try {
                    stmt.execute();
                } finally {
                    stmt.close();
                }

                stmt = _conn.prepareStatement("UPDATE schemaversion SET VersionID=?;");
                try {
                    stmt.setLong(1, rev);
                    stmt.executeUpdate();
                } finally {
                    stmt.close();
                }

            }

            _conn.commit();
        }
        catch (SchemaException ex) {
            _conn.rollback(schemaBegin);
            Log.e(LOG_TITLE, ex.toString());
            throw ex;
        }
        catch (SQLException ex) {
            //_conn.rollback(schemaBegin);
            throw ex;
        } catch (Exception e) {
            _conn.rollback(schemaBegin);
            throw new SchemaException(rev, "Unknown error. See inner exception.", e);
        } finally {
            _conn.setAutoCommit(autocommit);
        }
    }
}
