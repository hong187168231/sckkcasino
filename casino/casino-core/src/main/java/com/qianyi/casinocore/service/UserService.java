package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepository;
    public User findByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public void subMoney(Long id, BigDecimal money) {
        synchronized (id) {
            userRepository.subMoney(id,money);
        }
    }

    public void addMoney(Long id, BigDecimal money) {
        synchronized (id) {
            userRepository.addMoney(id,money);
        }
    }
}
