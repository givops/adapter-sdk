package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketException;
import io.givops.ticketing.model.TicketException.Code;
import io.givops.ticketing.model.TicketModels.InstanceSource;
import io.givops.ticketing.model.TicketModels.IssueObservation;
import io.givops.ticketing.model.TicketModels.ProjectQuery;
import io.givops.ticketing.model.TicketModels.ProviderInput;
import io.givops.ticketing.model.TicketModels.ProviderSnapshot;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import io.givops.ticketing.model.TicketModels.VersionObservation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface TicketProviderAdapter {
    TicketProvider provider();

    /** Returns a stable opaque context marker for a usable credential selection. */
    String authenticationContext(InstanceSource source);

    interface Access {
        String context();
    }

    default Access captureAccess(InstanceSource source) {
        String context = authenticationContext(source);
        return () -> context;
    }

    /** Revalidate the captured credential lease; locked=true must enforce host transaction freshness.
     * There is deliberately no context-comparison default: it cannot provide a publication lock.
     */
    boolean current(InstanceSource source, Access captured, boolean locked);

    default ProviderSnapshot read(ProviderInput input, TicketRequestGate gate, Access captured) {
        return read(input, gate);
    }

    ProviderSnapshot read(ProviderInput input, TicketRequestGate gate);

    /** Reads complete membership for the selected provider version IDs in a bound ticket project. */
    default VersionMemberships readVersionMemberships(InstanceSource source, ProjectQuery query, Access captured,
                                                      TicketRequestGate gate,
                                                      Function<List<VersionObservation>, Set<String>> select,
                                                      int maximumVersions, int maximumIssues) {
        throw new TicketException(Code.UNSUPPORTED_PROVIDER, io.givops.adapters.i18n.SdkMessages.format("sdk.provider.version.membership.unsupported"));
    }

    record VersionMemberships(Map<String, List<IssueObservation>> members) {
        public VersionMemberships {
            var copy = new LinkedHashMap<String, List<IssueObservation>>();
            members.forEach((id, issues) -> copy.put(id, List.copyOf(issues)));
            members = Map.copyOf(copy);
        }
    }
}
