package com.group2.VinfastAuto.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Stack;

@Component
@Slf4j
public class ConnectionPoolImpl implements ConnectionPool {

    private final Environment env;

    private String username;
    private String password;
    private String url;
    private String driver;

    private Stack<Connection> pool;

    @Autowired
    public ConnectionPoolImpl(Environment env) {
        this.env = env;

        try {
            this.driver = env.getProperty("db.driver");
            this.url = env.getProperty("db.url");
            this.username = env.getProperty("db.username");
            this.password = env.getProperty("db.password");
            this.pool = new Stack<>();

            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Database Driver not found!", e);
        }
    }

    @Override
    public Connection getConnection(String objName) throws SQLException {
        if (this.pool.isEmpty()) {
            log.info(objName + " have created a new Connection!");
            return DriverManager.getConnection(url, username, password);
        } else {
            log.info(objName + " have popped the Connection!");
            return (Connection) this.pool.pop();
        }

    }

    @Override
    public void releaseConnection(Connection con, String objName) throws SQLException {
        log.info(objName + " have push the Connection!");
        this.pool.push(con);
    }

}
