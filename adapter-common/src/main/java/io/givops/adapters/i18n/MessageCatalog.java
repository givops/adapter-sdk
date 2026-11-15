package io.givops.adapters.i18n;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.text.MessagePattern;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

/** Strict UTF-8 YAML catalog with immutable, validated ICU MessageFormat 1 patterns. */
public final class MessageCatalog {
    private record CompiledMessage(MessageFormat prototype, Set<String> arguments) {}
    private final Map<String, CompiledMessage> formats;
    private final Map<String, String> patterns;

    private MessageCatalog(Map<String, String> patterns) {
        this.patterns = Map.copyOf(patterns);
        var compiled = new LinkedHashMap<String, CompiledMessage>();
        patterns.forEach((key, pattern) -> {
            var parsed = new MessagePattern(MessagePattern.ApostropheMode.DOUBLE_OPTIONAL).parse(pattern);
            var arguments = new HashSet<String>();
            for (int index = 0; index < parsed.countParts(); index++) {
                var part = parsed.getPart(index);
                if (part.getType() == MessagePattern.Part.Type.ARG_NUMBER) {
                    throw new IllegalArgumentException("Message arguments must be named: " + key);
                }
                if (part.getType() == MessagePattern.Part.Type.ARG_NAME) arguments.add(parsed.getSubstring(part));
            }
            var format = new MessageFormat("", Locale.ENGLISH);
            format.applyPattern(pattern, MessagePattern.ApostropheMode.DOUBLE_OPTIONAL);
            compiled.put(key, new CompiledMessage(format, Set.copyOf(arguments)));
        });
        formats = Map.copyOf(compiled);
    }

    /** Loads only the named artifact-owned resource; never scans the host classpath for catalogs. */
    public static MessageCatalog load(Class<?> resourceOwner, String absoluteResourcePath) {
        if (absoluteResourcePath == null || !absoluteResourcePath.startsWith("/")
                || absoluteResourcePath.contains("..")) {
            throw new IllegalArgumentException("An absolute catalog resource path is required.");
        }
        var resources = new LinkedHashMap<String, InputStream>();
        resources.put(absoluteResourcePath, resourceOwner.getResourceAsStream(absoluteResourcePath));
        return load(resources);
    }

    public static MessageCatalog load(Map<String, InputStream> resources) {
        var patterns = new LinkedHashMap<String, String>();
        var options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(0);
        options.setNestingDepthLimit(16);
        options.setCodePointLimit(2_000_000);
        var yaml = new Yaml(new SafeConstructor(options));
        resources.forEach((resource, stream) -> {
            if (stream == null) throw new IllegalArgumentException("Missing message catalog: " + resource);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                Node root = yaml.compose(reader);
                if (root == null) throw new IllegalArgumentException("Empty message catalog: " + resource);
                flatten(root, "", patterns);
            } catch (java.io.IOException exception) {
                throw new IllegalArgumentException("Cannot read message catalog: " + resource, exception);
            }
        });
        return new MessageCatalog(patterns);
    }

    private static void flatten(Node node, String prefix, Map<String, String> patterns) {
        if (!(node instanceof MappingNode mapping) || !Tag.MAP.equals(node.getTag()) || node.getAnchor() != null) {
            throw new IllegalArgumentException("Message catalogs require plain mappings without anchors.");
        }
        var keys = new HashSet<String>();
        for (var tuple : mapping.getValue()) {
            if (!(tuple.getKeyNode() instanceof ScalarNode key) || !Tag.STR.equals(key.getTag())
                    || !key.getValue().matches("[a-zA-Z][a-zA-Z0-9_-]*(\\.[a-zA-Z][a-zA-Z0-9_-]*)*")
                    || !keys.add(key.getValue())) {
                throw new IllegalArgumentException("Message keys must be unique string identifiers.");
            }
            String name = prefix + key.getValue();
            Node value = tuple.getValueNode();
            if (value.getAnchor() != null) throw new IllegalArgumentException("Message aliases and anchors are forbidden.");
            if (value instanceof MappingNode) {
                flatten(value, name + ".", patterns);
            } else if (value instanceof ScalarNode scalar && Tag.STR.equals(value.getTag())) {
                if (patterns.putIfAbsent(name, scalar.getValue()) != null) {
                    throw new IllegalArgumentException("Duplicate message key: " + name);
                }
            } else {
                throw new IllegalArgumentException("Message values must be strings: " + name);
            }
        }
    }

    public String format(String key, Map<String, ?> parameters) {
        CompiledMessage message = formats.get(key);
        if (message == null) throw new IllegalArgumentException("Unknown message key: " + key);
        if (!parameters.keySet().containsAll(message.arguments())) {
            throw new IllegalArgumentException("Missing message arguments: " + key);
        }
        return ((MessageFormat) message.prototype().clone()).format(parameters);
    }

    public Map<String, String> patterns() { return patterns; }
}
