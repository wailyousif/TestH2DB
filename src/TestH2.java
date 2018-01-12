import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;

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

            conn.setAutoCommit(false);

            System.out.println("Current Active connections in Connections Pool=" + jdbcConnectionPool.getActiveConnections());

            //STEP 3: Execute a query
            System.out.println("Creating table in given database...");

            stmt = conn.createStatement();
            String sql =  "CREATE TABLE TRANS " +
                    "(id INTEGER not NULL, " +
                    " transtype VARCHAR(255), " +
                    " PRIMARY KEY ( id ))";

            stmt.executeUpdate(sql);
            System.out.println("Table created...");

            sql = "Insert into Trans (id, transtype) values (1, 'p2p')";
            stmt.executeUpdate(sql);
            System.out.println("First insert done...");

            sql = "Insert into Trans (id, transtype) values (2, 'p2m')";
            stmt.executeUpdate(sql);
            System.out.println("Second insert done...");

            conn.commit();

            PreparedStatement preparedStatement = conn.prepareStatement("Select id, transtype from Trans");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                System.out.println(resultSet.getInt(1) + ", " + resultSet.getString(2));
            }
            //preparedStatement.close();

            sql = "Update trans set transtype='p2b' where id = 1";
            stmt.executeUpdate(sql);
            //no commit now


            Thread anotherThred = new Thread()
            {
                public void run()
                {
                    try
                    {
                        System.out.println("Started another thread that has access to main thread objects");
                        System.out.println("AnotherThread: Current Active connections in Connections Pool=" + jdbcConnectionPool.getActiveConnections());

                        Connection thConn = jdbcConnectionPool.getConnection();

                        PreparedStatement thPrepStmt = thConn.prepareStatement("Select id, transtype from Trans");
                        ResultSet thRS = thPrepStmt.executeQuery();
                        while (thRS.next())
                        {
                            System.out.println(thRS.getInt(1) + ", " + thRS.getString(2));
                        }
                        thPrepStmt.close();

                        System.out.println("AnotherThread: Result set has been read");

                        String thSql = "Update trans set transtype='balanceInquiry' where id = 2";
                        Statement thStmt = thConn.createStatement();
                        thStmt.executeUpdate(thSql);

                        System.out.println("AnotherThread: Second record updated");
                    }
                    catch (Exception ex)
                    {
                        System.out.println("Exception:" + ex.getMessage());
                    }
                }
            };

            anotherThred.start();


            System.out.println("Main thread is sleeping for 5 seconds");
            Thread.sleep(2);
            System.out.println("Main thread finished sleeping, and will commit the update now");

            conn.commit();

            System.out.println("Reading data in main thread");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                System.out.println(resultSet.getInt(1) + ", " + resultSet.getString(2));
            }
            preparedStatement.close();

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
