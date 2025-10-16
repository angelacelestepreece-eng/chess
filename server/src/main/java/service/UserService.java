package service;

import datamodel.RegistrationResult;
import model.UserData;


public class UserService {
    public RegistrationResult register(UserData user) {
        return new RegistrationResult(user.username(), "zzzy");
    }
}
