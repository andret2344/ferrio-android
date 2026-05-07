package eu.andret.kalendarzswiatnietypowych.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ApiExceptionTest {

	@Test
	public void statusCode_isStored() {
		assertThat(new ApiException(500, null).getStatusCode()).isEqualTo(500);
	}

	@Test
	public void isBanned_trueOn403() {
		assertThat(new ApiException(403, null).isBanned()).isTrue();
		assertThat(new ApiException(401, null).isBanned()).isFalse();
		assertThat(new ApiException(404, null).isBanned()).isFalse();
	}

	@Test
	public void isUnauthorized_trueOn401() {
		assertThat(new ApiException(401, null).isUnauthorized()).isTrue();
		assertThat(new ApiException(403, null).isUnauthorized()).isFalse();
	}

	@Test
	public void getBanReason_nullBody_returnsEmptyString() {
		assertThat(new ApiException(403, null).getBanReason()).isEmpty();
	}

	@Test
	public void getBanReason_validReasonField_returnsReason() {
		assertThat(new ApiException(403, "{\"reason\":\"abuse\"}").getBanReason()).isEqualTo("abuse");
	}

	@Test
	public void getBanReason_jsonWithoutReasonField_returnsEmptyString() {
		assertThat(new ApiException(403, "{\"other\":\"x\"}").getBanReason()).isEmpty();
	}

	@Test
	public void getBanReason_malformedJson_returnsEmptyString() {
		assertThat(new ApiException(403, "not json").getBanReason()).isEmpty();
	}

	@Test
	public void getBanReason_emptyJsonObject_returnsEmptyString() {
		assertThat(new ApiException(403, "{}").getBanReason()).isEmpty();
	}

	@Test
	public void messageContainsStatusCode() {
		assertThat(new ApiException(404, null)).hasMessage("HTTP 404");
	}
}
