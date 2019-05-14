package cn.edu.shu.server.db;

import cn.edu.shu.server.config.DBConfig;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DBConnPool {

    private static DBConnPool instance = new DBConnPool();
    private List<Connection> freeConn = new LinkedList<>();
    private List<Connection> resrvConn = new LinkedList<>();
    private Logger logger = Logger.getLogger(DBConnPool.class);

    private DBConnPool() {
    }

    public static DBConnPool getInstance() {
        return instance;
    }

    public synchronized Connection reserveConn() {
        if(freeConn.isEmpty())
            freeConn.add(createConn());
        Connection conn = freeConn.get(0);
        freeConn.remove(0);
        resrvConn.add(conn);
        return conn;
    }

    public synchronized void releaseConn(Connection conn){
        if(resrvConn.contains(conn)) {
            resrvConn.remove(conn);
            freeConn.add(conn);
        }
    }

    private Connection createConn(){
        try {
            DBConfig config = DBConfig.getInstance();
            Class.forName(config.getDriver()).newInstance();
            return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public synchronized void clearConns(){
        for(Connection conn : freeConn)
            closeConn(conn);
        freeConn.clear();
        for(Connection conn : resrvConn)
            closeConn(conn);
        resrvConn.clear();
    }

    private void closeConn(Connection conn){
        try{
            conn.close();
        }catch (SQLException e){
            logger.error(e.getMessage(), e);
        }
    }
}
