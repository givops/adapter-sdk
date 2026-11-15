# Synthetic adapter replacement proof

This public sample makes no vendor HTTP requests. Its optional, local response fixture models a provider payload changing independently of the adapter. It is disabled by default.

Build the SDK with `mvn clean install`, then build this separate project with `mvn -f examples/example-ticketing/pom.xml clean verify`. Install its JAR in the host adapter directory. Set JVM property `-Dgivops.example.payload=/mounted/response.json`; mount that synthetic JSON file read-only. Configure an `EXAMPLE_TICKETS` instance and its generic `apiToken` credential through the normal host APIs.

1. Supply `{"name":"Original synthetic project"}`. A normal read uses that name; explicit capability inspection returns it in `reason` with state `UNKNOWN`.
2. Change only the external fixture to `{"displayName":"Changed synthetic project"}`. The original JAR rejects the payload with `INVALID_RESPONSE`.
3. Build the replacement with `mvn -f examples/example-ticketing/pom.xml -Dexample.payload-field=displayName clean verify`. Replace only this adapter JAR and restart the unchanged host. The same changed payload now maps successfully.

The mapping field is packaged in the adapter JAR, not a host setting. Preserve the two JAR hashes and the unchanged host JAR/image hash when recording the proof. The fixture is bounded to 4096 bytes and its mapped name to 255 characters. This demonstrates synthetic payload compatibility repair; it does not claim live Jira or YouTrack verification.
