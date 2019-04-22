/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ui.user;


import cn.edu.shu.common.bean.User;

import javax.swing.*;
import java.util.List;

public class UserListModel extends AbstractListModel {
    private List<User> users;

    UserListModel(){
    }


    @Override
    public int getSize() {
        return users.size();
    }

    @Override
    public Object getElementAt(int index) {
        return users.get(index).getUsername();
    }

    public User getUser(int index){
        return users.get(index);
    }

    void setUsers(List<User> users) {
        this.users = users;
    }

    void addRow(User user){
        users.add(user);
        fireIntervalAdded(this, users.size() - 1, users.size() - 1);
    }

    void removeRow(User user){
        int index = users.indexOf(user);
        users.remove(index);
        fireIntervalRemoved(this, index - 1, index - 1);
    }

    void removeRow(int index){
        users.remove(index);
        fireIntervalRemoved(this, index - 1, index - 1);
    }
}
