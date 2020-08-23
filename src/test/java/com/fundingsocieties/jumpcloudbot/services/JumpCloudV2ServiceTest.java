package com.fundingsocieties.jumpcloudbot.services;

import com.slack.api.model.User;
import io.swagger.client.ApiException;
import io.swagger.client.api.*;
import io.swagger.client.model.*;
import org.hibernate.graph.Graph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class JumpCloudV2ServiceTest {
    private static String contentType = "application/json";
    private static String accept = "application/json";

    @Autowired
    private JumpCloudV2Service jumpCloudV2Service;

    @MockBean
    private UsersApi usersApi;
    @MockBean
    private UserGroupsApi userGroupsApi;
    @MockBean
    private GraphApi graphApi;
    @MockBean
    private ApplicationsApi applicationsApi;
    @MockBean
    private SystemGroupsApi systemGroupsApi;

    @Test
    void getGroupById_shouldReturnGroupWhenFound() throws ApiException{
        String groupId = "xxx";
        UserGroup userGroup = new UserGroup();
        userGroup.setId(groupId);
        when(userGroupsApi.groupsUserGet(groupId, contentType, accept, null)).thenReturn(userGroup);
        UserGroup result = jumpCloudV2Service.getGroupById(groupId);
        assertEquals(userGroup.getId(),result.getId());
    }
    @Test
    void getGroupById_shouldReturnNullWhenExceptionThrown()throws ApiException{
        String groupId = "xxx";
        UserGroup userGroup = new UserGroup();
        userGroup.setId(groupId);
        when(userGroupsApi.groupsUserGet(groupId, contentType, accept, null)).thenThrow(new ApiException());
        UserGroup result = jumpCloudV2Service.getGroupById(groupId);
        assertNull(result);
    }

    @Test
    void getGroupForUser_shouldReturnGroupWhenFound()throws ApiException {
        List<UserGroup> userGroups = new ArrayList<>();
        String userId = "xxx";
        String groupId = "xxx";
        UserGroup userGroup = new UserGroup();
        userGroups.add(userGroup);
        userGroup.setId(groupId);
        List<GraphObjectWithPaths> fakeList = new ArrayList<>();
        GraphObjectWithPaths objectWithPaths = new GraphObjectWithPaths();
        objectWithPaths.setId("xxx");
        fakeList.add(objectWithPaths);
        when(usersApi.graphUserMemberOf(userId, contentType, accept, null, 10000, 0, null, "")).thenReturn(fakeList);
        when(userGroupsApi.groupsUserGet(groupId, contentType, accept, null)).thenReturn(userGroup);

        List<UserGroup> result = jumpCloudV2Service.getGroupsForAUser(userId);
        assertEquals(userGroups,result);
    }
    @Test
    void getGroupForUser_shouldReturnNullWhenExceptionThrown()throws ApiException {
        List<UserGroup> userGroups = new ArrayList<>();
        String userId = "xxx";
        String groupId = "xxx";
        UserGroup userGroup = new UserGroup();
        userGroups.add(userGroup);
        userGroup.setId(groupId);
        List<GraphObjectWithPaths> fakeList = new ArrayList<>();
        GraphObjectWithPaths objectWithPaths = new GraphObjectWithPaths();
        objectWithPaths.setId("xxx");
        fakeList.add(objectWithPaths);
        when(usersApi.graphUserMemberOf(userId, contentType, accept, null, 10000, 0, null, "")).thenThrow(new ApiException());
        when(userGroupsApi.groupsUserGet(groupId, contentType, accept, null)).thenReturn(userGroup);

        List<UserGroup> result = jumpCloudV2Service.getGroupsForAUser(userId);
        assertNull(result);
    }
    @Test
    void getAllGroup_returnSuccessfully() throws ApiException{
        List<UserGroup> tempResults = new ArrayList<UserGroup>();
        when(userGroupsApi.groupsUserList(contentType, accept, null, null, 100, 0, null, "")).thenReturn(tempResults);
        List<UserGroup> results = jumpCloudV2Service.getAllUserGroups();
        assertEquals(results,tempResults);
    }
    @Test
    void getAllGroup_shouldReturnNullWhenException() throws ApiException{
        List<UserGroup> tempResults = new ArrayList<UserGroup>();
        when(userGroupsApi.groupsUserList(contentType, accept, null, null, 100, 0, null, "")).thenThrow(new ApiException());
        List<UserGroup> results = jumpCloudV2Service.getAllUserGroups();
        assertNull(results);
    }
    @Test
    void removeUserFromUserGroup_returnSuccessfully() throws ApiException{
        UserGroupMembersReq body = new UserGroupMembersReq();
        String userId = "xxx";
        String groupId = "xxx";
        body.setId(userId);
        body.setOp(UserGroupMembersReq.OpEnum.REMOVE);
        body.setType(UserGroupMembersReq.TypeEnum.USER);
        String xOrgId = "";
        doNothing().when(graphApi).graphUserGroupMembersPost(groupId, contentType, accept, body, xOrgId);
        assertTrue(jumpCloudV2Service.removeUserFromUserGroup(groupId,userId));
    }
    @Test
    void removeUserFromUserGroup_shouldReturnFalseWhenExceptionThrown() throws ApiException{
        UserGroupMembersReq body = new UserGroupMembersReq();
        String userId = "xxx";
        String groupId = "xxx";
        body.setId(userId);
        body.setOp(UserGroupMembersReq.OpEnum.REMOVE);
        body.setType(UserGroupMembersReq.TypeEnum.USER);
        String xOrgId = "";
        doThrow(new ApiException()).when(graphApi).graphUserGroupMembersPost(groupId, contentType, accept, body, xOrgId);
        assertFalse(jumpCloudV2Service.removeUserFromUserGroup(groupId,userId));
    }
    @Test
    void addUserFromUserGroup_returnSuccessfully() throws ApiException{
        UserGroupMembersReq body = new UserGroupMembersReq();
        String userId = "xxx";
        String groupId = "xxx";
        body.setId(userId);
        body.setOp(UserGroupMembersReq.OpEnum.ADD);
        body.setType(UserGroupMembersReq.TypeEnum.USER);
        String xOrgId = "";
        doNothing().when(graphApi).graphUserGroupMembersPost(groupId, contentType, accept, body, xOrgId);
        assertTrue(jumpCloudV2Service.removeUserFromUserGroup(groupId,userId));
    }
    @Test
    void addUserFromUserGroup_shouldReturnFalseWhenExceptionThrown() throws ApiException{
        UserGroupMembersReq body = new UserGroupMembersReq();
        String userId = "xxx";
        String groupId = "xxx";
        body.setId(userId);
        body.setOp(UserGroupMembersReq.OpEnum.ADD);
        body.setType(UserGroupMembersReq.TypeEnum.USER);
        String xOrgId = "";
        doThrow(new ApiException()).when(graphApi).graphUserGroupMembersPost(groupId, contentType, accept, body, xOrgId);
        assertFalse(jumpCloudV2Service.addUserToUserGroup(groupId,userId));
    }
    @Test
    void getSystemsForAUser_shouldReturnSystemsWhenSuccessful() throws ApiException{
        String userId = "xxx";
        List<GraphObjectWithPaths> systems = new ArrayList<>();
        when(usersApi.graphUserTraverseSystem(userId, contentType, accept, 100000, "", 0, null)).thenReturn(systems);
        List<GraphObjectWithPaths> results = jumpCloudV2Service.getSystemsForAUser(userId);
        assertEquals(systems, results);
    }
    @Test
    void getSystemsForAUser_shouldReturnNullWhenExceptionThrown() throws ApiException{
        String userId = "xxx";
        List<GraphObjectWithPaths> systems = new ArrayList<>();
        when(usersApi.graphUserTraverseSystem(userId, contentType, accept, 100000, "", 0, null)).thenThrow(new ApiException());
        List<GraphObjectWithPaths> results = jumpCloudV2Service.getSystemsForAUser(userId);
        assertNull(results);
    }
    @Test
    void manageUserSystemBinding_shouldReturnTrueWhenSuccessful() throws ApiException{
        String userId = "xxx";
        String systemId = "xxx";
        String groupId = "xxx";
        SystemGraphManagementReq.OpEnum op = SystemGraphManagementReq.OpEnum.ADD;
        SystemGraphManagementReq body = new SystemGraphManagementReq();
        body.setType(SystemGraphManagementReq.TypeEnum.USER);
        body.setOp(op);
        body.setId(userId);
        String xOrgId = "";
        doNothing().when(graphApi).graphSystemAssociationsPost(systemId, contentType, accept, body, null, null, xOrgId);
        assertTrue(jumpCloudV2Service.manageUserSystemBinding(userId,groupId,op));
    }
    @Test
    void manageUserSystemBinding_shouldReturnFalseWhenExceptionThrown() throws ApiException{
        String userId = "xxx";
        String systemId = "xxx";
        String groupId = "xxx";
        SystemGraphManagementReq.OpEnum op = SystemGraphManagementReq.OpEnum.ADD;
        SystemGraphManagementReq body = new SystemGraphManagementReq();
        body.setType(SystemGraphManagementReq.TypeEnum.USER);
        body.setOp(op);
        body.setId(userId);
        String xOrgId = "";
        doThrow(new ApiException()).when(graphApi).graphSystemAssociationsPost(systemId, contentType, accept, body, null, null, xOrgId);
        assertFalse(jumpCloudV2Service.manageUserSystemBinding(userId,groupId,op));
    }
    @Test
    void getUserBoundSystem_returnObjectWhenSuccessful() throws ApiException{
        String systemId = "xxx";
        List<GraphObjectWithPaths> userList = new ArrayList<GraphObjectWithPaths>();
        when(graphApi.graphSystemTraverseUser(systemId, contentType, accept, 1000, "", 0, null, null, null)).thenReturn(userList);
        List<GraphObjectWithPaths> result = jumpCloudV2Service.getUserBoundSystem(systemId);
        assertEquals(result,userList);
    }
    @Test
    void getUserBoundSystem_returnNullWhenExceptionThrown() throws ApiException{
        String systemId = "xxx";
        List<GraphObjectWithPaths> userList = new ArrayList<GraphObjectWithPaths>();
        when(graphApi.graphSystemTraverseUser(systemId, contentType, accept, 1000, "", 0, null, null, null)).thenThrow(new ApiException());
        List<GraphObjectWithPaths> result = jumpCloudV2Service.getUserBoundSystem(systemId);
        assertNull(result);
    }
    @Test
    void getApplicationsForAUser_returnObjectWhenSuccesful() throws ApiException{
        String userId = "xxx";
        List<GraphObjectWithPaths> applicationList = new ArrayList<GraphObjectWithPaths>();
        when(graphApi.graphUserTraverseApplication(userId, contentType, accept, 1000, "", 0, null)).thenReturn(applicationList);
        List<GraphObjectWithPaths> result = jumpCloudV2Service.getApplicationsForAUser(userId);
        assertEquals(applicationList, result);
    }
    @Test
    void getApplicationsForAUser_returnNullWhenExceptionThrown() throws ApiException{
        String userId = "xxx";
        List<GraphObjectWithPaths> applicationList = new ArrayList<GraphObjectWithPaths>();
        when(graphApi.graphUserTraverseApplication(userId, contentType, accept, 1000, "", 0, null)).thenThrow(new ApiException());
        List<GraphObjectWithPaths> result = jumpCloudV2Service.getApplicationsForAUser(userId);
        assertNull(result);
    }
    @Test
    void getUserGroupForApplication_returnObjectWhenSuccessful() throws ApiException{
        String applicationId="xxx";
        List<GraphObjectWithPaths> userGroup = new ArrayList<GraphObjectWithPaths>();
        when(applicationsApi.graphApplicationTraverseUserGroup(applicationId, contentType, accept, 1000, "", null, null)).thenReturn(userGroup);
        List<GraphObjectWithPaths> result = jumpCloudV2Service.getUserGroupForApplication(applicationId);
        assertEquals(userGroup,result);
    }
    @Test
    void getUserGroupForApplication_returnNullWhenExceptionThrown() throws ApiException{
        String applicationId="xxx";
        List<GraphObjectWithPaths> userGroup = new ArrayList<GraphObjectWithPaths>();
        when(applicationsApi.graphApplicationTraverseUserGroup(applicationId, contentType, accept, 1000, "", null, null)).thenThrow(new ApiException());
        List<GraphObjectWithPaths> result = jumpCloudV2Service.getUserGroupForApplication(applicationId);
        assertNull(result);
    }
    @Test
    void getUsersForUserGroup_returnObjectWhenSuccessful() throws ApiException{
        String userGroupId = "xxx";
        List<GraphConnection> userList = new ArrayList<GraphConnection>();
        when(graphApi.graphUserGroupMembersList(userGroupId, contentType, accept, null, 0, "")).thenReturn(userList);
        List<GraphConnection> result = jumpCloudV2Service.getUsersForUserGroup(userGroupId);
        assertEquals(userList,result);
    }
    @Test
    void getUsersForUserGroup_returnNullWhenExceptionThrown() throws ApiException{
        String userGroupId = "xxx";
        List<GraphConnection> userList = new ArrayList<GraphConnection>();
        when(graphApi.graphUserGroupMembersList(userGroupId, contentType, accept, 10,0, "")).thenThrow(new ApiException());
        List<GraphConnection> result = jumpCloudV2Service.getUsersForUserGroup(userGroupId);
        assertNull(result);
    }
    @Test
    void getAllSystemGroups_returnObjectWhenSuccessful() throws ApiException{
        List<SystemGroup> tempResults = new ArrayList<SystemGroup>();
        when(systemGroupsApi.groupsSystemList(contentType, accept, null, null, 100, 0, null, "")).thenReturn(tempResults);
        List<SystemGroup> result = jumpCloudV2Service.getAllSystemGroups();
        assertEquals(tempResults,result);
    }
    @Test
    void getAllSystemGroups_returnNullWhenExceptionThrown() throws ApiException{
        List<SystemGroup> tempResults = new ArrayList<SystemGroup>();
        when(systemGroupsApi.groupsSystemList(contentType, accept, null, null, 100, 0, null, "")).thenThrow(new ApiException());
        List<SystemGroup> result = jumpCloudV2Service.getAllSystemGroups();
        assertNull(result);
    }
    @Test
    void manageSystemInSystemGroup_returnTrueWhenSuccessful() throws ApiException{
        String systemId = "xxx";
        String systemGroupId = "xxx";
        SystemGroupMembersReq.OpEnum op = SystemGroupMembersReq.OpEnum.ADD;
        SystemGroupMembersReq body = new SystemGroupMembersReq();
        body.setId(systemId);
        body.setOp(op);
        body.setType(SystemGroupMembersReq.TypeEnum.SYSTEM);
        doNothing().when(graphApi).graphSystemGroupMembersPost(systemGroupId, contentType, accept, body, null, null, "");
        assertTrue(jumpCloudV2Service.manageSystemInSystemGroup(systemId,systemGroupId,op));
    }
    @Test
    void manageSystemInSystemGroup_returnFalseWhenExceptionThrown() throws ApiException{
        String systemId = "xxx";
        String systemGroupId = "xxx";
        SystemGroupMembersReq.OpEnum op = SystemGroupMembersReq.OpEnum.ADD;
        SystemGroupMembersReq body = new SystemGroupMembersReq();
        body.setId(systemId);
        body.setOp(op);
        body.setType(SystemGroupMembersReq.TypeEnum.SYSTEM);
        doThrow(new ApiException()).when(graphApi).graphSystemGroupMembersPost(systemGroupId, contentType, accept, body, null, null, "");
        assertFalse(jumpCloudV2Service.manageSystemInSystemGroup(systemId,systemGroupId,op));
    }


}
