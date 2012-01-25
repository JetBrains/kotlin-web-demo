package org.jetbrains.demo.ukhorskaya.session;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 1/24/12
 * Time: 2:11 PM
 */

public class UserInfo {
    private String name = "";
    private String id = "";
    private String type = "";
    private boolean isLogged = false;

    public void login(String name, String id, String type) {
        isLogged = true;
        this.name = name;
        this.type = type;
        this.id = id;
    }

    public boolean isLogin() {
        return isLogged;
    }

    public void logout() {
        name = "";
        id = "";
        isLogged = false;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

}
