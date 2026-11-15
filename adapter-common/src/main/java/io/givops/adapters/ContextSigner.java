package io.givops.adapters;
import java.util.List;
/** Signs length-prefixed UTF-8 parts. The host retains the key. */
public interface ContextSigner {
    String sign(String provider, String namespace, List<String> parts);
}
