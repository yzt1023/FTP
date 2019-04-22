/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.db;

import cn.edu.shu.common.bean.User;
import cn.edu.shu.common.util.Constants;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private Logger logger = Logger.getLogger(getClass());
    private DBConnPool dbConnPool = DBConnPool.getInstance();
    private Connection connection;

    public void addUser(User user) {
        try {
            connection = dbConnPool.reserveConn();
            String sql = "insert into t_user(username, password, is_valid, readable, writable, deleted) values (?,?,?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setBoolean(3, user.isValid());
            statement.setBoolean(4, user.isReadable());
            statement.setBoolean(5, user.isWritable());
            statement.setBoolean(6, user.canDeleted());
            statement.executeUpdate();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            dbConnPool.releaseConn(connection);
        }
    }

    public void deleteUser(User user) {
        try {
            connection = dbConnPool.reserveConn();
            PreparedStatement statement = connection.prepareStatement("delete from t_user where username = ?");
            statement.setString(1, user.getUsername());
            statement.executeUpdate();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            dbConnPool.releaseConn(connection);
        }
    }

    public void updateUser(User user) {
        try {
            connection = dbConnPool.reserveConn();
            String sql = "update t_user set password = ?, readable = ?, writable = ?, deleted = ?, is_valid = ? where username = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getPassword());
            statement.setBoolean(2, user.isReadable());
            statement.setBoolean(3, user.isWritable());
            statement.setBoolean(4, user.canDeleted());
            statement.setBoolean(5, user.isValid());
            statement.setString(6, user.getUsername());
            statement.executeUpdate();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            dbConnPool.releaseConn(connection);
        }
    }

    public User getUserByName(String username) {
        try {
            connection = dbConnPool.reserveConn();
            PreparedStatement statement = connection.prepareStatement("select * from t_user where username = ? and is_valid = true ");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String password = resultSet.getString("password");
                boolean readable = resultSet.getBoolean("readable");
                boolean writable = resultSet.getBoolean("writable");
                boolean canDeleted = resultSet.getBoolean("deleted");
                return new User(username, password, true, readable, writable, canDeleted);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            dbConnPool.releaseConn(connection);
        }
        return null;
    }

    public List<User> getUsers(boolean exceptAnonymous) {
        List<User> users = new ArrayList<>();
        try {
            connection = dbConnPool.reserveConn();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from t_user");
            while (resultSet.next()) {
                if (exceptAnonymous && Constants.ANONYMOUS_USER.equals(resultSet.getString("username")))
                    continue;
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                boolean readable = resultSet.getBoolean("readable");
                boolean writable = resultSet.getBoolean("writable");
                boolean canDeleted = resultSet.getBoolean("deleted");
                boolean valid = resultSet.getBoolean("is_valid");
                User user = new User(username, password, valid, readable, writable, canDeleted);
                users.add(user);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            dbConnPool.releaseConn(connection);
        }
        return users;
    }

    public String findPswByUsername(String username) {
        try {
            connection = dbConnPool.reserveConn();
            PreparedStatement statement = connection.prepareStatement("select password from t_user where username = ? and is_valid = true");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("password");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            dbConnPool.releaseConn(connection);
        }
        return null;
    }

    public boolean isUserExists(String username) {
        return findPswByUsername(username) != null;
    }

}
