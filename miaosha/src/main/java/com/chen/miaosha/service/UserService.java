package com.chen.miaosha.service;


import com.chen.miaosha.dao.UserDao;
import com.chen.miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    UserDao userDao;

    public User getById(int id){
        return userDao.getById(id);
    }

    /**
     * 开启事务管理
     * @return
     */
    @Transactional
    public Boolean tx(){
        User u1 = new User();
        u1.setId(2);
        u1.setName("chen");

        userDao.insert(u1);

        User u2 = new User();
        u2.setId(1);
        u2.setName("feng");

        userDao.insert(u2);

        return true;
    }
}
