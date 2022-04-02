/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales.restaurant.dao;

import java.sql.SQLException;
import javax.sql.DataSource;
import java.sql.Connection;
import org.springframework.stereotype.Component;

/**
 *
 * @author premk
 */
@Component
public class ConnectionValidator {

    private final Connection connection;

    public ConnectionValidator(DataSource dataSource) {
        this.connection = getConnection(dataSource);
    }

    private Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean isConnectionValid() {
        try {
            return connection == null ? false : !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void releaseConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {

            }
        }
    }
}
