package io.givops.catalog;

/** Open forge provider identity; available implementations are discovered at startup. */
public final class ForgeProvider implements Comparable<ForgeProvider> {
        public static final ForgeProvider GITLAB = new ForgeProvider("GITLAB");
        public static final ForgeProvider GITHUB = new ForgeProvider("GITHUB");
        public static final ForgeProvider AZURE_DEVOPS = new ForgeProvider("AZURE_DEVOPS");
        private final String name;
        private ForgeProvider(String name) { this.name = name; }
        @com.fasterxml.jackson.annotation.JsonCreator
        public static ForgeProvider valueOf(String name) {
            if (name == null || !name.matches("[A-Z][A-Z0-9_]{0,63}")) throw new IllegalArgumentException(io.givops.adapters.i18n.SdkMessages.format("sdk.provider.identity.invalid"));
            return switch (name) {
                case "GITLAB" -> GITLAB;
                case "GITHUB" -> GITHUB;
                case "AZURE_DEVOPS" -> AZURE_DEVOPS;
                default -> new ForgeProvider(name);
            };
        }
        @com.fasterxml.jackson.annotation.JsonValue public String name() { return name; }
        @Override public String toString() { return name; }
        @Override public int compareTo(ForgeProvider other) { return name.compareTo(other.name); }
        @Override public boolean equals(Object other) { return other instanceof ForgeProvider provider && name.equals(provider.name); }
        @Override public int hashCode() { return name.hashCode(); }
    }

