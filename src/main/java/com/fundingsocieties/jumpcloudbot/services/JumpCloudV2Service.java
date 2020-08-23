package com.fundingsocieties.jumpcloudbot.services;

import io.swagger.client.ApiException;
import io.swagger.client.api.GraphApi;
import io.swagger.client.api.SystemGroupsApi;
import io.swagger.client.api.UserGroupsApi;
import io.swagger.client.api.UsersApi;
import io.swagger.client.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JumpCloudV2Service {

    @Autowired
    private UsersApi usersApi;

    @Autowired
    private UserGroupsApi userGroupsApi;

    @Autowired
    private GraphApi graphApi;

    @Autowired
    private io.swagger.client.api.ApplicationsApi applicationsApi;

    @Autowired
    private SystemGroupsApi systemGroupsApi;

    private static String contentType = "application/json";
    private static String accept = "application/json";

    public List<UserGroup> getGroupsForAUser(String userId) {
        try {
            return usersApi.graphUserMemberOf(userId, contentType, accept, null, 10000, 0, null, "")
                    .stream()
                    .map(graphObjectWithPaths -> getGroupById(graphObjectWithPaths.getId()))
                    .collect(Collectors.toList());
        } catch (ApiException ex) {
            return null;
        }
    }

    public UserGroup getGroupById(String id) {
        try {
            return userGroupsApi.groupsUserGet(id, contentType, accept, null);
        } catch (ApiException ex) {
            return null;
        }
    }

    public List<UserGroup> getAllUserGroups() {
        try {
            int skip = 0;
            List<UserGroup> result = new ArrayList<>();
            boolean hasResults = true;
            while (hasResults) {
                List<UserGroup> tempResults = userGroupsApi.groupsUserList(contentType, accept, null, null, 100, skip, null, "");
                if (tempResults != null && tempResults.size() > 0) {
                    result.addAll(tempResults);
                    skip = skip + 100;
                } else {
                    hasResults = false;
                }
            }
            return result;
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean removeUserFromUserGroup(String groupId, String userId) {
        UserGroupMembersReq body = new UserGroupMembersReq();
        body.setId(userId);
        body.setOp(UserGroupMembersReq.OpEnum.REMOVE);
        body.setType(UserGroupMembersReq.TypeEnum.USER);
        String xOrgId = "";
        try {
            graphApi.graphUserGroupMembersPost(groupId, contentType, accept, body, xOrgId);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addUserToUserGroup(String groupId, String userId) {
        UserGroupMembersReq body = new UserGroupMembersReq();
        body.setId(userId);
        body.setOp(UserGroupMembersReq.OpEnum.ADD);
        body.setType(UserGroupMembersReq.TypeEnum.USER);
        String xOrgId = "";
        try {
            graphApi.graphUserGroupMembersPost(groupId, contentType, accept, body, xOrgId);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<GraphObjectWithPaths> getSystemsForAUser(String userId) {
        try {
            List<GraphObjectWithPaths> systems = usersApi
                    .graphUserTraverseSystem(userId, contentType, accept, 100000, "", 0, null);
            return systems;
        } catch (ApiException ex) {
            return null;
        }
    }

    public boolean manageUserSystemBinding(String userId, String systemId, SystemGraphManagementReq.OpEnum op) {
        SystemGraphManagementReq body = new SystemGraphManagementReq();
        body.setType(SystemGraphManagementReq.TypeEnum.USER);
        body.setOp(op);
        body.setId(userId);
        String xOrgId = "";
        try {
            graphApi.graphSystemAssociationsPost(systemId, contentType, accept, body, null, null, xOrgId);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<GraphObjectWithPaths> getUserBoundSystem(String systemId) {
        try {
            List<GraphObjectWithPaths> result = graphApi.graphSystemTraverseUser(systemId, contentType, accept, 1000, "", 0, null, null, null);
            return result;
        } catch (ApiException ex) {
            return null;
        }
    }

    public List<GraphObjectWithPaths> getApplicationsForAUser(String userId) {
        try {
            List<GraphObjectWithPaths> result = graphApi.graphUserTraverseApplication(userId, contentType, accept, 1000, "", 0, null);
            return result;
        } catch (ApiException ex) {
            return null;
        }
    }


    public List<GraphObjectWithPaths> getUserGroupForApplication(String applicationId) {
        try {
            List<GraphObjectWithPaths> result = applicationsApi.graphApplicationTraverseUserGroup(applicationId, contentType, accept, 1000, "", null, null);
            return result;
        } catch (ApiException ex) {
            return null;
        }
    }

    public List<GraphConnection> getUsersForUserGroup(String userGroupId) {
        try {
            int i = 0;
            List<GraphConnection> result = new ArrayList<>();
            List<GraphConnection> fetchedResult = new ArrayList<>();
            boolean hasResults = true;
            while (hasResults) {
                fetchedResult = graphApi.graphUserGroupMembersList(userGroupId, contentType, accept, 10, i, "");
                if (fetchedResult == null || fetchedResult.size() == 0) {
                    hasResults = false;
                } else {
                    result.addAll(fetchedResult);
                    i = i + 10;
                }
            }
            return result;
        } catch (ApiException ex) {
            return null;
        }
    }

    public List<SystemGroup> getAllSystemGroups() {
        try {
            int skip = 0;
            List<SystemGroup> result = new ArrayList<>();
            boolean hasResults = true;
            while (hasResults) {
                List<SystemGroup> tempResults = systemGroupsApi.groupsSystemList(contentType, accept, null, null, 100, skip, null, "");
                if (tempResults != null && tempResults.size() > 0) {
                    result.addAll(tempResults);
                    skip = skip + 100;
                } else {
                    hasResults = false;
                }
            }
            return result;
        } catch (ApiException ex) {
            return null;
        }
    }


    public boolean manageSystemInSystemGroup(String systemId, String systemGroupId, SystemGroupMembersReq.OpEnum op) {
        try {

            SystemGroupMembersReq body = new SystemGroupMembersReq();
            body.setId(systemId);
            body.setOp(op);
            body.setType(SystemGroupMembersReq.TypeEnum.SYSTEM);
            graphApi.graphSystemGroupMembersPost(systemGroupId, contentType, accept, body, null, null, "");
            return true;
        } catch (ApiException ex) {
            return false;
        }
    }


}
