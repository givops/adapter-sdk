package io.givops.testreports.port;

import io.givops.testreports.model.TestReportProviderModels.MetadataSnapshot;
import io.givops.testreports.model.TestReportProviderModels.OrdinaryDataset;
import io.givops.testreports.model.TestReportProviderModels.ProbeResult;
import io.givops.testreports.model.TestReportProviderModels.ReportDataset;
import io.givops.testreports.model.TestReportProviderModels.ReportInput;
import io.givops.ticketing.model.TicketModels.InstanceSource;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import io.givops.ticketing.port.TicketProviderAdapter.Access;
import io.givops.ticketing.port.TicketRequestGate;

public interface TestReportProviderAdapter {
    TicketProvider provider();
    Access captureAccess(InstanceSource source);
    boolean current(InstanceSource source, Access access, boolean locked);
    MetadataSnapshot metadata(ReportInput input, Access access, TicketRequestGate gate);
    ProbeResult probe(ReportInput input, Access access, TicketRequestGate gate);
    ReportDataset reports(ReportInput input, Access access, TicketRequestGate gate);
    OrdinaryDataset ordinaryVersions(ReportInput input, Access access, TicketRequestGate gate);
}
