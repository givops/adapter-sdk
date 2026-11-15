package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketWriteModels.RemoteProjectRef;
import io.givops.ticketing.model.TicketModels.InstanceSource;
import io.givops.ticketing.model.TicketModels.IssueObservation;
import io.givops.ticketing.model.TicketModels.ProjectObservation;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import io.givops.ticketing.model.TicketModels.VersionObservation;
import io.givops.ticketing.model.TicketWriteModels.CreateIssueCommand;
import io.givops.ticketing.model.TicketWriteModels.CreateProjectCommand;
import io.givops.ticketing.model.TicketWriteModels.CreateVersionCommand;

/** Port implemented by ticketing providers that support creating remote projects, versions, and issues. */
public interface TicketWriteAdapter {
    TicketProvider provider();

    TicketProviderAdapter.Access captureAccess(InstanceSource source);

    void requireCurrent(InstanceSource source, TicketProviderAdapter.Access captured, boolean locked);

    ProjectObservation createProject(
            InstanceSource source,
            CreateProjectCommand command,
            TicketProviderAdapter.Access captured,
            TicketRequestGate gate);

    VersionObservation createVersion(
            InstanceSource source,
            RemoteProjectRef project,
            CreateVersionCommand command,
            TicketProviderAdapter.Access captured,
            TicketRequestGate gate);

    IssueObservation createIssue(
            InstanceSource source,
            RemoteProjectRef project,
            CreateIssueCommand command,
            TicketProviderAdapter.Access captured,
            TicketRequestGate gate);
}
