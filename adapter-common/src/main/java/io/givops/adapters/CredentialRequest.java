package io.givops.adapters;
import java.net.URI;
import java.util.UUID;
public record CredentialRequest(UUID instanceId, String instanceKey, String provider, URI baseUrl) { }
