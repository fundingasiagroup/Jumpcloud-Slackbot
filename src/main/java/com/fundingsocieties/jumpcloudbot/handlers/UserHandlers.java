package com.fundingsocieties.jumpcloudbot.handlers;

import com.fundingsocieties.jumpcloudbot.services.HelperService;
import com.fundingsocieties.jumpcloudbot.services.JumpCloudV1Service;
import com.fundingsocieties.jumpcloudbot.services.JumpCloudV2Service;
import com.fundingsocieties.jumpcloudbot.services.SlackService;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import io.swagger.clientv1.model.Systemuserreturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.fundingsocieties.jumpcloudbot.utilities.CommonUtils.*;
import static com.fundingsocieties.jumpcloudbot.utilities.SlackResponseUtilities.respondInChannel;

@Component
public class UserHandlers {

    @Autowired
    SlackService slackService;

    @Autowired
    HelperService helperService;

    @Autowired
    JumpCloudV2Service jumpCloudV2Service;

    @Autowired
    JumpCloudV1Service jumpCloudV1Service;

    public Response resetMfa(SlashCommandContext ctx, String[] inputArray, String command) {
        new Thread(() -> {
            try {
                String userEmail = null;
                int exclusionPeriod = 0;
                try {
                    userEmail = helperService.findEmail(inputArray[1]);
                    if (userEmail == null) {
                        ctx.respond(UNABLE_TO_RETRIEVE_EMAIL_MESSAGE);
                    }
                    if (inputArray.length > 2) {
                        try {
                            exclusionPeriod = Integer.parseInt(getSecondArgument(inputArray));
                        } catch (NumberFormatException nex) {
                            ctx.respond("Invalid arguments, please pass user email followed by exclusion period in days");
                            return;
                        }
                    } else {
                        ctx.respond("Invalid arguments, please pass user email followed by exclusion period in days");
                        return;
                    }
                } catch (IndexOutOfBoundsException iEx) {
                    ctx.respond("Invalid arguments, please pass user email followed by exclusion period in days");
                }
                if (userEmail == null) {
                    ctx.respond("Invalid arguments, please pass user email followed by exclusion period in days");

                } else {
                    Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
                    if (resultUser == null) {
                        ctx.respond(NO_USER_FOUND_MESSAGE);
                    }
                    boolean result = jumpCloudV1Service.resetUserMFA(resultUser.getId(), exclusionPeriod);
                    if (result) {
                        String responseString = "";
                        String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                        responseString = initializeResponseString(command, senderName, userEmail, String.valueOf(exclusionPeriod));
                        responseString = responseString + "User " + resultUser.getFirstname() + " " + resultUser.getLastname() + " MFA reset successfully";
                        respondInChannel(ctx, responseString);
                    } else {
                        ctx.respond(REQUEST_FAILED_MESSAGE);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack();
    }

    public Response userUnlock(SlashCommandContext ctx, String[] inputArray, String command) {
        new Thread(() -> {
            try {
                String userEmail = null;
                try {
                    userEmail = helperService.findEmail(inputArray[1]);
                    if (userEmail == null) {
                        ctx.respond(UNABLE_TO_RETRIEVE_EMAIL_MESSAGE);
                    }
                } catch (IndexOutOfBoundsException iEx) {
                    ctx.respond(INVALID_ARGUMENTS_EMAIL_GROUP_MESSAGE);
                }
                if (userEmail == null) {
                    ctx.respond(INVALID_ARGUMENTS_EMAIL_GROUP_MESSAGE);
                } else {
                    Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
                    if (resultUser == null) {
                        ctx.respond(NO_USER_FOUND_MESSAGE);
                    }
                    boolean result = jumpCloudV1Service.unlockUserById(resultUser.getId());
                    if (result) {
                        String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                        String responseString = initializeResponseString(command, senderName, userEmail, null);
                        responseString = responseString + "User " + resultUser.getFirstname() + " " + resultUser.getLastname() + " unlocked successfully";
                        respondInChannel(ctx, responseString);
                    } else {
                        ctx.respond(REQUEST_FAILED_MESSAGE);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack();
    }


    public Response listUserAttributes(SlashCommandRequest req, SlashCommandContext ctx, String command) {
        new Thread(() -> {
            try {
                Systemuserreturn result = returnUserAttributes(req.getPayload().getText());
                String responseString = "";
                if (result == null) {
                    responseString = "User not found.";
                } else {
                    String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());

                    responseString = initializeResponseString(command, senderName, result.getEmail(), null);
                    responseString = responseString + "User Attributes of " + result.getFirstname() + " " + result.getLastname() + ":\n";
                    responseString = responseString + "Name: `" + result.getFirstname() + " " + result.getLastname() + "`\n";
                    responseString = responseString + "Department: `" + result.getDepartment() + "`\n";
                    responseString = responseString + "Job Title: `" + result.getJobTitle() + "`\n";
                    responseString = responseString + "Employee Identifier: `" + result.getEmployeeIdentifier() + "`\n";
                    responseString = responseString + "Employee Type: `" + result.getEmployeeType() + "`\n";
                }
                respondInChannel(ctx, responseString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public Systemuserreturn returnUserAttributes(String email) {
        try {
            String[] emailParser = email.split(" ", 2);
            String userEmail = helperService.findEmail(emailParser[1]);
            if (userEmail == null) {
                return null;
            }
            Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
            return resultUser;
        } catch (Exception ex) {
            return null;
        }
    }

    public Response listJumpCloudAttributes(SlashCommandRequest req, SlashCommandContext ctx, String command) {
        new Thread(() -> {
            try {
                Systemuserreturn result = returnUserAttributes(req.getPayload().getText());
                String responseString = "";
                if (result == null) {
                    responseString = "User not found.";
                } else {
                    String senderName= slackService.findUserNameBySlackId(ctx.getRequestUserId());
                    responseString = initializeResponseString(command, senderName, result.getEmail(), null);
                    responseString = responseString + "User Info of "+ result.getFirstname() +" "+ result.getLastname() +":\n";
                    responseString = responseString + "Email: `" + result.getEmail() + "`\n";
                    responseString = responseString + "Username: `" + result.getUsername() + "`\n";
                    responseString = responseString + "Account Locked: `" + result.isAccountLocked() + "`\n";
                    responseString = responseString + "MFA Configuration Status: `" + result.getMfa().isConfigured() + "`\n";
                    responseString = responseString + "Password Expiration Date: `" + result.getPasswordExpirationDate() + "`\n";
                }
                respondInChannel(ctx, responseString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }
}
