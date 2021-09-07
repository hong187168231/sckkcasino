package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserDetail;
import com.qianyi.casinocore.repository.UserRepository;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    public Integer countByIp(String ip) {
        return userRepository.countByRegisterIp(ip);
    }

    public ResponseEntity resetPassword(String userName) {
        User user = userRepository.getByName(userName);
        if(user == null){
            return ResponseUtil.success();
        }
        //重置密码
        String password = Constants.USER_SET_PASSWORD;
        BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        //加密
        String newPassword = passwordEncoder.encode(password);
        user.setPassword(newPassword);
        userRepository.save(user);
        return ResponseUtil.success("保存成功");
    }


    /**
     * 用户列表查询
     *
     * @param current
     * @param size
     * @param user
     * @return
     */
    public Page<User> findUserPage(Integer current, Integer size, User user) {
        Pageable pageable = PageRequest.of(current, size, Sort.Direction.DESC, "id");
        return userRepository.findAll(User.getCondition(user), pageable);
    }

}
