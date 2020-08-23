package com.fundingsocieties.jumpcloudbot.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fundingsocieties.jumpcloudbot.services.CredentialService;
import io.swagger.client.ApiClient;
import io.swagger.client.api.GraphApi;
import io.swagger.client.api.SystemGroupsApi;
import io.swagger.client.api.UserGroupsApi;
import io.swagger.client.api.UsersApi;
import io.swagger.client.auth.ApiKeyAuth;
import io.swagger.clientv1.api.ApplicationsApi;
import io.swagger.clientv1.api.SearchApi;
import io.swagger.clientv1.api.SystemsApi;
import io.swagger.clientv1.api.SystemusersApi;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JumpCloudConfig {

    public static String JUMPCLOUD_V1_URL = "https://console.jumpcloud.com/api";

    public static String JUMPCLOUD_V2_URL = "https://console.jumpcloud.com/api/v2";

    @Autowired
    private CredentialService credentialService;

    @Bean
    public SearchApi searchApi() throws JsonProcessingException {
        String apiKey = credentialService.getJumpCloudToken();
        io.swagger.clientv1.ApiClient v1Client = new io.swagger.clientv1.ApiClient();
        v1Client.setApiKey(apiKey);
        v1Client.setBasePath(JUMPCLOUD_V1_URL);
        SearchApi searchApi = new SearchApi();
        searchApi.setApiClient(v1Client);
        return searchApi;
    }

    @Bean
    public SystemsApi systemsApi() throws JsonProcessingException {
        String apiKey = credentialService.getJumpCloudToken();
        io.swagger.clientv1.ApiClient v1Client = new io.swagger.clientv1.ApiClient();
        v1Client.setApiKey(apiKey);
        v1Client.setBasePath(JUMPCLOUD_V1_URL);
        SystemsApi systemsApi = new SystemsApi();
        systemsApi.setApiClient(v1Client);
        return systemsApi;
    }

    @Bean
    public SystemusersApi systemusersApi() throws JsonProcessingException {
        String apiKey = credentialService.getJumpCloudToken();
        io.swagger.clientv1.ApiClient v1Client = new io.swagger.clientv1.ApiClient();
        v1Client.setApiKey(apiKey);
        v1Client.setBasePath(JUMPCLOUD_V1_URL);
        SystemusersApi systemusersApi = new SystemusersApi();
        systemusersApi.setApiClient(v1Client);
        return systemusersApi;
    }

    @Bean
    public ApplicationsApi applicationsApiV1() throws JsonProcessingException {
        String apiKey = credentialService.getJumpCloudToken();
        io.swagger.clientv1.ApiClient v1Client = new io.swagger.clientv1.ApiClient();
        v1Client.setApiKey(apiKey);
        v1Client.setBasePath(JUMPCLOUD_V1_URL);
        ApplicationsApi applicationsApi = new ApplicationsApi();
        applicationsApi.setApiClient(v1Client);
        return applicationsApi;
    }

    @Bean
    public ApiClient v2Client() throws JsonProcessingException {
        String apiKey = credentialService.getJumpCloudToken();
        ApiClient v2Client = io.swagger.client.Configuration.getDefaultApiClient();
        ApiKeyAuth xApiKey = (ApiKeyAuth) v2Client.getAuthentication("x-api-key");
        xApiKey.setApiKey(apiKey);
        v2Client.setBasePath(JUMPCLOUD_V2_URL);
        return v2Client;
    }

    @Bean
    public UsersApi usersApi() throws JsonProcessingException {
        String apiKey = credentialService.getJumpCloudToken();
        ApiClient v2Client = io.swagger.client.Configuration.getDefaultApiClient();
        ApiKeyAuth xApiKey = (ApiKeyAuth) v2Client.getAuthentication("x-api-key");
        xApiKey.setApiKey(apiKey);
        v2Client.setBasePath(JUMPCLOUD_V2_URL);
        UsersApi usersApi  = new UsersApi();
        usersApi.setApiClient(v2Client);
        return usersApi;
    }

    @Bean
    public UserGroupsApi userGroupsApi() throws JsonProcessingException{
        String apiKey = credentialService.getJumpCloudToken();
        ApiClient v2Client = io.swagger.client.Configuration.getDefaultApiClient();
        ApiKeyAuth xApiKey = (ApiKeyAuth) v2Client.getAuthentication("x-api-key");
        xApiKey.setApiKey(apiKey);
        v2Client.setBasePath(JUMPCLOUD_V2_URL);
        UserGroupsApi userGroupsApi = new UserGroupsApi();
        userGroupsApi.setApiClient(v2Client);
        return userGroupsApi;
    }

    @Bean
    public GraphApi graphApi() throws JsonProcessingException{
        String apiKey = credentialService.getJumpCloudToken();
        ApiClient v2Client = io.swagger.client.Configuration.getDefaultApiClient();
        ApiKeyAuth xApiKey = (ApiKeyAuth) v2Client.getAuthentication("x-api-key");
        xApiKey.setApiKey(apiKey);
        v2Client.setBasePath(JUMPCLOUD_V2_URL);
        GraphApi graphApi = new GraphApi();
        graphApi.setApiClient(v2Client);
        return graphApi;
    }

    @Bean
    public io.swagger.client.api.ApplicationsApi applicationsApi() throws JsonProcessingException {
        String apiKey = credentialService.getJumpCloudToken();
        ApiClient v2Client = io.swagger.client.Configuration.getDefaultApiClient();
        ApiKeyAuth xApiKey = (ApiKeyAuth) v2Client.getAuthentication("x-api-key");
        xApiKey.setApiKey(apiKey);
        v2Client.setBasePath(JUMPCLOUD_V2_URL);
        io.swagger.client.api.ApplicationsApi applicationsApi = new io.swagger.client.api.ApplicationsApi();
        applicationsApi.setApiClient(v2Client);
        return applicationsApi;
    }

    @Bean
    public SystemGroupsApi systemGroupsApi() throws JsonProcessingException {
        String apiKey = credentialService.getJumpCloudToken();
        ApiClient v2Client = io.swagger.client.Configuration.getDefaultApiClient();
        ApiKeyAuth xApiKey = (ApiKeyAuth) v2Client.getAuthentication("x-api-key");
        xApiKey.setApiKey(apiKey);
        v2Client.setBasePath(JUMPCLOUD_V2_URL);
        SystemGroupsApi systemGroupsApi = new SystemGroupsApi();
        systemGroupsApi.setApiClient(v2Client);
        return systemGroupsApi;
    }

    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient().newBuilder()
                .build();
    }

}
