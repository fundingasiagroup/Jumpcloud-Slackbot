package com.fundingsocieties.jumpcloudbot.handlers;

import com.fundingsocieties.jumpcloudbot.services.HelperService;
import com.fundingsocieties.jumpcloudbot.services.JumpCloudV2Service;
import com.fundingsocieties.jumpcloudbot.services.JumpCloudV1Service;
import com.fundingsocieties.jumpcloudbot.services.SlackService;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import io.swagger.client.model.GraphConnection;
import io.swagger.client.model.GraphObjectWithPaths;
import io.swagger.clientv1.model.Application;
import io.swagger.clientv1.model.Systemuserreturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fundingsocieties.jumpcloudbot.utilities.CommonUtils.*;
import static com.fundingsocieties.jumpcloudbot.utilities.SlackResponseUtilities.buildUserListString;
import static com.fundingsocieties.jumpcloudbot.utilities.SlackResponseUtilities.respondInChannel;

@Component
public class AppHandlers {

    @Autowired
    SlackService slackService;

    @Autowired
    HelperService helperService;

    @Autowired
    JumpCloudV2Service jumpCloudV2Service;

    @Autowired
    JumpCloudV1Service jumpCloudV1Service;

    public Response listAppBoundUser(SlashCommandRequest req, SlashCommandContext ctx, String command) {
        new Thread(() -> {
            try {
                String[] emailParser = req.getPayload().getText().split(" ", 2);
                String userEmail = helperService.findEmail(emailParser[1]);
                if (userEmail == null) {
                    ctx.respond(UNABLE_TO_RETRIEVE_EMAIL_MESSAGE);
                }
                Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
                if (resultUser != null) {
                    List<Application> result = returnAppBoundUser(resultUser);
                    String responseString = "";
                    if (result == null) {
                        responseString = "No applications are bound to this user";
                    } else {
                        String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                        responseString = initializeResponseString(command, senderName, userEmail, null);
                        responseString = responseString + "User " + resultUser.getFirstname() + " " + resultUser.getLastname() + " is associated to the following applications: \n";
                        for (Application app : result) {
                            responseString = responseString + "`" + app.getDisplayLabel() + "`" + "\n";
                        }
                    }
                    respondInChannel(ctx, responseString);
                } else {
                    ctx.respond("No user found with this email");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public List<Application> returnAppBoundUser(Systemuserreturn user) {
        try {
            List<GraphObjectWithPaths> result = jumpCloudV2Service.getApplicationsForAUser(user.getId());
            List<Application> applications = result.stream()
                    .map(graphObjectWithPaths -> jumpCloudV1Service.findApplicationById(graphObjectWithPaths.getId()))
                    .collect(Collectors.toList());
            return applications;
        } catch (Exception ex) {
            return null;
        }
    }

    public Response listUserBoundApp(SlashCommandRequest req, SlashCommandContext ctx, String command) {
        new Thread(() -> {
            try {
                Application app = getApplicationByDisplayLabel(req.getPayload().getText());
                if (app == null) {
                    ctx.respond("No such application exists");
                } else {
                    List<Systemuserreturn> result = returnUserBoundApp(ctx, app);
                    String responseString = "";
                    if (result == null) {
                        responseString = "No users are bound to this application. alternatively check if you spelled the display name of the application correctly.";
                    } else {
                        String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                        responseString = initializeResponseString(command, senderName, app.getDisplayName(), null);
                        responseString = responseString + "Users bound to Application " + app.getDisplayName() + ":\n";
                        responseString = responseString + buildUserListString(result);
                    }
                    respondInChannel(ctx, responseString);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public List<Systemuserreturn> returnUserBoundApp(SlashCommandContext ctx, Application app) {
        try {
            Application application = jumpCloudV1Service.findApplicationById(app.getId());
            List<GraphObjectWithPaths> userGroup = jumpCloudV2Service.getUserGroupForApplication(application.getId());
            List<Systemuserreturn> listUserBoundApp = new ArrayList<>();
            for (int i = 0; i < userGroup.size(); i++) {
                List<GraphConnection> usersInUserGroup = jumpCloudV2Service.getUsersForUserGroup(userGroup.get(i).getId());
                List<Systemuserreturn> users = usersInUserGroup.stream()
                        .map(GraphConnection -> jumpCloudV1Service.findSystemUserById(GraphConnection.getTo().getId()))
                        .collect(Collectors.toList());
                for (int j = 0; j < users.size(); j++) {
                    if (listUserBoundApp.indexOf(users.get(j)) == -1) {
                        listUserBoundApp.add(users.get(j));
                    }
                }
            }
            return listUserBoundApp;
        } catch (Exception ex) {
            return null;
        }
    }

    private Application getApplicationByDisplayLabel(String payload) {
        String[] applicationNameParser = payload.split(" ", 2);
        List<Application> applicationList = jumpCloudV1Service.getAllApps();
        Application selectedApplication = null;
        for (Application app : applicationList) {
            if (app.getDisplayLabel().equals(applicationNameParser[1])) {
                selectedApplication = app;
                break;
            }
        }
        return selectedApplication;
    }

}
