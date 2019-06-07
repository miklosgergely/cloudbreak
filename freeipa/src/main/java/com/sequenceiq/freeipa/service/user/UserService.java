package com.sequenceiq.freeipa.service.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.freeipa.api.v1.freeipa.user.model.CreateUsersRequest;
import com.sequenceiq.freeipa.api.v1.freeipa.user.model.Group;
import com.sequenceiq.freeipa.api.v1.freeipa.user.model.SynchronizationStatus;
import com.sequenceiq.freeipa.api.v1.freeipa.user.model.SynchronizeUsersRequest;
import com.sequenceiq.freeipa.api.v1.freeipa.user.model.SynchronizeUsersResponse;
import com.sequenceiq.freeipa.api.v1.freeipa.user.model.User;
import com.sequenceiq.freeipa.client.FreeIpaClient;
import com.sequenceiq.freeipa.client.FreeIpaClientException;
import com.sequenceiq.freeipa.client.model.RPCResponse;
import com.sequenceiq.freeipa.entity.Stack;
import com.sequenceiq.freeipa.service.FreeIpaClientFactory;
import com.sequenceiq.freeipa.service.stack.StackService;
import com.sequenceiq.freeipa.service.user.model.UsersState;
import com.sequenceiq.freeipa.service.user.model.UsersStateDifference;
import com.sequenceiq.freeipa.util.CrnService;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Inject
    private StackService stackService;

    @Inject
    private FreeIpaClientFactory freeIpaClientFactory;

    @Inject
    private ThreadBasedUserCrnProvider threadBaseUserCrnProvider;

    @Inject
    private CrnService crnService;

    @Inject
    private UmsUsersStateProvider umsUsersStateProvider;

    @Inject
    private FreeIpaUsersStateProvider freeIpaUsersStateProvider;

    public SynchronizeUsersResponse synchronizeUsers(SynchronizeUsersRequest request) {
        String accountId = crnService.getCurrentAccountId();
        List<Stack> stacks = stackService.getAllByAccountId(accountId);
        LOGGER.debug("Found {} stacks for account {}", stacks.size(), accountId);
        Set<String> environmentsFilter = request.getEnvironments();
        if (!environmentsFilter.isEmpty()) {
            stacks = stacks.stream()
                    .filter(stack -> environmentsFilter.contains(stack.getEnvironmentCrn()))
                    .collect(Collectors.toList());
        }
        for (Stack stack : stacks) {
            // TODO improve exception handling
            try {
                LOGGER.info("Syncing Environment {}", stack.getEnvironmentCrn());
                // TODO filter by users in request
                UsersState umsUsersState = umsUsersStateProvider.getUsersState(stack.getEnvironmentCrn());
                LOGGER.debug("UMS UsersState = {}", umsUsersState);
                // TODO filter by users in request
                UsersState ipaUsersState = freeIpaUsersStateProvider.getUsersState(stack);
                LOGGER.debug("IPA UsersState = {}", ipaUsersState);
                // TODO calculate group membership changes. this currently only picks up group membership for added users
                UsersStateDifference stateDifference = UsersStateDifference.fromUmsAndIpaUsersStates(umsUsersState, ipaUsersState);
                LOGGER.debug("State Difference = {}", stateDifference);
                FreeIpaClient freeIpaClient = freeIpaClientFactory.getFreeIpaClientForStack(stack);
                addGroups(freeIpaClient, stateDifference.getGroupsToAdd());
                addUsers(freeIpaClient, stateDifference.getUsersToAdd());
                // TODO remove/deactivate groups/users
            } catch (Exception e) {
                LOGGER.warn("Failed to syncronize environment {}", stack.getEnvironmentCrn());
            }
        }

        return new SynchronizeUsersResponse(UUID.randomUUID().toString(), SynchronizationStatus.FAILED,
                null, null);
    }

    public SynchronizeUsersResponse getSynchronizeUsersStatus(String syncId) {
        return new SynchronizeUsersResponse(syncId, SynchronizationStatus.FAILED,
                null, null);
    }

    public void createUsers(CreateUsersRequest request, String accountId) throws Exception {
        LOGGER.info("UserService.synchronizeUsers() called");

        Stack stack = stackService.getByEnvironmentCrnAndAccountId(request.getEnvironmentCrn(), accountId);

        FreeIpaClient freeIpaClient = freeIpaClientFactory.getFreeIpaClientForStack(stack);

        // TODO improve exception handling. e.g., group or user add will fail if group or user already exists
        // TODO batch calls for improved performance. i.e., get all members of each group so we call groupAddMember once per group. investigate batch api

        addGroups(freeIpaClient, request.getGroups());

        Set<User> users = request.getUsers();
        addUsers(freeIpaClient, users);

        addUsersToGroups(freeIpaClient, getGroupUserMapping(users));

        // TODO remove users from groups that are not specified
        // TODO remove groups that are not specified
        // TODO remove/disable users

        LOGGER.info("Done!");
    }

    private void addGroups(FreeIpaClient freeIpaClient, Set<Group> groups) throws FreeIpaClientException {
        for (Group group : groups) {
            LOGGER.debug("adding group {}", group.getName());
            try {
                com.sequenceiq.freeipa.client.model.Group groupAdd = freeIpaClient.groupAdd(group.getName());
                LOGGER.debug("Success: {}", groupAdd);
            } catch (FreeIpaClientException e) {
                // TODO propagate this information out to API
                LOGGER.error("Failed to add group {}", group.getName(), e);
            }
        }
    }

    private void addUsers(FreeIpaClient freeIpaClient, Set<User> users) throws FreeIpaClientException {
        for (User user : users) {
            String username = user.getName();

            LOGGER.debug("adding user {}", username);

            try {
                com.sequenceiq.freeipa.client.model.User userAdd = freeIpaClient.userAdd(
                        username, user.getFirstName(), user.getLastName(), generateRandomPassword());
                LOGGER.debug("Success: {}", userAdd);
            } catch (FreeIpaClientException e) {
                // TODO propagate this information out to API
                LOGGER.error("Failed to add {}", username, e);
            }
        }
    }

    private void addUsersToGroups(FreeIpaClient freeIpaClient, Map<String, Set<String>> groupMapping) throws FreeIpaClientException {
        for (Map.Entry<String, Set<String>> entry : groupMapping.entrySet()) {
            LOGGER.debug("adding users {} to group {}", entry.getValue(), entry.getKey());
            try {
                // TODO specialize response object
                RPCResponse<Object> groupAddMember = freeIpaClient.groupAddMembers(entry.getKey(), entry.getValue());
                LOGGER.debug("Success: {}", groupAddMember.getResult());
            } catch (FreeIpaClientException e) {
                // TODO propagate this information out to API
                LOGGER.error("Failed to add users {} to group {}", entry.getValue(), entry.getKey(), e);
            }
        }
    }

    private Map<String, Set<String>> getGroupUserMapping(Set<User> users) {
        Map<String, Set<String>> mapping = new HashMap<>();

        for (User user : users) {
            String username = user.getName();
            for (String group : user.getGroups()) {
                Set<String> members = mapping.get(group);
                if (members == null) {
                    members = new HashSet<>();
                    mapping.put(group, members);
                }
                members.add(username);
            }
        }
        return mapping;
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString();
    }
}