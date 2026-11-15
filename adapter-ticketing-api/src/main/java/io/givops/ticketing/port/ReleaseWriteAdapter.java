package io.givops.ticketing.port;
import io.givops.release.commands.ReleaseCommandModels.Configuration;
import io.givops.release.commands.ReleaseCommandModels.ExistingReport;
import io.givops.release.commands.ReleaseCommandModels.Kind;
import io.givops.release.commands.ReleaseCommandModels.Request;
import io.givops.ticketing.model.TicketModels.InstanceSource;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import java.time.LocalDate;
public interface ReleaseWriteAdapter extends AutoCloseable {
    TicketProvider provider();
    interface Session { String context(); }
    Session capture(InstanceSource source);
    void requireCurrent(Session session, boolean locked);
    String version(Session session, String projectId, String name, TicketRequestGate gate);
    String createVersion(Session session, String projectId, String name, LocalDate plannedDate, TicketRequestGate gate);
    ExistingReport duplicate(Session session, String destinationId, Configuration config, Kind kind,
                             String versionName, String versionId, TicketRequestGate gate);
    ExistingReport createReport(Session session, String destinationId, Configuration config, Request request,
                                String versionId, String assignee, TicketRequestGate gate);
    @Override default void close() { }
    class WriteFailure extends RuntimeException {
        private final String code; private final boolean uncertain;
        private final io.givops.adapters.FailureCategory category;
        public WriteFailure(String code, boolean uncertain) {
            this(code, uncertain, io.givops.adapters.i18n.SdkMessages.format("sdk.release.unconfirmed"));
        }
        public WriteFailure(String code, boolean uncertain, String safeMessage) {
            this(code, uncertain, safeMessage, io.givops.adapters.FailureCategory.UNKNOWN);
        }
        public WriteFailure(String code, boolean uncertain, String safeMessage, io.givops.adapters.FailureCategory category) {
            super(safeMessage); this.code = code; this.uncertain = uncertain; this.category = java.util.Objects.requireNonNull(category);
        }
        public io.givops.adapters.FailureCategory category() { return category; }
        public String code() { return code; }
        public boolean uncertain() { return uncertain; }
    }
}
