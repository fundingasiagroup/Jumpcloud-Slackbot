package com.fundingsocieties.jumpcloudbot.handlers;


import com.fundingsocieties.jumpcloudbot.services.HelperService;
import com.fundingsocieties.jumpcloudbot.services.JumpCloudV1Service;
import com.fundingsocieties.jumpcloudbot.services.JumpCloudV2Service;
import com.fundingsocieties.jumpcloudbot.services.SlackService;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import io.swagger.client.model.GraphObjectWithPaths;
import io.swagger.client.model.SystemGraphManagementReq;
import io.swagger.client.model.SystemGroup;
import io.swagger.client.model.SystemGroupMembersReq;
import io.swagger.clientv1.model.System;
import io.swagger.clientv1.model.Systemuserreturn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fundingsocieties.jumpcloudbot.utilities.CommonUtils.*;
import static com.fundingsocieties.jumpcloudbot.utilities.SlackResponseUtilities.*;

@Component
@Slf4j
public class SystemHandlers {

    @Autowired
    SlackService slackService;

    @Autowired
    HelperService helperService;

    @Autowired
    JumpCloudV2Service jumpCloudV2Service;

    @Autowired
    JumpCloudV1Service jumpCloudV1Service;

    public Response listSystemInfo(SlashCommandRequest req, SlashCommandContext ctx, String command) {
        new Thread(() -> {
            try {
                String[] systemNameParser = req.getPayload().getText().split(" ", 2);
                System result = findSystemByNameOrId(systemNameParser[1], ctx);
                String responseString = "";
                if (result != null) {
                    String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                    responseString = initializeResponseString(command, senderName, result.getHostname(), null);
                    responseString = responseString + "System " + result.getHostname() + " info: \n";
                    responseString = responseString + "Hostname: `" + result.getHostname() + "`\n";
                    responseString = responseString + "System ID: `" + result.getId() + "`\n";
                    responseString = responseString + "JC version: `" + result.getAgentVersion() + "`\n";
                    responseString = responseString + "Active: `" + result.isActive() + "`\n";
                    String finalResponseString = responseString;
                    ctx.respond(r -> r.text(finalResponseString)
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public Response manageSystemGroup(SlashCommandContext ctx, String[] inputArray, SystemGroupMembersReq.OpEnum op, String command) {
        new Thread(() -> {
            try {
                String system = null;
                String systemGroup = null;
                try {
                    systemGroup = getSecondArgument(inputArray);
                    system = inputArray[1];
                } catch (IndexOutOfBoundsException iEx) {
                    ctx.respond(INVALID_ARGUMENTS_EMAIL_GROUP_MESSAGE);
                }
                if (system == null || systemGroup == null) {
                    ctx.respond("Invalid arguments, please pass system name followed by system group name");
                } else {
                    String finalSystemGroup = systemGroup;
                    Optional<SystemGroup> matchingSystemGroup = jumpCloudV2Service
                            .getAllSystemGroups()
                            .stream()
                            .filter(sg -> sg.getName().equalsIgnoreCase(finalSystemGroup))
                            .findFirst();
                    if (!matchingSystemGroup.isPresent()) {
                        ctx.respond(NO_GROUPS_FOUND_MESSAGE);
                    } else {
                        System matchingSystem = findSystemByNameOrId(system, ctx);
                        if (matchingSystem != null) {
                            boolean response = jumpCloudV2Service.manageSystemInSystemGroup(matchingSystem.getId(), matchingSystemGroup.get().getId(), op);
                            if (response == true) {
                                String responseString = "";
                                String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                                responseString = initializeResponseString(command, senderName, system, matchingSystemGroup.get().getName());
                                responseString = responseString + "Successfully " + op.toString() + " " + system + " from " + systemGroup;
                                respondInChannel(ctx, responseString);
                            } else {
                                ctx.respond("Failed to " + op.toString() + " " + system + " from " + systemGroup + ". Please Contact Jumpcloud Admin.");
                            }

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public Response manageUserSystemBinding(SlashCommandContext ctx, String[] inputArray, SystemGraphManagementReq.OpEnum action, String command) {
        new Thread(() -> {
            try {
                String systemName = null;
                String userEmail = null;

                try {
                    systemName = getSecondArgument(inputArray);
                    userEmail = helperService.findEmail(inputArray[1]);
                    if (userEmail == null) {
                        ctx.respond("Unable to retrieve email, please try again or contact Jumpcloud Admin");
                    }
                } catch (IndexOutOfBoundsException iEx) {
                    ctx.respond("Invalid arguments, please pass user email followed by system name or systemID");
                }
                if (systemName == null || userEmail == null) {
                    ctx.respond("Invalid arguments, please pass user email followed by system name or systemID");

                } else {
                    System system = null;
                    system = findSystemByNameOrId(systemName, ctx);
                    if (system != null) {
                        Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
                        if (resultUser == null) {
                            ctx.respond(NO_USER_FOUND_MESSAGE);
                        }
                        boolean response = jumpCloudV2Service.manageUserSystemBinding(resultUser.getId(), system.getId(), action);
                        if (response) {
                            String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                            String responseString = initializeResponseString(command, senderName, userEmail, systemName);

                            if (action.equals(SystemGraphManagementReq.OpEnum.ADD)) {
                                responseString = responseString + resultUser.getFirstname() + " " + resultUser.getLastname() + " bound to system " + systemName;
                            } else {
                                responseString = responseString + "User " + resultUser.getFirstname() + " " + resultUser.getLastname() + " unbound from system " + systemName;
                            }
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

    public Response listAllSystemGroup(SlashCommandRequest req, SlashCommandContext ctx, String command) throws IOException {
        new Thread(() -> {
            try {
                List<SystemGroup> result = jumpCloudV2Service.getAllSystemGroups();
                if (result.size() == 0) {
                    ctx.respond("Unable to retrieve global list of system groups.");
                }
                String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                String responseString = initializeResponseString(command, senderName, null, null);
                for (SystemGroup systemGroup : result) {
                    responseString = responseString + "`" + systemGroup.getName() + "` ";
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

    public Response manageSystemTOTPLogin(SlashCommandRequest req, SlashCommandContext ctx, boolean enableTOTP, String command) {
        new Thread(() -> {
            try {
                boolean response = changeSystemTOTPLogin(req.getPayload().getText(), enableTOTP);
                String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                String[] emailParser = req.getPayload().getText().split(" ", 2);
                String userEmail = helperService.findEmail(emailParser[1]);
                String responseString = initializeResponseString(command, senderName, userEmail, null);
                if (response && enableTOTP) {
                    responseString = responseString + "Successfully enabled TOTP MFA for user.";
                    respondInChannel(ctx, responseString);
                } else if (response && !enableTOTP) {
                    responseString = responseString + "Successfully disabled TOTP MFA for user.";
                    respondInChannel(ctx, responseString);
                } else if (!response && enableTOTP) {
                    ctx.respond("Failed to enable TOTP MFA for user.");
                } else if (!response && !enableTOTP) {
                    ctx.respond("Failed to disable TOTP MFA for user.");
                } else {
                    ctx.respond("Something went wrong while setting TOTP MFA for user. Please contact Jumpcloud Admin.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    private boolean changeSystemTOTPLogin(String payload, boolean enableTOTP) {
        try {
            String[] emailParser = payload.split(" ", 2);
            String userEmail = helperService.findEmail(emailParser[1]);
            if (userEmail == null) {
                return false;
            }
            Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
            if (resultUser != null) {
                return jumpCloudV1Service.manageSystemTOTPLogin(resultUser.getId(), enableTOTP);
            } else {
                return false;
            }
        } catch (Exception ex) {
            log.error("Error while changing system totp setting exception: {}", ex.getMessage());
            return false;
        }

    }

    public Response listSystemsForUser(SlashCommandRequest req, SlashCommandContext ctx, String command) {
        new Thread(() -> {
            try {
                String[] argument = req.getPayload().getText().split(" ", 2);
                String userEmail = helperService.findEmail(argument[1]);
                if (userEmail == null) {
                    ctx.respond("Unable to retrieve email, please try again or contact Jumpcloud Admin");
                }
                Systemuserreturn resultUser = helperService.findSystemUserByEmailWrapper(userEmail);
                if (resultUser != null) {
                    List<System> result = returnUserSystems(resultUser);
                    if (result == null || result.size() == 0) {
                        ctx.respond("No systems bound to this user");
                    } else {
                        String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                        String responseString = initializeResponseString(command, senderName, userEmail, null);
                        responseString = responseString + buildResponseFormatSystem(result, resultUser);
                        respondInChannel(ctx, responseString);
                    }
                } else {
                    ctx.respond("No matching user found");
                }
            } catch (IOException ex) {
                log.error("Error while listing systems for a user exception: {}", ex.getMessage());
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public Response listUserBoundSystem(SlashCommandRequest req, SlashCommandContext ctx, String command) {
        new Thread(() -> {
            try {
                String[] argument = req.getPayload().getText().split(" ", 2);
                System system = findSystemByNameOrId(argument[1], ctx);
                if (system != null) {
                    List<Systemuserreturn> result = returnUsersBoundToSystem(system);
                    if (result != null) {
                        String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
                        String responseString = initializeResponseString(command, senderName, system.getHostname(), null);
                        responseString = responseString + "Users bound to System " + system.getHostname() + ":\n";
                        responseString = responseString + buildUserListString(result);
                        respondInChannel(ctx, responseString);
                    } else {
                        ctx.respond("No users bound to this system");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public List<System> returnUserSystems(Systemuserreturn resultUser) {
        try {
            List<GraphObjectWithPaths> result = jumpCloudV2Service.getSystemsForAUser(resultUser.getId());
            List<System> systems = result.stream()
                    .map(graphObjectWithPaths -> jumpCloudV1Service.findSystemById(graphObjectWithPaths.getId()))
                    .collect(Collectors.toList());
            return systems;
        } catch (Exception ex) {
            return null;
        }
    }

    public System findSystemByNameOrId(String systemName, SlashCommandContext ctx) {
        System system = null;
        try {
            System systemById = jumpCloudV1Service.findSystemById(systemName);
            if (systemById != null) {
                system = systemById;
            } else {
                System systemByName = findMatchingSystemByName(systemName, ctx);
                if (systemByName != null) {
                    system = systemByName;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return system;
    }

    System findMatchingSystemByName(String systemName, SlashCommandContext ctx) throws IOException {
        List<System> matchingSystems = getMatchingSystemsByName(systemName);
        if (matchingSystems.size() > 1) {
            String responseString = responseForDuplicateSystems(matchingSystems);
            ctx.respond(responseString);
            return null;
        } else if (matchingSystems.size() == 0) {
            ctx.respond("No system found by this name or ID, please contact Jumpcloud Admin");
            return null;
        }
        return matchingSystems.get(0);
    }

    public String responseForDuplicateSystems(List<System> matchingSystems) {
        String responseString = "Multiple systems by same name found, please complete the command with systemID instead. \n";
        for (System system : matchingSystems) {
            responseString = responseString + "Users in system *" + system.getHostname() + "* (SystemID: `" + system.getId() + "`) " + " : \n";
            List<Systemuserreturn> listSystemUser = returnUsersBoundToSystem(system);
            for (Systemuserreturn user : listSystemUser) {
                responseString = responseString + " `" + user.getFirstname() + " " + user.getLastname() + "` ";
            }
            responseString = responseString + "\n";
        }
        return responseString;
    }

    private List<System> getMatchingSystemsByName(String systemName) {
        List<System> matchingSystems = jumpCloudV1Service
                .getAllSystems()
                .stream()
                .filter(system -> {
                    if(system.getHostname()!=null){
                        if(system.getHostname().equalsIgnoreCase(systemName)){
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        return matchingSystems;
    }

    public List<Systemuserreturn> returnUsersBoundToSystem(System system) {
        try {
            List<GraphObjectWithPaths> result = jumpCloudV2Service.getUserBoundSystem(system.getId());
            List<Systemuserreturn> users = result.stream()
                    .map(graphObjectWithPaths -> jumpCloudV1Service.findSystemUserById(graphObjectWithPaths.getId()))
                    .collect(Collectors.toList());
            return users;
        } catch (Exception ex) {
            log.error("Error while fetching users bound to a system exception: {}", ex.getMessage());
            return null;
        }
    }


}
