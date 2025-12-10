package jrails;

import java.util.*;
import java.sql.*;
import java.lang.reflect.*;
public class Model {
    private int id = 0;
    private static final String DB_FILE = "sample.db";
    private static Connection connection = null;

    /**
     * Returns the id of this model instance.
     */
    public int id() {
        return id;
    }

    /**
     * Gets or creates a database connection.
     */
    private static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the table name for a model class.
     */
    private static String getTableName(Class<?> clazz) {
        return clazz.getSimpleName().toLowerCase();
    }

    /**
     * Gets all @Column fields for a model class.
     */
    private static List<Field> getColumnFields(Class<?> clazz) {
        List<Field> columnFields = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                columnFields.add(field);
            }
        }
        return columnFields;
    }

    /**
     * Gets all @HasMany fields for a model class.
     */
    private static List<Field> getHasManyFields(Class<?> clazz) {
        List<Field> hasManyFields = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(HasMany.class)) {
                hasManyFields.add(field);
            }
        }
        return hasManyFields;
    }

    /**
     * Maps Java type to SQLite type.
     */
    private static String getSQLType(Class<?> type) {
        if (type == String.class) {
            return "TEXT";
        } else if (type == int.class || type == Integer.class) {
            return "INTEGER";
        } else if (type == boolean.class || type == Boolean.class) {
            return "INTEGER";
        } else {
            throw new RuntimeException("Unsupported column type: " + type.getName());
        }
    }

    /**
     * Creates a table for the given model class if it doesn't exist.
     */
    private static void ensureTableExists(Class<?> clazz) {
        try {
            String tableName = getTableName(clazz);
            List<Field> columnFields = getColumnFields(clazz);
            
            StringBuilder createTable = new StringBuilder();
            createTable.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
            createTable.append("id INTEGER PRIMARY KEY");
            
            for (Field field : columnFields) {
                createTable.append(", ");
                createTable.append(field.getName()).append(" ");
                createTable.append(getSQLType(field.getType()));
            }
            
            // Add columns for @HasMany relationships (store comma-separated ids)
            List<Field> hasManyFields = getHasManyFields(clazz);
            for (Field field : hasManyFields) {
                createTable.append(", ");
                createTable.append(field.getName()).append("_ids TEXT");
            }
            
            createTable.append(")");
            
            Connection conn = getConnection();

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTable.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the next available id for a table.
     */
    private static int getNextId(String tableName) {
        try {
            Connection conn = getConnection();

            String query = "SELECT MAX(id) as max_id FROM " + tableName;
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                return maxId + 1;
            }

            return 1;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves this model to the database.
     */
    public void save() {
        try {
            Class<?> clazz = this.getClass();
            String tableName = getTableName(clazz);
            
            ensureTableExists(clazz);
            
            List<Field> columnFields = getColumnFields(clazz);
            List<Field> hasManyFields = getHasManyFields(clazz);
            Connection conn = getConnection();

            
            if (this.id == 0) {
                // Insert new record
                this.id = getNextId(tableName);
                
                StringBuilder insertSQL = new StringBuilder();
                insertSQL.append("INSERT INTO ").append(tableName).append(" (id");
                
                for (Field field : columnFields) {
                    insertSQL.append(", ").append(field.getName());
                }
                for (Field field : hasManyFields) {
                    insertSQL.append(", ").append(field.getName()).append("_ids");
                }
                
                insertSQL.append(") VALUES (?");
                for (int i = 0; i < columnFields.size() + hasManyFields.size(); i++) {
                    insertSQL.append(", ?");
                }
                insertSQL.append(")");
                
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL.toString())) {
                    pstmt.setInt(1, this.id);
                    
                    int paramIndex = 2;
                    for (Field field : columnFields) {
                        setParameter(pstmt, paramIndex++, field, this);
                    }
                    
                    for (Field field : hasManyFields) {
                        String ids = getHasManyIds(field, this);
                        pstmt.setString(paramIndex++, ids);
                    }
                    
                    pstmt.executeUpdate();
                }
            } else {
                // Update existing record
                // First check if record exists
                if (!recordExists(tableName, this.id)) {
                    throw new RuntimeException("Cannot save model with non-zero id that doesn't exist in database");
                }
                
                StringBuilder updateSQL = new StringBuilder();
                updateSQL.append("UPDATE ").append(tableName).append(" SET ");
                
                boolean first = true;
                for (Field field : columnFields) {
                    if (!first) updateSQL.append(", ");
                    updateSQL.append(field.getName()).append(" = ?");
                    first = false;
                }
                for (Field field : hasManyFields) {
                    if (!first) updateSQL.append(", ");
                    updateSQL.append(field.getName()).append("_ids = ?");
                    first = false;
                }
                
                updateSQL.append(" WHERE id = ?");
                
                try (PreparedStatement pstmt = conn.prepareStatement(updateSQL.toString())) {
                    int paramIndex = 1;
                    for (Field field : columnFields) {
                        setParameter(pstmt, paramIndex++, field, this);
                    }
                    
                    for (Field field : hasManyFields) {
                        String ids = getHasManyIds(field, this);
                        pstmt.setString(paramIndex++, ids);
                    }
                    
                    pstmt.setInt(paramIndex, this.id);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a record exists in the database.
     */
    private static boolean recordExists(String tableName, int id) {
        try {
            Connection conn = getConnection();

            String query = "SELECT COUNT(*) as count FROM " + tableName + " WHERE id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count") > 0;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets comma-separated ids from a @HasMany field.
     */
    private static String getHasManyIds(Field field, Object obj) throws IllegalAccessException {
        List<?> list = (List<?>) field.get(obj);
        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder ids = new StringBuilder();
        boolean first = true;

        for (Object o : list) {
            Model item = (Model) o;
            int itemId = item.id();

            if (itemId <= 0) {
                // Skip unsaved models — DO NOT write "0"
                continue;
            }

            if (!first) ids.append(",");
            ids.append(itemId);
            first = false;
        }

        return ids.toString();
    }

    /**
     * Sets a parameter in a prepared statement.
     */
    private static void setParameter(PreparedStatement pstmt, int index, Field field, Object obj) {
        try {
            Class<?> type = field.getType();
            
            if (type == String.class) {
                pstmt.setString(index, (String) field.get(obj));
            } else if (type == int.class || type == Integer.class) {
                pstmt.setInt(index, field.getInt(obj));
            } else if (type == boolean.class || type == Boolean.class) {
                pstmt.setInt(index, field.getBoolean(obj) ? 1 : 0);
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds a model by id.
     */
    public static <T> T find(Class<T> clazz, int id) {
        if (clazz == Model.class) {
            throw new RuntimeException();
        }
        String tableName = getTableName(clazz);
        Connection conn = getConnection();
        try {

            String query = "SELECT * FROM " + tableName + " WHERE id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return materialize(clazz, rs);
                }
            }

            
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Gets all instances of a model class.
     */
    public static <T> List<T> all(Class<T> clazz) {
        try {
            if (clazz == Model.class) {
                throw new RuntimeException("Cannot call all with Model.class");
            }
            
            List<T> results = new ArrayList<>();
            String tableName = getTableName(clazz);
            Connection conn = getConnection();

            
            String query = "SELECT * FROM " + tableName;
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                results.add(materialize(clazz, rs));
            }

            
            return results;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Materializes a model instance from a ResultSet row.
     */
    private static <T> T materialize(Class<T> clazz, ResultSet rs) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            
            // Set the id field
            Field idField = Model.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.setInt(instance, rs.getInt("id"));
            
            // Set @Column fields
            List<Field> columnFields = getColumnFields(clazz);
            for (Field field : columnFields) {
                Class<?> type = field.getType();
                
                if (type == String.class) {
                    field.set(instance, rs.getString(field.getName()));
                } else if (type == int.class || type == Integer.class) {
                    field.setInt(instance, rs.getInt(field.getName()));
                } else if (type == boolean.class || type == Boolean.class) {
                    field.setBoolean(instance, rs.getInt(field.getName()) != 0);
                }
            }
            
            // Set @HasMany fields
            List<Field> hasManyFields = getHasManyFields(clazz);
            for (Field field : hasManyFields) {
                String idsStr = rs.getString(field.getName() + "_ids");
                List<Model> relatedObjects = new ArrayList<>();
                
                if (idsStr != null && !idsStr.isEmpty()) {
                    String[] ids = idsStr.split(",");
                    
                    // Get the type of elements in the List
                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    Class<?> elementClass = (Class<?>) listType.getActualTypeArguments()[0];
                    
                    for (String idStr : ids) {
                        int relatedId = Integer.parseInt(idStr.trim());
                        Object relatedObj = find(elementClass, relatedId);
                        if (relatedObj != null) {
                            relatedObjects.add((Model) relatedObj);
                        }
                    }
                }
                
                field.set(instance, relatedObjects);
            }
            
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Destroys this model (removes it from the database).
     */
    public void destroy() {
        try {
            if (this.id == 0) {
                throw new RuntimeException();
            }
            
            Class<?> clazz = this.getClass();
            String tableName = getTableName(clazz);
            Connection conn = getConnection();

            
            String deleteSQL = "DELETE FROM " + tableName + " WHERE id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                pstmt.setInt(1, this.id);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    throw new RuntimeException("Cannot destroy a model that doesn't exist in the database");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Resets the entire database (deletes all tables and data).
     */

    public static void reset() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = null;

            java.io.File f = new java.io.File(DB_FILE);
            if (f.exists()) f.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void sample() {
        // Sample code, slightly modified from https://github.com/xerial/sqlite-jdbc
        // demonstrating sqlite
        // NOTE: Connection and Statement are AutoCloseable.
        // Don't forget to close them both in order to avoid leaks.
        try {
            // create a database connection
            Connection conn = getConnection();

            Statement statement = conn.createStatement();
            statement.setQueryTimeout(5); // set timeout to 5 sec.

            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("create table person (id integer, name string)");
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            e.printStackTrace(System.err);
        }
    }
}