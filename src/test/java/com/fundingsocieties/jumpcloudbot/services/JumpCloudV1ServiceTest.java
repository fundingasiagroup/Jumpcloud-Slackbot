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
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.fundingsocieties.jumpcloudbot.config.JumpCloudConfig.JUMPCLOUD_V1_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class JumpCloudV1ServiceTest {

    private static String contentType = "application/json";
    private static String accept = "application/json";

    @MockBean
    private CredentialService credentialService;
    @MockBean
    private SystemsApi systemsApi;
    @MockBean
    private SystemusersApi systemusersApi;
    @MockBean
    private ApplicationsApi applicationsApiV1;
    @MockBean
    private SearchApi searchApi;
    @MockBean
    private OkHttpClient okHttpClient;

    @Autowired
    JumpCloudV1Service jumpCloudV1Service;

    @Test
    void findSystemUserByEmail_shouldReturnUserWhenFound() throws ApiException {
        String testEmail = "john@doe.com";
        Search search = new Search();
        SearchFilterDTO searchFilterDTO = new SearchFilterDTO();
        searchFilterDTO.setSearchTerm(testEmail);
        List<String> fields = new ArrayList<>();
        fields.add("email");
        searchFilterDTO.setFields(fields);
        search.setSearchFilter(searchFilterDTO);
        List<Systemuserreturn> systemusers = new ArrayList<>();
        Systemuserreturn systemuserreturn = new Systemuserreturn();
        systemuserreturn.setEmail(testEmail);
        systemusers.add(systemuserreturn);
        Systemuserslist systemuserslist = new Systemuserslist();
        systemuserslist.setResults(systemusers);
        when(searchApi.searchSystemusersPost(contentType, accept, search, null, null, null, null, null)).thenReturn(systemuserslist);
        Systemuserreturn response = jumpCloudV1Service.findSystemUserByEmail(testEmail);
        assertEquals(response.getEmail(), testEmail);
    }

    @Test
    void findSystemUserByEmail_shouldReturnNullWhenApiExceptionThrow() throws ApiException {
        String testEmail = "john@doe.com";
        Search search = new Search();
        SearchFilterDTO searchFilterDTO = new SearchFilterDTO();
        searchFilterDTO.setSearchTerm(testEmail);
        List<String> fields = new ArrayList<>();
        fields.add("email");
        searchFilterDTO.setFields(fields);
        search.setSearchFilter(searchFilterDTO);
        when(searchApi.searchSystemusersPost(contentType, accept, search, null, null, null, null, null)).thenThrow(new ApiException());
        Systemuserreturn response = jumpCloudV1Service.findSystemUserByEmail(testEmail);
        assertNull(response);
    }

    @Test
    void findSystemUserById_shouldReturnUserWhenFound() throws ApiException {
        String testUserId = "abcefkjkj2";
        Systemuserreturn systemuserreturn = new Systemuserreturn();
        systemuserreturn.setId(testUserId);
        when(systemusersApi.systemusersGet(testUserId, contentType, accept, null, null, "")).thenReturn(systemuserreturn);
        Systemuserreturn response = jumpCloudV1Service.findSystemUserById(testUserId);
        assertEquals(response.getId(), testUserId);
    }

    @Test
    void findSystemUserById_shouldReturnNullWhenNotFound() throws ApiException {
        String testUserId = "abcefkjkj2";
        when(systemusersApi.systemusersGet(testUserId, contentType, accept, null, null, "")).thenThrow(new ApiException());
        Systemuserreturn response = jumpCloudV1Service.findSystemUserById(testUserId);
        assertNull(response);
    }

    @Test
    void findSystemById_shouldReturnSystemWhenFound() throws ApiException {
        String testSystemId = "abcdefg";
        System system = new System();
        system.setId(testSystemId);
        when(systemsApi.systemsGet(testSystemId, contentType, accept, null, null, null, null, null)).thenReturn(system);
        System result = jumpCloudV1Service.findSystemById(testSystemId);
        assertEquals(testSystemId, result.getId());
    }

    @Test
    void findSystemById_shouldReturnNullWhenNotFound() throws ApiException {
        String testSystemId = "abcdefg";
        System system = new System();
        system.setId(testSystemId);
        when(systemsApi.systemsGet(testSystemId, contentType, accept, null, null, null, null, null)).thenThrow(new ApiException());
        System result = jumpCloudV1Service.findSystemById(testSystemId);
        assertNull(result);
    }

    @Test
    void findApplicationById_shouldReturnApplicationWhenFound() throws ApiException {
        String testAppId = "abcdefg";
        Application application = new Application();
        application.setId(testAppId);
        when(applicationsApiV1.applicationsGet(testAppId, contentType, accept, null)).thenReturn(application);
        Application result = jumpCloudV1Service.findApplicationById(testAppId);
        assertEquals(testAppId, result.getId());
    }

    @Test
    void findApplicationById_shouldReturnNullWhenNotFound() throws ApiException {
        String testAppId = "abcdefg";
        when(applicationsApiV1.applicationsGet(testAppId, contentType, accept, null)).thenThrow(new ApiException());
        Application result = jumpCloudV1Service.findApplicationById(testAppId);
        assertNull(result);
    }

    @Test
    void getAllApps_shouldReturnListOfApps() throws ApiException {
        List<Application> applications = new ArrayList<>();
        Application application = new Application();
        application.setId("abc");
        application.setName("def");
        applications.add(application);
        Applicationslist applicationslist = new Applicationslist();
        applicationslist.setTotalCount(1);
        applicationslist.setResults(applications);
        when(applicationsApiV1.applicationsList(contentType, accept, null, null, 0, null, null, "")).thenReturn(applicationslist);
        List<Application> result = jumpCloudV1Service.getAllApps();
        assertEquals(applications, result);
    }

    @Test
    void getAllApps_shouldReturnNullWhenExceptionIsThrown() throws ApiException {
        when(applicationsApiV1.applicationsList(contentType, accept, null, null, 0, null, null, "")).thenThrow(new ApiException());
        List<Application> result = jumpCloudV1Service.getAllApps();
        assertNull(result);
    }

    @Test
    void resetUserMFA_returnTrueWhenSuccessful() throws IOException {
        when(credentialService.getJumpCloudToken()).thenReturn("xyz");
        long exclusionPeriod = 7;
        String userId = "abc";
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
        Response response = new Response.Builder().request(request).protocol(Protocol.HTTP_2).message("").body(ResponseBody.create(
                MediaType.get("application/json; charset=utf-8"),
                "{}"
        )).code(200).build();
        final Call remoteCall = mock(Call.class);
        when(remoteCall.execute()).thenReturn(response);
        when(okHttpClient.newCall(any())).thenReturn(remoteCall);
        boolean result = jumpCloudV1Service.resetUserMFA(userId, (int) exclusionPeriod);
        assertTrue(result);
    }

    @Test
    void resetUserMFA_returnFalseWhenNotSuccessful() throws IOException {
        when(credentialService.getJumpCloudToken()).thenReturn("xyz");
        long exclusionPeriod = 7;
        String userId = "abc";
        MediaType mediaType = MediaType.parse("application/json");
        String bodyString = null;
        LocalDateTime ldt = LocalDateTime.now().plusDays(exclusionPeriod);
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
        Response response = new Response.Builder().request(request).protocol(Protocol.HTTP_2).message("").body(ResponseBody.create(
                MediaType.get("application/json; charset=utf-8"),
                "{}"
        )).code(500).build();
        final Call remoteCall = mock(Call.class);
        when(remoteCall.execute()).thenReturn(response);
        when(okHttpClient.newCall(any())).thenReturn(remoteCall);
        boolean result = jumpCloudV1Service.resetUserMFA(userId, (int) exclusionPeriod);
        assertFalse(result);
    }

    @Test
    void unlockUserById_returnTrueWhenSuccessful() throws ApiException {
        String userId = "abc";
        doNothing().when(systemusersApi).systemusersUnlock(userId, contentType, accept, "");
        assertTrue(jumpCloudV1Service.unlockUserById(userId));
    }

    @Test
    void unlockUserById_returnFalseWhenUnSuccessful() throws ApiException {
        String userId = "abc";
        doThrow(new ApiException()).when(systemusersApi).systemusersUnlock(userId, contentType, accept, "");
        assertFalse(jumpCloudV1Service.unlockUserById(userId));
    }

    @Test
    void getAllSystems_returnSystemsWhenFound() throws ApiException {
        List<System> systems = new ArrayList<>();
        System system = new System();
        system.setId("abc");
        systems.add(system);
        Systemslist systemslist = new Systemslist();
        systemslist.setResults(systems);
        systemslist.setTotalCount(1);
        when(systemsApi.systemsList(contentType, accept, null, 100, "", null, 0, null, null)).thenReturn(systemslist);
        List<System> result = jumpCloudV1Service.getAllSystems();
        assertEquals(result, systems);
    }

    @Test
    void getAllSystems_returnNullWhenNotFound() throws ApiException {
        when(systemsApi.systemsList(contentType, accept, null, 100, "", null, 0, null, null)).thenThrow(new ApiException());
        List<System> result = jumpCloudV1Service.getAllSystems();
        assertNull(result);
    }

    @Test
    void manageSystemTOTPLogin_returnTrueWhenSuccessful() throws ApiException {
        boolean enableTOTP = true;
        String userId = "abc";
        Systemput body = new Systemput();
        body.allowMultiFactorAuthentication(enableTOTP);
        when(systemsApi.systemsPut(userId, contentType, accept, body, null, null, "")).thenReturn(new System());
        boolean result = jumpCloudV1Service.manageSystemTOTPLogin(userId, enableTOTP);
        assertTrue(result);
    }

    @Test
    void manageSystemTOTPLogin_returnFalseWhenUnSuccessful() throws ApiException {
        boolean enableTOTP = true;
        String userId = "abc";
        Systemput body = new Systemput();
        body.allowMultiFactorAuthentication(enableTOTP);
        when(systemsApi.systemsPut(userId, contentType, accept, body, null, null, "")).thenThrow(new ApiException());
        boolean result = jumpCloudV1Service.manageSystemTOTPLogin(userId, enableTOTP);
        assertFalse(result);
    }
}
