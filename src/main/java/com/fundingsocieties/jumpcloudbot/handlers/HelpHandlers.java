package com.fundingsocieties.jumpcloudbot.handlers;

import com.fundingsocieties.jumpcloudbot.services.LogService;
import com.fundingsocieties.jumpcloudbot.services.SlackService;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import static com.fundingsocieties.jumpcloudbot.utilities.CommonUtils.initializeResponseString;
import static com.fundingsocieties.jumpcloudbot.utilities.CommonUtils.PROCESSING_MESSAGE;

@Component
public class HelpHandlers {

    @Autowired
    SlackService slackService;

    @Autowired
    LogService logService;

    public Response helpBaseHandler(SlashCommandRequest req, SlashCommandContext ctx, String[] inputArray, String command) throws IOException {
        String commandAction = null;
        String senderName = slackService.findUserNameBySlackId(ctx.getRequestUserId());
        try {
            if (inputArray.length > 2) {
                return ctx.ack("Invalid argument length. Please pass command name after help parameter");
            } else {
                commandAction = inputArray[1];
                logService.log(req.getPayload().getUserId(), commandAction, Arrays.toString(inputArray));
                return identifyCommandAction(ctx, commandAction, command, senderName);
            }
        } catch (IndexOutOfBoundsException e) {
            return defaultHelpMessage(req, ctx, command, senderName);
        }

    }

    public Response identifyCommandAction(SlashCommandContext ctx, String commandAction, String command, String senderName) {
        new Thread(() -> {
            try {
                String responseString = initializeResponseString(command, senderName, commandAction, null);
                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream("helpCommand.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = in.readLine()) != null) {
                    String[] inputParser = line.split("`", 4);
                    if (commandAction.equals(inputParser[0])) {
                        responseString = responseString + "*" + inputParser[0] + "* " + inputParser[1] + "\n" + inputParser[2] + "\n _" + inputParser[3] + "_ \n \n";
                    }
                }
                in.close();
                if (responseString.equals("")) {
                    responseString = "command is not found.";
                }
                String finalResponseString = responseString;
                ctx.respond(r -> r.text(finalResponseString));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }

    public Response defaultHelpMessage(SlashCommandRequest req, SlashCommandContext ctx, String command, String senderName) {
        new Thread(() -> {
            try {
                String responseString = initializeResponseString(command, senderName, null, null);
                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream("helpCommand.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));


                String line;
                while ((line = in.readLine()) != null) {
                    String[] inputParser = line.split("`", 4);
                    responseString = responseString + "*" + inputParser[0] + "* " + inputParser[1] + "\n" + inputParser[2] + "\n _" + inputParser[3] + "_ \n \n";
                }
                in.close();
                String finalResponseString = responseString;
                ctx.respond(r -> r.text(finalResponseString));

            } catch (IOException e) {
                ctx.ack("cannot find helpCommand file. please check file directory.");
                e.printStackTrace();
            }
        }).start();
        return ctx.ack(PROCESSING_MESSAGE);
    }
}
