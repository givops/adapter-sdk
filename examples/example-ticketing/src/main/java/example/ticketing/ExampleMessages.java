package example.ticketing;
import io.givops.adapters.i18n.MessageCatalog;
import java.util.Map;
final class ExampleMessages {
    private static final MessageCatalog CATALOG = MessageCatalog.load(ExampleMessages.class, "/example/ticketing/en.yml");
    private ExampleMessages() { }
    static String format(String key) { return CATALOG.format(key, Map.of()); }
}
