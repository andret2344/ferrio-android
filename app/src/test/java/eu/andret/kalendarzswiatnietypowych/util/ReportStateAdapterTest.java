package eu.andret.kalendarzswiatnietypowych.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import eu.andret.kalendarzswiatnietypowych.entity.ReportState;

public class ReportStateAdapterTest {

	@Test
	public void roundTrip_eachKnownState_preservesValue() {
		for (final ReportState state : ReportState.values()) {
			final String json = Util.GSON.toJson(state);
			assertThat(Util.GSON.fromJson(json, ReportState.class))
					.as("round trip for %s", state)
					.isEqualTo(state);
		}
	}

	@Test
	public void serialize_writesEnumName() {
		assertThat(Util.GSON.toJson(ReportState.APPLIED)).isEqualTo("\"APPLIED\"");
	}

	@Test
	public void deserialize_unknownName_fallsBackToUnknown() {
		assertThat(Util.GSON.fromJson("\"NOT_A_STATE\"", ReportState.class))
				.isEqualTo(ReportState.UNKNOWN);
	}

	@Test
	public void deserialize_jsonNull_fallsBackToUnknown() {
		assertThat(Util.GSON.fromJson("null", ReportState.class)).isEqualTo(ReportState.UNKNOWN);
	}

	@Test
	public void deserialize_caseSensitive_unknownCasingFallsBackToUnknown() {
		// ReportState.valueOf is case-sensitive — "applied" must not match APPLIED.
		assertThat(Util.GSON.fromJson("\"applied\"", ReportState.class))
				.isEqualTo(ReportState.UNKNOWN);
	}
}
