package com.fundingsocieties.jumpcloudbot.utilities;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.response.Response;
import io.swagger.client.model.UserGroup;
import io.swagger.clientv1.model.System;
import io.swagger.clientv1.model.Systemuserreturn;

import java.io.IOException;
import java.util.List;

public class SlackResponseUtilities {

    public static String buildGroupResponseFormat(List<UserGroup> result, Systemuserreturn user) {
        String responseString ="";
        responseString = responseString + user.getFirstname() + " " + user.getLastname() + " is associated to the following groups:\n";
        for (UserGroup userGroup : result) {
            if (userGroup == null) {
                continue;
            }
            responseString = responseString + "`"+ userGroup.getName()+"` ";
        }
        return responseString;
    }

    public static void respondInChannel(SlashCommandContext ctx, String responseString) throws IOException {
        ctx.respond(res -> {
            res.responseType("in_channel");
            res.text(responseString);
            return res;
        });
    }

    public static String buildUserListString(List<Systemuserreturn> users){
        StringBuilder responseBuilder = new StringBuilder();
        int i = 0;
        for (Systemuserreturn user : users) {
            responseBuilder.append( ++i +". Name :`" + user.getFirstname() + " " + user.getLastname() + "`\n Email:`" + user.getEmail() + "`" + "\n");
        }
        return responseBuilder.toString();
    }

    public static String buildResponseFormatSystem(List<System> result, Systemuserreturn user) {
        String responseString ="";
        responseString = responseString + user.getFirstname() + " " + user.getLastname() + " is associated to the following systems:\n";
        for (System system : result) {
            if (system == null) {
                continue;
            }
            responseString = responseString + "`"+ system.getHostname()+"` ";
        }
        return responseString;
    }
}
