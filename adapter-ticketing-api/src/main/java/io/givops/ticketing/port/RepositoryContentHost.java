package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketBrowserModels.SourceRevision;
import io.givops.ticketing.model.TicketModels.InstanceSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** Host-authorized immutable repository reads; no worktree, remote URL, automatic hydration or mutation is exposed.
 * The host validates the exact catalog binding, configured roots and credentials on capture and current checks.
 * Entry/byte exhaustion, unavailable revisions and unsupported links fail explicitly; never return truncated success.
 */
public interface RepositoryContentHost {
    Access captureAccess(InstanceSource source);
    interface Access {
        String context();
        boolean current(boolean locked);
        /** Null selects the configured ref. A full immutable revision never falls back to the current ref. */
        Snapshot resolve(String revision, TicketRequestGate gate);
        default Snapshot resolve(TicketRequestGate gate) { return resolve(null, gate); }
    }
    interface Snapshot extends AutoCloseable {
        SourceRevision revision();
        /** Recursive repository-relative entries: complete within the bound or explicit failure. */
        List<Entry> entries(String relativeRoot, int maximumEntries, TicketRequestGate gate);
        /** Exact-size bounded blob at this revision, confined to the authorized roots/configuration file. */
        Blob open(String repositoryRelativePath, long maximumBytes, TicketRequestGate gate);
        @Override default void close() { }
    }
    enum EntryKind { FILE, DIRECTORY, SYMLINK, SUBMODULE, LFS_POINTER }
    record Entry(String path, EntryKind kind, long size, String objectId) { }
    interface Blob extends AutoCloseable {
        InputStream stream();
        long size();
        String objectId();
        @Override void close() throws IOException;
    }
}
