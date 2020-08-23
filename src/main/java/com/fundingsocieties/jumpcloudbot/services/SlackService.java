package com.fundingsocieties.jumpcloudbot.services;

import com.slack.api.Slack;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.users.UsersInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlackService {

    @Autowired
    CredentialService credentialService;

    public String findUserNameBySlackId(String slackId) {
        try {
            Slack slack = Slack.getInstance();
            String token = null;
            try {
                token = credentialService.getSlackToken();
            } catch (Exception e) {
                return null;
            }
            UsersInfoRequest usersInfoRequest = UsersInfoRequest.builder().token(token).user(slackId).build();
            UsersInfoResponse userInfo = slack.methods(token).usersInfo(usersInfoRequest);
            String realName = userInfo.getUser().getRealName();
            return realName;
        } catch (Exception e) {
            return null;
        }
    }

    public String findEmailBySlackId(String slackId) {
        try {
            Slack slack = Slack.getInstance();
            String token = null;
            try {
                token = credentialService.getSlackToken();
            } catch (Exception e) {
                return null;
            }
            String[] userIdParse1 = slackId.split("@", 2);
            String[] userIdParse2 = userIdParse1[1].split("\\|", 2);
            String userID = userIdParse2[0];
            UsersInfoRequest usersInfoRequest = UsersInfoRequest.builder().token(token).user(userID).build();
            UsersInfoResponse userInfo = slack.methods(token).usersInfo(usersInfoRequest);
            String userEmail = userInfo.getUser().getProfile().getEmail();
            return userEmail;
        } catch (Exception e) {
            return null;
        }
    }
}
