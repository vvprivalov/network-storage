package ru.gb.storage.server;

import java.sql.*;

public class UseDBforAuth {
    private Connection connection = null;

    public UseDBforAuth() {
        try {
            String url = "jdbc:mysql://localhost:3306/network";
            String user = "root";
            String password = "P0014-r290";
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Соединение с БД прошло успешно");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeDB() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkLoginAndPasswordAtIdentification(String login, String password) {
        String sql = "SELECT * FROM User WHERE login = ? AND password = ?";
        ResultSet rs;

        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean newUserRegistration(String login, String password, String firstName, String lastName) {
        String sql = "SELECT * FROM User WHERE login = ?";
        ResultSet rs;

        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, login);

            rs = preparedStatement.executeQuery();
            if (!rs.next()) {
                sql = "INSERT INTO user (login, password, firstname, lastname) VALUES (?, ?, ?, ?)";
                preparedStatement = this.connection.prepareStatement(sql);
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, firstName);
                preparedStatement.setString(4, lastName);
                preparedStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
