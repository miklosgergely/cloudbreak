package com.sequenceiq.cloudbreak.service.workspace;

import java.util.Set;

import com.sequenceiq.authorization.resource.AuthorizationResource;
import com.sequenceiq.cloudbreak.workspace.model.User;
import com.sequenceiq.cloudbreak.workspace.model.Workspace;
import com.sequenceiq.cloudbreak.workspace.model.WorkspaceAwareResource;

public interface WorkspaceAwareResourceService<T extends WorkspaceAwareResource> {

    T createForLoggedInUser(T resource, Long workspaceId);

    T create(T resource, Long workspaceId, User user);

    T createWithMdcContextRestore(T resource, Workspace workspace, User user);

    T create(T resource, Workspace workspace, User user);

    T getByNameForWorkspaceId(String name, Long workspaceId);

    Set<T> getByNamesForWorkspaceId(Set<String> name, Long workspaceId);

    T getByNameForWorkspace(String name, Workspace workspace);

    Set<T> findAllByWorkspace(Workspace workspace);

    Set<T> findAllByWorkspaceId(Long workspaceId);

    T delete(T resource);

    T deleteWithMdcContextRestore(T resource);

    Set<T> delete(Set<T> resources);

    T deleteByNameFromWorkspace(String name, Long workspaceId);

    Set<T> deleteMultipleByNameFromWorkspace(Set<String> names, Long workspaceId);

    AuthorizationResource resource();

    Iterable<T> findAll();

    T pureSave(T resource);

    Iterable<T> pureSaveAll(Iterable<T> resources);
}
