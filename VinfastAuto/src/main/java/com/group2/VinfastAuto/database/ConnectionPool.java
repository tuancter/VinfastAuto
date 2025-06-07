package com.group2.VinfastAuto.database;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPool {
    public Connection getConnection(String objName) throws SQLException;
    public void releaseConnection(Connection con, String objName) throws SQLException;
}
