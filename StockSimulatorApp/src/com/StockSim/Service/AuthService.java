package com.StockSim.Service;

import com.StockSim.Model.User;
import com.StockSim.Storage.UserStore;
import org.mindrot.jbcrypt.BCrypt;
 // Register and login — backed by users.txt
public class AuthService implements IAuthService{
    private final UserStore userStore = new UserStore();

    public User register(String username, String password) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        if (userStore.exists(username))
            throw new RuntimeException("Username already taken");

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, hashed);
        userStore.save(user);
        return user;
    }

    public User login(String username, String password) {
        return userStore.findByUsername(username)
                .filter(u -> BCrypt.checkpw(password, u.getPasswordHash()))
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
    }
}