package eu.andret.kalendarzswiatnietypowych.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApiClient {
	private static final String TAG = "Ferrio-ApiClient";
	private static final HttpUrl BASE_URL = HttpUrl.get("https://api.ferrio.app/v3/");
	private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

	public static final String REPORT_TYPE_ERROR = "error";
	public static final String REPORT_TYPE_SUGGESTION = "suggestion";
	public static final String HOLIDAY_TYPE_FIXED = "fixed";
	public static final String HOLIDAY_TYPE_FLOATING = "floating";

	private final OkHttpClient httpClient = new OkHttpClient.Builder()
			.connectTimeout(15, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(30, TimeUnit.SECONDS)
			.retryOnConnectionFailure(true)
			.build();

	/**
	 * Unauthenticated GET returning a typed list. Throws on any failure; callers that need a
	 * fire-and-forget empty-list behavior must catch {@link ApiException} themselves.
	 */
	@NonNull
	public <T> List<T> getList(@NonNull final HttpUrl url, @NonNull final Class<T> clazz)
			throws ApiException {
		return executeGet(url, null, clazz, null);
	}

	@NonNull
	public <T> List<T> getList(@NonNull final HttpUrl url, @NonNull final String authToken,
			@NonNull final Class<T> clazz, @Nullable final CancellableRequest cancel)
			throws ApiException {
		return executeGet(url, authToken, clazz, cancel);
	}

	public void post(@NonNull final HttpUrl url, @NonNull final String authToken,
			@NonNull final String jsonBody) throws ApiException {
		post(url, authToken, jsonBody, null);
	}

	public void post(@NonNull final HttpUrl url, @NonNull final String authToken,
			@NonNull final String jsonBody, @Nullable final CancellableRequest cancel)
			throws ApiException {
		final Request request = new Request.Builder()
				.url(url)
				.header("Authorization", "Bearer " + authToken)
				.post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
				.build();
		try (final Response response = execute(request, cancel, "POST " + url.encodedPath())) {
			ensureSuccessful(response);
		}
	}

	@NonNull
	private <T> List<T> executeGet(@NonNull final HttpUrl url, @Nullable final String authToken,
			@NonNull final Class<T> clazz, @Nullable final CancellableRequest cancel)
			throws ApiException {
		final Request.Builder builder = new Request.Builder().url(url).get();
		if (authToken != null) {
			builder.header("Authorization", "Bearer " + authToken);
		}
		try (final Response response = execute(builder.build(), cancel, "GET " + url.encodedPath())) {
			ensureSuccessful(response);
			final ResponseBody body = response.body();
			if (body == null) {
				return Collections.emptyList();
			}
			try (final Reader reader = new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8)) {
				final Type type = TypeToken.getParameterized(List.class, clazz).getType();
				final List<T> result = Util.GSON.fromJson(reader, type);
				return result != null ? result : Collections.emptyList();
			} catch (final IOException ex) {
				throw new NetworkException(ex.getMessage());
			}
		}
	}

	@NonNull
	private Response execute(@NonNull final Request request,
			@Nullable final CancellableRequest cancel, @NonNull final String description)
			throws ApiException {
		final Call call = httpClient.newCall(request);
		if (cancel != null) {
			cancel.attach(call);
		}
		try {
			return call.execute();
		} catch (final IOException ex) {
			if (call.isCanceled()) {
				throw new ApiException(-1, "cancelled");
			}
			Log.e(TAG, description + " failed", ex);
			throw new NetworkException(ex.getMessage());
		}
	}

	private void ensureSuccessful(@NonNull final Response response) throws ApiException {
		if (!response.isSuccessful()) {
			throw new ApiException(response.code(), readBody(response));
		}
	}

	@Nullable
	private String readBody(@NonNull final Response response) {
		final ResponseBody body = response.body();
		if (body == null) {
			return null;
		}
		try {
			return body.string();
		} catch (final IOException ex) {
			Log.w(TAG, "Failed to read response body", ex);
			return null;
		}
	}

	@NonNull
	public HttpUrl buildHolidaysUrl() {
		return BASE_URL.newBuilder()
				.addPathSegment("holidays")
				.addQueryParameter("lang", Util.getLanguageCode())
				.build();
	}

	@NonNull
	public HttpUrl buildHolidaysUrl(final int month, final int day) {
		return BASE_URL.newBuilder()
				.addPathSegment("holidays")
				.addQueryParameter("lang", Util.getLanguageCode())
				.addQueryParameter("month", Integer.toString(month))
				.addQueryParameter("day", Integer.toString(day))
				.build();
	}

	@NonNull
	public HttpUrl buildReportsUrl(@NonNull final String reportType,
			@NonNull final String holidayType) {
		return BASE_URL.newBuilder()
				.addPathSegments("users/reports")
				.addQueryParameter("reportType", reportType)
				.addQueryParameter("holidayType", holidayType)
				.build();
	}
}
