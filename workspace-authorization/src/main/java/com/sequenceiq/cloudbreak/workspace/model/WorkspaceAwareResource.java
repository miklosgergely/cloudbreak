package com.sequenceiq.cloudbreak.workspace.model;


import org.springframework.security.access.AccessDeniedException;

import com.sequenceiq.authorization.resource.AuthorizationResource;

public interface WorkspaceAwareResource extends TenantAwareResource {

    Long getId();

    Workspace getWorkspace();

    String getName();

    void setWorkspace(Workspace workspace);

    AuthorizationResource getResource();

    @Override
    default Tenant getTenant() {
        if (getWorkspace() == null) {
            throw new AccessDeniedException(String.format("Workspace cannot be null for object: %s with name: %s",
                    getClass().toString(), (getName() == null) ? "name not provided" : getName()));
        }
        return getWorkspace().getTenant();
    }
}
