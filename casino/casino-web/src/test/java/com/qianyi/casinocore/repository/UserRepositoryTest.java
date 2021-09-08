package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    public void should_add_new_user(){

        User user = new User();

    }
}
