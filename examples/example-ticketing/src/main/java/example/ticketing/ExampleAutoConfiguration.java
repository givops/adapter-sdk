package example.ticketing;
import io.givops.adapters.CapabilityInspection;
import io.givops.adapters.ContextSigner;
import io.givops.adapters.CredentialHost;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
public class ExampleAutoConfiguration {
    @Bean ExampleDefinition exampleDefinition() { return new ExampleDefinition(); }
    @Bean ExampleAdapter exampleAdapter(CredentialHost host, ContextSigner signer, Clock clock) {
        return new ExampleAdapter(host, signer, clock, null, CapabilityInspection.State.UNKNOWN);
    }
}
