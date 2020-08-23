package com.fundingsocieties.jumpcloudbot.handlers;

import com.fundingsocieties.jumpcloudbot.services.*;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import io.swagger.client.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static com.slack.api.model.block.composition.BlockCompositions.markdownText;


@Component
public class BaseHandlers {

    @Autowired
    private AdminService adminService;

    @Value("${allowed-channels}")
    private List<String> allowedChannels;

    @Autowired
    private LogService logService;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    HelpHandlers helpHandlers;

    @Autowired
    UserGroupHandlers userGroupHandlers;

    @Autowired
    AppHandlers appHandlers;

    @Autowired
    UserHandlers userHandlers;

    @Autowired
    SystemHandlers systemHandlers;

    public static String UNAUTHORIZED_MESSAGE = "You are not authorized to use this app";
    public static String INVALID_ARGUMENT_MESSAGE = "Invalid arguments, please refer to help";
    public static String INVALID_COMMAND_MESSAGE = "No matching command found, please try again";

    public Response baseHandler(SlashCommandRequest req, SlashCommandContext ctx) throws IOException {
        if (!adminService.isAdminUser(req.getPayload().getUserId()) || allowedChannels.indexOf(req.getPayload().getChannelId()) == -1) {
            return ctx.ack(UNAUTHORIZED_MESSAGE);
        } else {
            String[] inputArray = null;
            String commandAction = null;
            try {
                inputArray = req.getPayload().getText().split("\\s+");
                commandAction = inputArray[0];
                logService.log(req.getPayload().getUserId(), commandAction, Arrays.toString(inputArray));
            } catch (IndexOutOfBoundsException iEx) {
                ctx.respond(INVALID_ARGUMENT_MESSAGE);
            }
            switch (commandAction) {
                case "list-user-in-group":
                    return userGroupHandlers.listUsersInAGroup(ctx, inputArray, commandAction);
                case "list-user-group":
                    return userGroupHandlers.listGroupsForUser(ctx, inputArray[1], commandAction);
                case "remove-user-group":
                    return userGroupHandlers.removeUserFromUserGroup(ctx, inputArray, commandAction);
                case "add-user-group":
                    return userGroupHandlers.addUserToUserGroup(ctx, inputArray, commandAction);
                case "list-app-bound-user":
                    return appHandlers.listAppBoundUser(req, ctx, commandAction);
                case "list-system":
                    return systemHandlers.listSystemsForUser(req, ctx, commandAction);
                case "list-user-bound-system":
                    return systemHandlers.listUserBoundSystem(req, ctx, commandAction);
                case "list-user-bound-app":
                    return appHandlers.listUserBoundApp(req, ctx, commandAction);
                case "reset-mfa":
                    return userHandlers.resetMfa(ctx, inputArray, commandAction);
                case "user-unlock":
                    return userHandlers.userUnlock(ctx, inputArray, commandAction);
                case "list-user-attributes":
                    return userHandlers.listUserAttributes(req, ctx, commandAction);
                case "list-user-info":
                    return userHandlers.listJumpCloudAttributes(req, ctx, commandAction);
                case "list-system-info":
                    return systemHandlers.listSystemInfo(req, ctx, commandAction);
                case "remove-system":
                    return systemHandlers.manageSystemGroup(ctx, inputArray, SystemGroupMembersReq.OpEnum.REMOVE, commandAction);
                case "add-system":
                    return systemHandlers.manageSystemGroup(ctx, inputArray, SystemGroupMembersReq.OpEnum.ADD, commandAction);
                case "bind-system-user":
                    return systemHandlers.manageUserSystemBinding(ctx, inputArray, SystemGraphManagementReq.OpEnum.ADD, commandAction);
                case "unbind-system-user":
                    return systemHandlers.manageUserSystemBinding(ctx, inputArray, SystemGraphManagementReq.OpEnum.REMOVE, commandAction);
                case "enable-system-totp-login":
                    return systemHandlers.manageSystemTOTPLogin(req, ctx, true, commandAction);
                case "disable-system-totp-login":
                    return systemHandlers.manageSystemTOTPLogin(req, ctx, false, commandAction);
                case "list-all-user-group":
                    return userGroupHandlers.listAllUserGroup(req, ctx, commandAction);
                case "list-all-system-group":
                    return systemHandlers.listAllSystemGroup(req, ctx, commandAction);
                case "help":
                    return helpHandlers.helpBaseHandler(req,ctx, inputArray, commandAction);
                default:
                    return ctx.ack(INVALID_COMMAND_MESSAGE);
            }
        }
    }


}
