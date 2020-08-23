package com.fundingsocieties.jumpcloudbot.handlers;

import com.fundingsocieties.jumpcloudbot.services.HelperService;
import com.fundingsocieties.jumpcloudbot.services.JumpCloudV1Service;
import com.fundingsocieties.jumpcloudbot.services.JumpCloudV2Service;
import com.fundingsocieties.jumpcloudbot.services.SlackService;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import io.swagger.client.model.GraphConnection;
import io.swagger.client.model.GraphObjectWithPaths;
import io.swagger.client.model.UserGroup;
import io.swagger.clientv1.model.Application;
import io.swagger.clientv1.model.Systemuserreturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fundingsocieties.jumpcloudbot.utilities.CommonUtils.*;
import static com.fundingsocieties.jumpcloudbot.utilities.SlackResponseUtilities.*;

@Component
public class UserGroupHandlers {

    @Autowired
    SlackService slackService;

    @Autowired
    HelperService helperService;

    @Autowired
    JumpCloudV2Service jumpCloudV2Service;

    @Autowired
    JumpCloudV1Service jumpCloudV1Service;

    public Response listGroupsForUser(SlashCommandContext ctx, String userId, String command) {

        new Thread(() -> {
            try {
                String userEmail = helperService.findEmail(userId);

                if (userEmail == null) {
                    ctx.respond(UNABLE_TO_RETRIEVE_EMAIL_MESSAGE);
                    return;
                }
                Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
                if (resultUser == null) {
                    ctx.respond(NO_USER_FOUND_MESSAGE);
                    return;
                }
                List<UserGroup> result = returnUserGroups(resultUser);
                if (result.size() == 0) {
                    ctx.respond("User " + resultUser.getFirstname() + " " + resultUser.getLastname() + " is not associated to any group");
                    return;
                }
                String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                String responseString = initializeResponseString(command, senderName, userEmail, null);
                responseString = responseString + buildGroupResponseFormat(result, resultUser);
                respondInChannel(ctx, responseString);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    ctx.respond(REQUEST_FAILED_MESSAGE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public Response listUsersInAGroup(SlashCommandContext ctx, String[] input, String command) {

        new Thread(() -> {
            try {
                String groupName = getFirstArgument(input);
                List<UserGroup> userGroups = findUserGroupByName(groupName);

                if (userGroups.size() > 1) {
                    ctx.respond(MULTIPLE_GROUPS_FOUND_MESSAGE);
                    return;
                }

                if (userGroups.size() == 0) {
                    ctx.respond(NO_GROUPS_FOUND_MESSAGE);
                    return;
                }

                List<GraphConnection> usersInUserGroup = jumpCloudV2Service.getUsersForUserGroup(userGroups.get(0).getId());
                List<Systemuserreturn> users = usersInUserGroup.stream()
                        .map(GraphConnection -> jumpCloudV1Service.findSystemUserById(GraphConnection.getTo().getId()))
                        .collect(Collectors.toList());
                if (users.size() == 0) {
                    ctx.respond("Group " + groupName + " does not have any members");
                    return;
                }
                String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                String responseString = initializeResponseString(command, senderName, groupName, null);
                responseString = responseString + "Group " + groupName + " has " + users.size() + " users: \n";

                responseString = responseString + buildUserListString(users);
                respondInChannel(ctx, responseString);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    ctx.respond(REQUEST_FAILED_MESSAGE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public Response removeUserFromUserGroup(SlashCommandContext ctx, String[] inputArray, String command) {
        new Thread(() -> {
            try {
                String userGroupName = null;
                String userEmail = null;
                try {
                    userGroupName = getSecondArgument(inputArray);
                    userEmail = helperService.findEmail(inputArray[1]);
                    if (userEmail == null) {
                        ctx.respond(UNABLE_TO_RETRIEVE_EMAIL_MESSAGE);
                    }
                } catch (IndexOutOfBoundsException iEx) {
                    ctx.respond(INVALID_ARGUMENTS_EMAIL_GROUP_MESSAGE);
                }
                if (userGroupName == null || userEmail == null) {
                    ctx.respond(INVALID_ARGUMENTS_EMAIL_GROUP_MESSAGE);

                } else {
                    List<UserGroup> matchingGroups = findUserGroupByName(userGroupName);
                    if (matchingGroups.size() > 1) {
                        ctx.respond("Multiple groups by same name found, please contact Jumpcloud Admin");
                    } else if (matchingGroups.size() == 0) {
                        ctx.respond(NO_GROUPS_FOUND_MESSAGE);
                    } else {
                        Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
                        if (resultUser == null) {
                            ctx.respond(NO_USER_FOUND_MESSAGE);
                        }
                        boolean response = jumpCloudV2Service.removeUserFromUserGroup(matchingGroups.get(0).getId(), resultUser.getId());
                        if (response) {
                            String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                            String responseString = initializeResponseString(command, senderName, userEmail, userGroupName);
                            responseString = responseString + "User " + resultUser.getFirstname() + " " + resultUser.getLastname() + " removed from group " + userGroupName;
                            respondInChannel(ctx, responseString);
                        } else {
                            ctx.respond(REQUEST_FAILED_MESSAGE);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack();
    }

    public Response addUserToUserGroup(SlashCommandContext ctx, String[] inputArray, String command) {
        new Thread(() -> {
            try {
                String userGroupName = null;
                String userEmail = null;
                try {
                    userGroupName = getSecondArgument(inputArray);
                    userEmail = helperService.findEmail(inputArray[1]);
                    if (userEmail == null) {
                        ctx.respond(UNABLE_TO_RETRIEVE_EMAIL_MESSAGE);
                    }
                } catch (IndexOutOfBoundsException iEx) {
                    ctx.respond(INVALID_ARGUMENTS_EMAIL_GROUP_MESSAGE);
                }
                if (userGroupName == null || userEmail == null) {
                    ctx.respond(INVALID_ARGUMENTS_EMAIL_GROUP_MESSAGE);

                } else {
                    List<UserGroup> matchingGroups = findUserGroupByName(userGroupName);
                    if (matchingGroups.size() > 1) {
                        ctx.respond("Multiple groups by same name found, please contact Jumpcloud Admin");
                    } else if (matchingGroups.size() == 0) {
                        ctx.respond(NO_GROUPS_FOUND_MESSAGE);
                    } else {
                        Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
                        if (resultUser == null) {
                            ctx.respond(NO_USER_FOUND_MESSAGE);
                        }
                        boolean response = jumpCloudV2Service.addUserToUserGroup(matchingGroups.get(0).getId(), resultUser.getId());
                        String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                        String responseString = initializeResponseString(command, senderName, userEmail, userGroupName);
                        responseString = responseString + "User " + resultUser.getFirstname() + " " + resultUser.getLastname() + " added to group " + userGroupName;
                        if (response) {
                            respondInChannel(ctx, responseString);
                        } else {
                            ctx.respond(REQUEST_FAILED_MESSAGE);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack();
    }

    public Response listAllUserGroup(SlashCommandRequest req, SlashCommandContext ctx, String command) throws IOException {
        new Thread(() -> {
            try {
                List<UserGroup> result = jumpCloudV2Service.getAllUserGroups();
                if (result.size() == 0) {
                    ctx.respond("Unable to retrieve global list of user groups.");
                }
                String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                String responseString = initializeResponseString(command, senderName, null, null);
                for (UserGroup userGroup : result) {
                    responseString = responseString + "`" + userGroup.getName() + "` ";
                }
                final String finalResponseString = responseString;
                ctx.respond(res -> {
                    res.text(finalResponseString);
                    return res;
                });
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    ctx.respond(REQUEST_FAILED_MESSAGE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public List<UserGroup> returnUserGroups(Systemuserreturn resultUser) {
        try {
            List<UserGroup> result = jumpCloudV2Service.getGroupsForAUser(resultUser.getId());
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<UserGroup> findUserGroupByName(String userGroupName) {
        List<UserGroup> userGroups = jumpCloudV2Service.getAllUserGroups();
        return userGroups
                .stream()
                .filter(userGroup -> userGroup.getName().equalsIgnoreCase(userGroupName))
                .collect(Collectors.toList());
    }

}
