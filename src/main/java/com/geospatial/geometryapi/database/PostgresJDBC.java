package com.geospatial.geometryapi.database;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import com.geospatial.geometryapi.hexagon.Coordinates;
import com.geospatial.geometryapi.hexagon.Hexagon;
import com.geospatial.geometryapi.index.CubeCoordinatesIndex;
import com.geospatial.geometryapi.tessellation.Boundary;
import com.geospatial.geometryapi.tessellation.CornerEdgeTessellation;
import com.geospatial.geometryapi.utils.JVMUtils;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.ToString;

@ToString
public class PostgresJDBC {
        private static final String DBMS_URL = "jdbc:postgresql:";
        private final String pgjdbcUrl;
        @Getter
        private final String host;
        @Getter
        private final int port;
        @Getter
        private final String database;
        @ToString.Exclude
        private final Properties properties;

        public PostgresJDBC(Builder builder) {
                this.host = builder.host;
                this.port = builder.port;
                this.database = builder.database;
                this.properties = builder.properties;
                this.pgjdbcUrl = generateJDBCUrl();
        }

        /* Public methods */
        public final Connection getConnection() {
                Connection connection = null;
                try {
                        connection = DriverManager.getConnection(pgjdbcUrl, properties);
                        System.out.println("\n"
                                        + "Connected to the PostgreSQL server as user: "
                                        + connection.getMetaData().getUserName());
                } catch (SQLException e) {
                        printSQLException(e);
                }

                return connection;
        }

        public final void testQuery(String tableName, int rowsLimit) {
                final String SQL_TEMPLATE = new StringBuilder()
                                .append("SELECT * FROM %s\n")
                                .append("LIMIT %s")
                                .toString();
                final String sql = String.format(SQL_TEMPLATE, tableName, rowsLimit);

                try (final Connection connection = getConnection();
                                final Statement statement = connection.createStatement();
                                final ResultSet rs = statement.executeQuery(sql)) {

                        final ResultSetMetaData rsMetadata = rs.getMetaData();
                        final int columnsCount = rsMetadata.getColumnCount();

                        System.out.println("--- Query results");

                        // Iterate through the data in the result set and display it.
                        while (rs.next()) {
                                // Print one row
                                for (int i = 1; i <= columnsCount; i++) {
                                        String tabs;
                                        if (rsMetadata.getColumnName(i).length() < 8) {
                                                tabs = "\t\t\t";
                                        } else {
                                                tabs = "\t\t";
                                        }

                                        System.out.print(String.format("(%s) ", rsMetadata.getColumnTypeName(i)));
                                        System.out.print(rsMetadata.getColumnName(i) + tabs + ": ");
                                        System.out.print(rs.getString(i) + "\n");
                                }

                                System.out.println("--------------------------------");
                        }

                } catch (SQLException e) {
                        printSQLException(e);
                }
        }

