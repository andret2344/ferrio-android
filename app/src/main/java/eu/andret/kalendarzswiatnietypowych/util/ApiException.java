package eu.andret.kalendarzswiatnietypowych.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class ApiException extends Exception {
	private final int statusCode;
	private final String responseBody;

	public ApiException(final int statusCode, @Nullable final String responseBody) {
		super("HTTP " + statusCode);
		this.statusCode = statusCode;
		this.responseBody = responseBody;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public boolean isBanned() {
		return statusCode == 403;
	}

	public boolean isUnauthorized() {
		return statusCode == 401;
	}

	@NonNull
	public String getBanReason() {
		if (responseBody == null) {
			return "";
		}
		try {
			final JsonObject json = Util.GSON.fromJson(responseBody, JsonObject.class);
			if (json != null && json.has("reason")) {
				return json.get("reason").getAsString();
			}
		} catch (final JsonSyntaxException ignored) {
		}
		return "";
	}
}
