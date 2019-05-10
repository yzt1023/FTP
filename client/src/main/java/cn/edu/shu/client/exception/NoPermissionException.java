/*
 * Written By : Skye You
 * Copyright (c) 2018 - 2019. All Rights Reserved.
 */

package cn.edu.shu.client.exception;

import cn.edu.shu.common.util.Constants;

public class NoPermissionException extends Exception {

    public NoPermissionException() {
        super(Constants.PERMISSION_DENIED);
    }

}
