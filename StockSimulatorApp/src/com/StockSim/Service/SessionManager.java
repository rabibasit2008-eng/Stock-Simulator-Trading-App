package com.StockSim.Service;
import com.StockSim.Model.User;

public class SessionManager {
    private static volatile User currentUser;

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }
    public static synchronized User getCurrentUser() {
        return currentUser;
    }
    public static synchronized void logout() {
        currentUser = null;
    }
}