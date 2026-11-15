package io.givops.adapters;
public record CredentialField(String name, String label, Type type, boolean required, int maxLength) {
    public enum Type { TEXT, SECRET }
    public CredentialField {
        if (name == null || !name.matches("[a-zA-Z][a-zA-Z0-9]{0,63}") || label == null || label.isBlank()
                || type == null || maxLength < 1 || maxLength > 4096) throw new IllegalArgumentException(io.givops.adapters.i18n.SdkMessages.format("sdk.credential.field.invalid"));
    }
}
