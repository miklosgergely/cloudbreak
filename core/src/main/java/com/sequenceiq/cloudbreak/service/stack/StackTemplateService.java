package com.sequenceiq.cloudbreak.service.stack;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.authorization.resource.AuthorizationResource;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.workspace.repository.workspace.WorkspaceResourceRepository;
import com.sequenceiq.cloudbreak.service.AbstractWorkspaceAwareResourceService;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;

@Service
public class StackTemplateService extends AbstractWorkspaceAwareResourceService<Stack> {

    @Inject
    private StackRepository stackRepository;

    @Inject
    private ClusterService clusterService;

    public Optional<Stack> getByIdWithLists(Long id) {
        return stackRepository.findTemplateWithLists(id);
    }

    @Override
    protected WorkspaceResourceRepository<Stack, Long> repository() {
        return stackRepository;
    }

    @Override
    protected void prepareDeletion(Stack resource) {
        clusterService.pureDelete(resource.getCluster());
    }

    @Override
    protected void prepareCreation(Stack resource) {

    }

    @Override
    public AuthorizationResource resource() {
        return AuthorizationResource.DATAHUB;
    }

}
