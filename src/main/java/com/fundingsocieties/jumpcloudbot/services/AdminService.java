package com.fundingsocieties.jumpcloudbot.services;

import com.fundingsocieties.jumpcloudbot.repositories.AdminsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AdminsRepository adminsRepository;

    public boolean isAdminUser(String slackUserId){
        if(adminsRepository.findActiveAdminBySlackUserId(slackUserId)!=null){
            return true;
        }else {
            return false;
        }
    }


}
