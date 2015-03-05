package com.westbrain.sandbox.spring.user;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jruch on 2/26/2015.
 */
public class UserRepository {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();

    public UserRepository() {
        initializeData();
    }

    public User save(User user) {
        users.put(user.getLogin(), user);
        return user;
    }

    public User findByLogin(String login) {
        return users.get(login);
    }

    private void initializeData() {
        save(new User("user1", "password1"));
        save(new User("user2", "password2"));
        save(new User("user3", "password3"));
    }
}





