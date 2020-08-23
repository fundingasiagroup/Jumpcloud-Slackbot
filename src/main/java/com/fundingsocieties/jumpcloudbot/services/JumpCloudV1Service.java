package com.fundingsocieties.jumpcloudbot.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundingsocieties.jumpcloudbot.dtos.ResetMFADTO;
import com.fundingsocieties.jumpcloudbot.dtos.SearchFilterDTO;
import io.swagger.clientv1.ApiException;
import io.swagger.clientv1.api.ApplicationsApi;
import io.swagger.clientv1.api.SearchApi;
import io.swagger.clientv1.api.SystemsApi;
import io.swagger.clientv1.api.SystemusersApi;
import io.swagger.clientv1.model.System;
import io.swagger.clientv1.model.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.fundingsocieties.jumpcloudbot.config.JumpCloudConfig.JUMPCLOUD_V1_URL;

@Slf4j
@Service
public class JumpCloudV1Service {

    private static String contentType = "application/json";
    private static String accept = "application/json";
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private SystemsApi systemsApi;
    @Autowired
    private SystemusersApi systemusersApi;
    @Autowired
    private ApplicationsApi applicationsApiV1;
    @Autowired
    private SearchApi searchApi;
    @Autowired
    private OkHttpClient okHttpClient;

    public Systemuserreturn findSystemUserByEmail(String email) {
        try {
            Search search = new Search();
            SearchFilterDTO searchFilterDTO = new SearchFilterDTO();
            searchFilterDTO.setSearchTerm(email);
            List<String> fields = new ArrayList<>();
            fields.add("email");
            searchFilterDTO.setFields(fields);
            search.setSearchFilter(searchFilterDTO);
            return searchApi.searchSystemusersPost(contentType, accept, search, null, null, null, null, null)
                    .getResults().get(0);
        } catch (Exception ex) {
            return null;
        }
    }

    public Systemuserreturn findSystemUserById(String userId) {
        String xOrgId = ""; // String |
        try {
            return systemusersApi.systemusersGet(userId, contentType, accept, null, null, xOrgId);
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public System findSystemById(String systemId) {
        try {
            return systemsApi.systemsGet(systemId, contentType, accept, null, null, null, null, null);
        } catch (ApiException ex) {
            return null;
        }
    }

    public Application findApplicationById(String applicationId) {
        try {
            return applicationsApiV1.applicationsGet(applicationId, contentType, accept, null);
        } catch (ApiException ex) {
            return null;
        }
    }

    public List<Application> getAllApps() {
        List<Application> apps = new ArrayList<>();
        Applicationslist applicationslist = null;
        int i = 100;
        try {
            while (i == 100 || applicationslist.getTotalCount() > apps.size()) {
                applicationslist = applicationsApiV1.applicationsList(contentType, accept, null, null, i - 100, null, null, "");
                apps.addAll(applicationslist.getResults());
                i = i + 100;
            }
            return apps;
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean resetUserMFA(String userId, int exclusionPeriod) {
        try {
            //Had to use manual call as the SDK function doesn't work
            MediaType mediaType = MediaType.parse("application/json");
            String bodyString = null;
            LocalDateTime ldt = LocalDateTime.now().plusDays(exclusionPeriod);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
            ResetMFADTO resetMFADTO = new ResetMFADTO();
            resetMFADTO.setExclusion(true);
            resetMFADTO.setExclusionUntil(ldt.toString());
            ObjectMapper objectMapper = new ObjectMapper();
            bodyString = objectMapper.writeValueAsString(resetMFADTO);
            RequestBody body = RequestBody.create(mediaType, bodyString);
            String url = JUMPCLOUD_V1_URL + "/systemusers/" + userId + "/resetmfa";
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("x-api-key", credentialService.getJumpCloudToken())
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                response.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unlockUserById(String userId) {
        String xOrgId = "";
        try {
            systemusersApi.systemusersUnlock(userId, contentType, accept, xOrgId);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<System> getAllSystems() {
        List<System> systems = new ArrayList<>();
        Systemslist systemslist = null;
        int i = 100;
        try {
            while (i == 100 || systemslist.getTotalCount() > systems.size()) {
                systemslist = systemsApi.systemsList(contentType, accept, null, 100, "", null, i - 100, null, null);
                systems.addAll(systemslist.getResults());
                i = i + 100;
            }
            return systems;
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean manageSystemTOTPLogin(String userId, boolean enableTOTP) {
        Systemput body = new Systemput();
        body.allowMultiFactorAuthentication(enableTOTP);
        try {
            System result = systemsApi.systemsPut(userId, contentType, accept, body, null, null, "");
            if (result != null) {
                return true;
            }
        } catch (Exception ex) {
            log.error("Error while changing system totp login exception {}", ex.getMessage());
        }
        return false;
    }

}
