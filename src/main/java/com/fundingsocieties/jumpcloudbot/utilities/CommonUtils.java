package com.fundingsocieties.jumpcloudbot.utilities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommonUtils {

    private static String BASECOMMAND;

    @Value("${base-command}")
    public void setBaseCommand(String baseCommand){
        BASECOMMAND=baseCommand;
    }

    public static final String PROCESSING_MESSAGE = "Processing your request. Please wait...";
    public static final String REQUEST_FAILED_MESSAGE = "Request failed, please contact Jumpcloud Admin";
    public static final String UNABLE_TO_RETRIEVE_EMAIL_MESSAGE = "Unable to retrieve email, please try again or contact Jumpcloud Admin";
    public static final String INVALID_ARGUMENTS_EMAIL_GROUP_MESSAGE = "Invalid arguments, please pass user email followed by user group name";
    public static final String NO_USER_FOUND_MESSAGE = "No user found with this email, please try again or contact Jumpcloud Admin";
    public static final String NO_GROUPS_FOUND_MESSAGE = "No groups found by this name, please contact Jumpcloud Admin";
    public static final String MULTIPLE_GROUPS_FOUND_MESSAGE = "Multiple groups found by this name, please contact Jumpcloud Admin";

    public static String initializeResponseString(String command, String requestor, String paramOne, String paramTwo){
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("`"+BASECOMMAND+" ");
        responseBuilder.append(command);
        if(paramOne!=null) {
            responseBuilder.append(" ");
            responseBuilder.append(paramOne);
        }
        if(paramTwo!=null){
            responseBuilder.append(" ");
            responseBuilder.append(paramTwo);

        }
        responseBuilder.append("`");
        responseBuilder.append(" requested by: ");
        responseBuilder.append("*");
        responseBuilder.append(requestor);
        responseBuilder.append("* \n\n");
        return responseBuilder.toString();
    }

    public static String getSecondArgument(String[] inputArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 2; i < inputArray.length; i++) {
            stringBuilder.append(inputArray[i]);
            if (i < inputArray.length - 1) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    public static String getFirstArgument(String[] inputArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < inputArray.length; i++) {
            stringBuilder.append(inputArray[i]);
            if (i < inputArray.length - 1) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }
}
