package io.givops.forge.command;

import io.givops.catalog.ForgeProvider;
import io.givops.forge.port.ForgeAdapter.Access;
import java.net.URI;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/** Mutating CI operations are separate from the forge observation adapters. */
public interface PipelineCommandProvider {
    record InputDefinition(String name, String type, String description, boolean required,
                           JsonNode defaultValue, List<JsonNode> options, String regex) {
        public InputDefinition { options = List.copyOf(options); }
    }
    record Repository(ForgeProvider provider, URI baseUrl, String externalId, String path) {}
    record Result(String actualRef, String actualSha, String pipelineId, String webUrl, String errorCode,
                  boolean possiblyAccepted) {
        public static Result accepted(String actualRef, String actualSha, String pipelineId, String webUrl) {
            return new Result(actualRef, actualSha, pipelineId, webUrl, null, false);
        }

        public static Result failure(String code, boolean possiblyAccepted) {
            return new Result(null, null, null, null, code, possiblyAccepted);
        }
    }

    boolean supports(Repository repository);

    List<InputDefinition> definitions(Repository repository, Access credential, String fullRef);

    Result create(Repository repository, Access credential, String fullRef, Map<String, JsonNode> inputs);
}
