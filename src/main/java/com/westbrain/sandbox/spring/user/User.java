package com.westbrain.sandbox.spring.user;

/**
 * Created by jruch on 2/26/2015.
 */

public class User {

    private Integer id;
    private String name;
    private String login;
    private String password;

    public User() {}

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public User(User user) {
        super();
        this.login = user.getLogin();
        this.password = user.getPassword();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
