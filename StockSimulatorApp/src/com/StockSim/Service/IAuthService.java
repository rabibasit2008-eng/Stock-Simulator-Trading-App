package com.StockSim.Service;

import com.StockSim.Model.User;

public interface IAuthService {
    User register(String username, String password);
    User login(String username, String password);
}
