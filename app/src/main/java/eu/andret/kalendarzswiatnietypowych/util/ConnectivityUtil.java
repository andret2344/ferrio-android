package eu.andret.kalendarzswiatnietypowych.util;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public final class ConnectivityUtil {
	private ConnectivityUtil() {
	}

	public static boolean send(@NonNull final String path, @NonNull final JSONObject jsonObject) {
		try {
			final URL url = new URL(String.format("https://api.unusualcalendar.net/v2/%s", path));
			final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			final OutputStream outputStream = connection.getOutputStream();
			outputStream.write(jsonObject.toString().getBytes());
			final int responseCode = connection.getResponseCode();
			connection.disconnect();
			return responseCode < 400;
		} catch (final IOException ex) {
			return false;
		}
	}
}
