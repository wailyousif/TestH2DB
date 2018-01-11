import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by wailm.yousif on 1/11/18.
 */
public class TestH2 {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    //static final String DB_URL = "jdbc:h2:~/test";
    //static final String DB_URL = "jdbc:h2:~/test;MVCC=TRUE";
    //static final String DB_URL = "jdbc:h2:mem:";
    //static final String DB_URL = "jdbc:h2:mem:web";
    //static final String DB_URL = "jdbc:h2:mem:web;MVCC=TRUE";

    static final String DB_URL = "jdbc:h2:mem:web;MVCC=TRUE;MULTI_THREADED=1";

    //  Database credentials
    static final String USER = "sa";
    static final String PASS = "";

    public static void main(String args[])
    {
        System.out.println("Hello World");

        JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create(DB_URL, USER, PASS);

        Connection conn = null;
        Statement stmt = null;
        try {
            // STEP 1: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            jdbcConnectionPool.setMaxConnections(2000);

            System.out.println("Max number of connections in Connections Pool=" + jdbcConnectionPool.getMaxConnections());

            System.out.println("Current Active connections in Connections Pool=" + jdbcConnectionPool.getActiveConnections());

            //STEP 2: Open a connection
            System.out.println("Connecting to database...");
            //conn = DriverManager.getConnection(DB_URL,USER,PASS);
            conn = jdbcConnectionPool.getConnection();

            System.out.println("Current Active connections in Connections Pool=" + jdbcConnectionPool.getActiveConnections());

            //STEP 3: Execute a query
            System.out.println("Creating table in given database...");
            stmt = conn.createStatement();
            String sql =  "CREATE TABLE   REGISTRATION " +
                    "(id INTEGER not NULL, " +
                    " first VARCHAR(255), " +
                    " last VARCHAR(255), " +
                    " age INTEGER, " +
                    " PRIMARY KEY ( id ))";
            stmt.executeUpdate(sql);
            System.out.println("Created table in given database...");

            //Thread.sleep(20000);

            // STEP 4: Clean-up environment
            stmt.close();
            conn.close();

            jdbcConnectionPool.dispose();

        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try{
                if(stmt!=null) stmt.close();
            } catch(SQLException se2) {
            } // nothing we can do
            try {
                if(conn!=null) conn.close();
            } catch(SQLException se){
                se.printStackTrace();
            } //end finally try
        } //end try
        System.out.println("Goodbye!");
    }
}
