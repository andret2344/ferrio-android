package eu.andret.kalendarzswiatnietypowych.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class ApiClient {
	private static final String TAG = "ApiClient";
	private static final String BASE_URL = "https://api.ferrio.app/v3";
	private static final int CONNECT_TIMEOUT_MS = 15_000;
	private static final int READ_TIMEOUT_MS = 30_000;

	public static final String REPORT_TYPE_ERROR = "error";
	public static final String REPORT_TYPE_SUGGESTION = "suggestion";
	public static final String HOLIDAY_TYPE_FIXED = "fixed";
	public static final String HOLIDAY_TYPE_FLOATING = "floating";

	@NonNull
	public <T> List<T> getList(@NonNull final String path, @NonNull final Class<T> clazz) {
		try {
			return executeGet(path, null, clazz);
		} catch (final ApiException ex) {
			Log.e(TAG, "GET request failed: " + path, ex);
			return Collections.emptyList();
		}
	}

	@NonNull
	public <T> List<T> getList(@NonNull final String path, @NonNull final String authToken,
			@NonNull final Class<T> clazz) throws ApiException {
		return executeGet(path, authToken, clazz);
	}

	public void post(@NonNull final String path, @NonNull final String authToken,
			@NonNull final String jsonBody) throws ApiException {
		HttpsURLConnection connection = null;
		try {
			final URL url = new URL(String.format("%s/%s", BASE_URL, path));
			connection = (HttpsURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
			connection.setReadTimeout(READ_TIMEOUT_MS);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + authToken);
			connection.setDoOutput(true);
			try (final OutputStream outputStream = connection.getOutputStream()) {
				outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
			}
			final int code = connection.getResponseCode();
			if (code < 200 || code >= 300) {
				throw new ApiException(code, readErrorStream(connection));
			}
		} catch (final IOException ex) {
			Log.e(TAG, "POST request failed: " + path, ex);
			throw new ApiException(0, ex.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	@NonNull
	private <T> List<T> executeGet(@NonNull final String path, @Nullable final String authToken,
			@NonNull final Class<T> clazz) throws ApiException {
		HttpsURLConnection connection = null;
		try {
			final URL url = new URL(String.format("%s/%s", BASE_URL, path));
			connection = (HttpsURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
			connection.setReadTimeout(READ_TIMEOUT_MS);
			if (authToken != null) {
				connection.setRequestProperty("Authorization", "Bearer " + authToken);
			}
			final int code = connection.getResponseCode();
			if (code < 200 || code >= 300) {
				throw new ApiException(code, readErrorStream(connection));
			}
			try (final InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
				final Type type = TypeToken.getParameterized(List.class, clazz).getType();
				final List<T> result = Util.GSON.fromJson(reader, type);
				return result != null ? result : Collections.emptyList();
			}
		} catch (final IOException ex) {
			Log.e(TAG, "GET request failed: " + path, ex);
			throw new ApiException(0, ex.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	@Nullable
	private String readErrorStream(@NonNull final HttpsURLConnection connection) {
		try (final InputStreamReader reader = new InputStreamReader(connection.getErrorStream())) {
			final StringBuilder sb = new StringBuilder();
			final char[] buffer = new char[1024];
			int read;
			while ((read = reader.read(buffer)) != -1) {
				sb.append(buffer, 0, read);
			}
			return sb.toString();
		} catch (final Exception ex) {
			Log.w(TAG, "Failed to read error stream", ex);
			return null;
		}
	}

	@NonNull
	public String buildHolidaysPath() {
		return String.format(Locale.ROOT, "holidays?lang=%s", Util.getLanguageCode());
	}

	@NonNull
	public String buildHolidaysPath(final int month, final int day) {
		return String.format(Locale.ROOT, "holidays?lang=%s&month=%d&day=%d",
				Util.getLanguageCode(), month, day);
	}

	@NonNull
	public String buildReportsPath(@NonNull final String reportType,
			@NonNull final String holidayType) {
		return String.format(Locale.ROOT, "users/reports?reportType=%s&holidayType=%s",
				reportType, holidayType);
	}
}
