package com.fundingsocieties.jumpcloudbot.services;

import io.swagger.clientv1.model.Systemuserreturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HelperService {

    @Autowired
    SlackService slackService;

    @Value("${domain}")
    private String domain;

    @Autowired
    JumpCloudV1Service jumpCloudV1Service;

    public String findEmail(String slackId) {
        try {
            String userEmail = "";
            String[] userIdParser1 = slackId.split(">", 2);
            //email is parsed as input
            if (userIdParser1[0].endsWith("@"+domain)) {
                String[] userIdParser2 = userIdParser1[0].split("\\|", 2);
                userEmail = userIdParser2[1];
            } else {
                // slack username is passed as input
                userEmail = slackService.findEmailBySlackId(slackId);
            }
            return userEmail;
        } catch (Exception e) {
            return null;
        }
    }

    public Systemuserreturn findSystemUserByEmailWrapper(String email) {
        Systemuserreturn resultUser = jumpCloudV1Service.findSystemUserByEmail(email);
        if (resultUser != null && resultUser.getEmail().equalsIgnoreCase(email)) {
            return resultUser;
        } else {
            return null;
        }
    }

}
