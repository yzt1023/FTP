/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.server.ui.user;

import cn.edu.shu.common.bean.User;

public interface UserSettingListener {
    void dispose();
    boolean newUserToDB(User user);
    boolean RemoveUserFromDB(User user);
    boolean updateUser(User user);
    void showUserDetail(User user);
    void cleanDetail();
}
