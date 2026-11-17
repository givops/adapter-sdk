package io.givops.adapters;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CompatibilityTest {
    @Test void experimentalSdkApiRequiresExactLevel() {
        SdkCompatibility.requireCompatible("0.3");
        for (String incompatible : new String[]{"0", "0.1", "0.2", "1.0", "0.1.1", "17", "19", null})
            assertThatThrownBy(() -> SdkCompatibility.requireCompatible(incompatible)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void numericRangesDoNotCompareLexicallyOrInferFeatures() {
        var seventeen = ProviderVersion.parse("17").orElseThrow();
        var nineteen = ProviderVersion.parse("19").orElseThrow();
        var range = new ProviderVersionRange(seventeen, nineteen);
        assertThat(range.contains(ProviderVersion.parse("17.10.2").orElseThrow())).isTrue();
        assertThat(ProviderVersion.parse("17.10").orElseThrow()).isGreaterThan(ProviderVersion.parse("17.9").orElseThrow());
        assertThat(range.contains(nineteen)).isFalse();
        assertThat(ProviderVersion.parse("17.0.0")).contains(seventeen);
        assertThat(ProviderVersion.parse("unknown")).isEmpty();
        assertThat(ProviderVersion.parse(null)).isEmpty();
        assertThat(range.contains(null)).isFalse();
        assertThat(new CredentialAccessException()).hasNoCause().hasMessage("Credential access is unavailable.");
    }
}
