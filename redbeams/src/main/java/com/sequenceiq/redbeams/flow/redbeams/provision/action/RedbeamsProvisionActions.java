package com.sequenceiq.redbeams.flow.redbeams.provision.action;

import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.scheduler.CancellationException;
import com.sequenceiq.cloudbreak.cloud.transform.ResourceLists;
import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.flow.core.Flow;
import com.sequenceiq.flow.core.FlowParameters;
import com.sequenceiq.redbeams.api.model.common.DetailedDBStackStatus;
import com.sequenceiq.redbeams.flow.redbeams.common.RedbeamsContext;
import com.sequenceiq.redbeams.flow.redbeams.common.RedbeamsEvent;
import com.sequenceiq.redbeams.flow.redbeams.common.RedbeamsFailureEvent;
import com.sequenceiq.redbeams.flow.redbeams.provision.AbstractRedbeamsProvisionAction;
import com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent;
import com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionState;
import com.sequenceiq.redbeams.flow.redbeams.provision.event.allocate.AllocateDatabaseServerRequest;
import com.sequenceiq.redbeams.flow.redbeams.provision.event.allocate.AllocateDatabaseServerSuccess;
import com.sequenceiq.redbeams.flow.redbeams.provision.event.register.RegisterDatabaseServerRequest;
import com.sequenceiq.redbeams.flow.redbeams.provision.event.register.RegisterDatabaseServerSuccess;
import com.sequenceiq.redbeams.service.stack.DBStackStatusUpdater;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

@Configuration
public class RedbeamsProvisionActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedbeamsProvisionActions.class);

    @Inject
    private DBStackStatusUpdater dbStackStatusUpdater;

    @Bean(name = "ALLOCATE_DATABASE_SERVER_STATE")
    public Action<?, ?> allocateDatabaseServer() {
        return new AbstractRedbeamsProvisionAction<>(RedbeamsEvent.class) {

            @Override
            protected void prepareExecution(RedbeamsEvent payload, Map<Object, Object> variables) {
                dbStackStatusUpdater.updateStatus(payload.getResourceId(), DetailedDBStackStatus.CREATING_INFRASTRUCTURE);
            }

            @Override
            protected Selectable createRequest(RedbeamsContext context) {
                return new AllocateDatabaseServerRequest(context.getCloudContext(), context.getCloudCredential(),
                        context.getDatabaseStack());
            }
        };
    }

    @Bean(name = "REGISTER_DATABASE_SERVER_STATE")
    public Action<?, ?> registerDatabaseServer() {
        return new AbstractRedbeamsProvisionAction<>(AllocateDatabaseServerSuccess.class) {

            private List<CloudResource> dbResources;

            @Override
            protected void prepareExecution(AllocateDatabaseServerSuccess payload, Map<Object, Object> variables) {
                dbStackStatusUpdater.updateStatus(payload.getResourceId(), DetailedDBStackStatus.PROVISIONED);
                dbResources = ResourceLists.transform(payload.getResults());
            }

            @Override
            protected Selectable createRequest(RedbeamsContext context) {
                return new RegisterDatabaseServerRequest(context.getCloudContext(), context.getDBStack(), dbResources);
            }
        };
    }

    @Bean(name = "REDBEAMS_PROVISION_FINISHED_STATE")
    public Action<?, ?> provisionFinished() {
        return new AbstractRedbeamsProvisionAction<>(RegisterDatabaseServerSuccess.class) {

            @Override
            protected void prepareExecution(RegisterDatabaseServerSuccess payload, Map<Object, Object> variables) {
                dbStackStatusUpdater.updateStatus(payload.getResourceId(), DetailedDBStackStatus.AVAILABLE);
            }

            @Override
            protected Selectable createRequest(RedbeamsContext context) {
                return new RedbeamsEvent(RedbeamsProvisionEvent.REDBEAMS_PROVISION_FINISHED_EVENT.name(), 0L);
            }
        };
    }

    @Bean(name = "REDBEAMS_PROVISION_FAILED_STATE")
    public Action<?, ?> provisionFailed() {
        return new AbstractRedbeamsProvisionAction<>(RedbeamsFailureEvent.class) {

            // A lot here - some of this could go into some sort of failure handler class
            // compare to core StackCreationService::handleStackCreationFailure

            @Override
            protected void prepareExecution(RedbeamsFailureEvent payload, Map<Object, Object> variables) {

                Exception failureException = payload.getException();
                LOGGER.info("Error during database stack creation flow:", failureException);

                if (failureException instanceof CancellationException || ExceptionUtils.getRootCause(failureException) instanceof CancellationException) {
                    LOGGER.debug("The flow has been cancelled");
                } else {
                    // StackCreationActions / StackCreationService only update status if stack isn't mid-deletion
                    String errorReason = failureException == null ? "Unknown error" : failureException.getMessage();
                    dbStackStatusUpdater.updateStatus(payload.getResourceId(), DetailedDBStackStatus.PROVISION_FAILED, errorReason);
                }

            }

            @Override
            protected RedbeamsContext createFlowContext(FlowParameters flowParameters,
                StateContext<RedbeamsProvisionState, RedbeamsProvisionEvent> stateContext, RedbeamsFailureEvent payload) {

                Flow flow = getFlow(flowParameters.getFlowId());
                flow.setFlowFailed(payload.getException());

                // FIXME perhaps better to use something like StackFailureContext / AbstractStackFailureAction
                // - leave out cloud context, cloud credentials, converted database stack
                // - only "view" of dbstack, but not sure why that matters though

                return super.createFlowContext(flowParameters, stateContext, payload);
            }

            @Override
            protected Selectable createRequest(RedbeamsContext context) {
                return new RedbeamsEvent(RedbeamsProvisionEvent.REDBEAMS_PROVISION_FAILURE_HANDLED_EVENT.event(), 0L);
            }
        };
    }
}