        public final void createTessellationTable(String tableName) {
                final String sql = """
                                CREATE TABLE IF NOT EXISTS %s (
                                        ccid_q          integer                         NOT NULL,
                                        ccid_r          integer                         NOT NULL,
                                        ccid_s          integer                         NOT NULL,
                                        circumradius    float8                          NOT NULL,
                                        centroid        geometry(POINT, 4326)           NOT NULL,
                                        geometry        geometry(POLYGON, 4326)         NOT NULL
                                );
                                """;

                try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                        String createTessellationTableQuery = String.format(sql, tableName);
                        statement.executeUpdate(createTessellationTableQuery);
                        System.out.println("Executed createTessellationTable query successfully.");
                } catch (SQLException e) {
                        printSQLException(e);
                }
        }

        public final void batchInsertTessellation(String tableName, CornerEdgeTessellation tessellation) {
                // Get hexagons
                final List<Hexagon> hexagons = tessellation.getGisHexagons();

                // Prepare SQL
                final String insertTessellationQuery = String.format("""
                                INSERT INTO %s (ccid_q, ccid_r, ccid_s, circumradius, centroid, geometry)
                                VALUES (?,
                                        ?,
                                        ?,
                                        ?,
                                        ST_SetSRID(ST_MakePoint(?, ?), 4326),
                                        ST_SetSRID(ST_MakePolygon(ST_MakeLine(ARRAY[
                                                        ST_MakePoint(?, ?),
                                                        ST_MakePoint(?, ?),
                                                        ST_MakePoint(?, ?),
                                                        ST_MakePoint(?, ?),
                                                        ST_MakePoint(?, ?),
                                                        ST_MakePoint(?, ?),
                                                        ST_MakePoint(?, ?)
                                                ])), 4326));
                                """, tableName);

                // Prepare dynamic queries to insert Hexagons to PostGIS
                try (Connection connection = getConnection();
                                PreparedStatement preparedStatement = connection
                                                .prepareStatement(insertTessellationQuery)) {
                        // Set autocommit off
                        connection.setAutoCommit(false);

                        // JDBC batch configurations
                        final int BATCH_SIZE_LIMIT = 5000;

                        // Counters
                        int batchCount = 0;
                        int batchExecutionCount = 0;

                        // Start time
                        System.out.println("--- Batch execution begin..");
                        final long startTime = System.currentTimeMillis();

                        for (Hexagon hexagon : hexagons) {
                                CubeCoordinatesIndex cci = hexagon.getCCI();
                                preparedStatement.setInt(1, cci.getQ());
                                preparedStatement.setInt(2, cci.getR());
                                preparedStatement.setInt(3, cci.getS());

                                double circumradius = hexagon.getCircumradius();
                                preparedStatement.setDouble(4, circumradius);

                                Coordinates centroid = hexagon.getCentroid();
                                preparedStatement.setDouble(5, centroid.getLongitude());
                                preparedStatement.setDouble(6, centroid.getLatitude());

                                List<Coordinates> gisVertices = hexagon.getGisVertices();
                                preparedStatement.setDouble(7, gisVertices.get(0).getLongitude());
                                preparedStatement.setDouble(8, gisVertices.get(0).getLatitude());

                                preparedStatement.setDouble(9, gisVertices.get(1).getLongitude());
                                preparedStatement.setDouble(10, gisVertices.get(1).getLatitude());

                                preparedStatement.setDouble(11, gisVertices.get(2).getLongitude());
                                preparedStatement.setDouble(12, gisVertices.get(2).getLatitude());

                                preparedStatement.setDouble(13, gisVertices.get(3).getLongitude());
                                preparedStatement.setDouble(14, gisVertices.get(3).getLatitude());

                                preparedStatement.setDouble(15, gisVertices.get(4).getLongitude());
                                preparedStatement.setDouble(16, gisVertices.get(4).getLatitude());

                                preparedStatement.setDouble(17, gisVertices.get(5).getLongitude());
                                preparedStatement.setDouble(18, gisVertices.get(5).getLatitude());

                                preparedStatement.setDouble(19, gisVertices.get(6).getLongitude());
                                preparedStatement.setDouble(20, gisVertices.get(6).getLatitude());

                                // Add statement into batch
                                preparedStatement.addBatch();
                                batchCount++;

                                // Commit to DB every BATCH_SIZE (default: 1000)
                                if (batchCount % BATCH_SIZE_LIMIT == 0) {
                                        try {
                                                preparedStatement.executeBatch();
                                                connection.commit();
                                                batchExecutionCount++;
                                                System.out.println("- Batch " + batchExecutionCount + "th");
                                        } catch (SQLException e) {
                                                connection.rollback();
                                                printSQLException(e);
                                        }
                                }
                        }

                        /*
                         * Set auto-commit back to normal and execute left over batches (batch amount <
                         * JDBC_BATCH_LIMIT)
                         */
                        connection.setAutoCommit(true);
                        preparedStatement.executeBatch();
                        batchExecutionCount++;
                        System.out.println("- Batch " + batchExecutionCount + "th (final) executed.");

                        // End time when finished batch inserts
                        final long endTime = System.currentTimeMillis();

                        // Calculate elapsed time of batch execution
                        final double elapsedMillisecs = endTime - startTime;
                        final double elapsedSeconds = elapsedMillisecs / 1000;

                        System.out.println("\n--- Batch Insert Tessellation Results");
                        System.out.println("Table name             : " + tableName);
                        System.out.println("Total hexagons         : " + hexagons.size());
                        System.out.println("Total batch executions : " + batchExecutionCount);
                        System.out.println("Elapsed time           : " + elapsedSeconds + " s");
                        System.out.println("Hexagons per batch     : " + BATCH_SIZE_LIMIT);
                        System.out.println("Hexagons inserted      : " + batchCount);

                } catch (BatchUpdateException batchUpdateException) {
                        printBatchUpdateException(batchUpdateException);
                } catch (SQLException e) {
                        printSQLException(e);
                }
        }

        public final void addPrimaryKeyIfNotExists(String tableName) {
                final String checkPrimaryKeySQL = String.format("""
                                SELECT constraint_name from information_schema.table_constraints
                                WHERE table_name = '%s'
                                AND constraint_type = 'PRIMARY KEY'
                                """, tableName);
                final String addPrimaryKeySQL = String.format("""
                                ALTER TABLE %s
                                ADD PRIMARY KEY (ccid_q, ccid_r, ccid_s)
                                """, tableName);

                try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                        ResultSet rs = statement.executeQuery(checkPrimaryKeySQL);
                        
                        boolean hasPrimaryKey = rs.next(); // Return `false` if there is no more row (means no PK)
                        if (hasPrimaryKey) {
                                // Query PRIMARY KEY name
                                String constraintName = rs.getString("constraint_name");
                                System.out.println(String.format(
                                                "PRIMARY KEY '%s' already exists for table '%s'.",
                                                constraintName,
                                                tableName));
                        } else {
                                statement.executeUpdate(addPrimaryKeySQL);
                                rs = statement.executeQuery(checkPrimaryKeySQL);
                                rs.next();

                                System.out.println(String.format(
                                                "PRIMARY KEY '%s' added to table '%s'.",
                                                rs.getString("constraint_name"),
                                                tableName));
                        }
                } catch (SQLException e) {
                        printSQLException(e);
                }
        }

        public static void printBatchUpdateException(BatchUpdateException b) {
                System.err.println("--- BatchUpdateException");
                System.err.println("SQLState: \t" + b.getSQLState());
                System.err.println("Message: \t" + b.getMessage());
                System.err.println("Vendor: \t" + b.getErrorCode());

                System.err.println("Update counts: \t");
                int[] updateCounts = b.getUpdateCounts();
                for (int i = 0; i < updateCounts.length; i++) {
                        System.err.print(updateCounts[i] + "   ");
                }
        }

        /* Private methods */
        private final String generateJDBCUrl() {
                final StringBuilder urlBuilder = new StringBuilder().append(DBMS_URL);

                if (this.host == null) {
                        // No host
                        if (this.database == null) {
                                // jdbc:postgresql:/
                                urlBuilder.append("/");
                        } else {
                                // jdbc:postgresql:database
                                urlBuilder.append(this.database);
                        }

                } else {
                        // jdbc:postgresql://host
                        urlBuilder.append("//").append(this.host);

                        if (this.database == null) {
                                /* No port */
                                if (this.port == 0) {
                                        // jdbc:postgresql://host/
                                        urlBuilder.append("/");
                                } else {
                                        // jdbc:postgresql://host:port/
                                        urlBuilder.append(":").append(this.port)
                                                        .append("/");
                                }
                        } else {
                                /* There is port */
                                if (this.port == 0) {
                                        // jdbc:postgresql://host/database
                                        urlBuilder.append("/").append(this.database);
                                } else {
                                        // jdbc:postgresql://host:port/database
                                        urlBuilder.append(":").append(this.port)
                                                        .append("/").append(this.database);
                                }
                        }
                }

                return urlBuilder.toString();
        }

        public static void printSQLException(SQLException exception) {
                for (Throwable e : exception) {
                        if (e instanceof SQLException) {
                                e.printStackTrace(System.err);
                                System.err.println(System.lineSeparator());
                                System.err.println("SQLState:\t" + ((SQLException) e).getSQLState());
                                System.err.println("Error Code:\t" + ((SQLException) e).getErrorCode());
                                System.err.println("Message:\t" + e.getMessage());
                                Throwable t = exception.getCause();
                                while (t != null) {
                                        System.out.println("Cause:\t\t" + t);
                                        t = t.getCause();
                                }
                        }
                }
        }

        /* PostgresJDBC Builder */
        public static class Builder {
                private String host;
                private int port;
                private String database;
                private Properties properties = new Properties();

                private final Dotenv dotenv = Dotenv.configure()
                                // .directory("./geometryapi")
                                .filename(".env")
                                .load();

                public Builder() {
                }

                public final Builder host(String hostKey) {
                        this.host = dotenv.get(hostKey);
                        return this;
                }

                public final Builder port(int port) {
                        this.port = port;
                        return this;
                }

                public final Builder database(String database) {
                        this.database = database;
                        return this;
                }

                public final Builder authentication(String usernameKey, String passwordKey) {
                        final String username = dotenv.get(usernameKey);
                        final String password = dotenv.get(passwordKey);

                        this.properties.setProperty("user", username);
                        this.properties.setProperty("password", password);

                        return this;
                }

                public final Builder reWriteBatchedInserts(boolean isEnabled) {
                        this.properties.setProperty("reWriteBatchedInserts", Boolean.toString(isEnabled));
                        return this;
                }

                public final PostgresJDBC build() {
                        return new PostgresJDBC(this);
                }
        }

        /* Test */
        public static void main(String[] args) {
                PostgresJDBC pg = new PostgresJDBC.Builder()
                                .host("POSTGRES_HOST")
                                .port(5432)
                                .database("spatial_dwh")
                                .authentication("POSTGRES_USERNAME", "POSTGRES_PASSWORD")
                                .reWriteBatchedInserts(true)
                                .build();

                /* Vietnam */
                final Boundary vn_boundary = new Boundary(
                                new Coordinates(102.133333, 8.033333),
                                new Coordinates(109.466667, 23.383333));

                // Still missing some wards at the top, check missing_wards.csv
                final Boundary oct_17_vn_boundary = new Boundary(
                                new Coordinates(102.050278, 23.583612),
                                new Coordinates(109.666945, 8));

                final Coordinates vn_centroid = new Coordinates(106, 15);

                /*
                 * Vietnam - Nominatim OpenStreetMap
                 * - URL :
                 * https://nominatim.openstreetmap.org/ui/details.html?osmtype=R&osmid=49915
                 * - ID : R49915
                 */
                final Coordinates vn_min_coords_osm = new Coordinates(102, 8);
                final Coordinates vn_max_coords_osm = new Coordinates(109.9, 23.5);
                final Coordinates vn_centroid_osm = new Coordinates(107.9650855, 15.9266657);
                final Boundary vn_boundary_osm = new Boundary(vn_min_coords_osm, vn_max_coords_osm);

                final Coordinates hcm_centroid_osm = new Coordinates(106.7011391, 10.7763897);
                final Boundary hcm_boundary_osm = new Boundary(
                                new Coordinates(106.35667121999998, 10.35422636000001),
                                new Coordinates(107.02750646000003, 11.160309929999999));

                /*
                 * Vietnam - spatial_db
                 * - database : spatial_db
                 * - table : vietnam_border
                 */
                final Coordinates vn_min_coords_internal = new Coordinates(102.14458466, 7.39143848);
                final Coordinates vn_max_coords_internal = new Coordinates(117.81734467, 23.39243698);

                final Coordinates vn_centroid_internal = new Coordinates(106.4063821609223, 16.57755915233502);
                final Boundary vn_boundary_internal = new Boundary(vn_min_coords_internal, vn_max_coords_internal);

                /* Ho Chi Minh City */
                final Coordinates hcm_min_coords = new Coordinates(106.35667121999998, 10.35422636000001);
                final Coordinates hcm_max_coords = new Coordinates(107.02750646000003, 11.160309929999999);

                final Coordinates hcm_centroid = new Coordinates(106.70475886133208, 10.73530289102618);
                final Boundary hcm_boundary = new Boundary(hcm_min_coords, hcm_max_coords);

                /* Ha Noi City */
                final Coordinates hanoi_min_coords = new Coordinates(105.28813170999999, 20.564474110000003);
                final Coordinates hanoi_max_coords = new Coordinates(106.02005767999997, 21.385208129999985);

                final Coordinates hanoi_centroid = new Coordinates(105.700030001506, 20.998981122751463);
                final Boundary hanoi_boundary = new Boundary(hanoi_min_coords, hanoi_max_coords);


                // Tessellation configurations
                final int circumradius = 1000;
                final Coordinates centroid = vn_centroid_internal;
                final Boundary boundary = vn_boundary_internal;

                // Don't modify this
                final Hexagon hexagon = new Hexagon(centroid, circumradius);
                final CornerEdgeTessellation tessellation = new CornerEdgeTessellation(hexagon);
                tessellation.tessellate(boundary);

                // Database table name formats
                final String TABLE_NAME_TEMPLATE = "%s_tessellation_%sm_test";

                // Database configurations
                System.out.println("\n------ Database configs ------");
                final String table_name = String.format(TABLE_NAME_TEMPLATE,
                                "vietnam",
                                circumradius);
                System.out.println("Table name: " + table_name);

                pg.createTessellationTable(table_name);
                pg.batchInsertTessellation(table_name, tessellation);
                pg.addPrimaryKeyIfNotExists(table_name);
                JVMUtils.printMemoryUsages("MB");

                // Test query
                pg.testQuery(table_name, 5);
        }
}