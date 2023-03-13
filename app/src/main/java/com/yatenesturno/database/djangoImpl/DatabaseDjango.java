package com.yatenesturno.database.djangoImpl;

import com.yatenesturno.Flags;
import com.yatenesturno.database.interfaces.Database;

public class DatabaseDjango implements Database {

    /* Django URL */
    String DJANGO_URL_BASE_LOCALHOST = "http://10.0.2.2:3000";
    String DJANGO_URL_BASE_SANDBOX = "https://yatenesturno-sandbox.herokuapp.com/";
    String DJANGO_URL_BASE_LAN = "http://192.168.0.246:3000";
    String DJANGO_URL_BASE_REMOTE = "https://salty-hollows-67678.herokuapp.com";

    @Override
    public String getUrl() {
        if (Flags.LOCAL) {
            return DJANGO_URL_BASE_LOCALHOST;
        }
        if (Flags.SANDBOX) {
            return DJANGO_URL_BASE_SANDBOX;
        }
        if (Flags.LAN) {
            return DJANGO_URL_BASE_LAN;
        }
        return DJANGO_URL_BASE_REMOTE;
    }
}
