package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.BuildConfig;

/**
 * Collects non-sensitive device metadata attached to reports and suggestions so the backend can
 * triage them per platform/device/version. All values come from {@link Build}, {@link BuildConfig}
 * and the active locale, so no runtime permissions are required.
 */
public final class DeviceMetadata {
	private static final String PLATFORM_ANDROID = "android";

	private DeviceMetadata() {
	}

	/**
	 * Attaches a nested {@code device} object ({@code platform}, {@code model}, {@code os_version},
	 * {@code app_version} and, when resolvable, {@code country}) to the given request body. Keys
	 * match the API's {@code DeviceDTO} shape.
	 */
	public static void addTo(@NonNull final JsonObject body, @NonNull final Context context) {
		final JsonObject device = new JsonObject();
		device.addProperty("platform", PLATFORM_ANDROID);
		device.addProperty("model", getRealDevice());
		device.addProperty("os_version", Build.VERSION.RELEASE);
		device.addProperty("app_version", BuildConfig.VERSION_NAME);
		final String country = getDeviceCountry(context);
		if (country != null) {
			device.addProperty("country", country);
		}
		body.add("device", device);
	}

	@NonNull
	private static String getRealDevice() {
		final String manufacturer = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.trim();
		final String model = Build.MODEL == null ? "" : Build.MODEL.trim();
		if (model.isEmpty()) {
			return manufacturer;
		}
		final boolean modelRepeatsManufacturer = !manufacturer.isEmpty()
				&& model.toLowerCase(Locale.ROOT).startsWith(manufacturer.toLowerCase(Locale.ROOT));
		if (manufacturer.isEmpty() || modelRepeatsManufacturer) {
			return model;
		}
		return manufacturer + " " + model;
	}

	// API stores device_country as VARCHAR(2), so only ISO 3166-1 alpha-2 region codes qualify;
	// locales without a region (or with a numeric UN M.49 region) are dropped.
	@Nullable
	private static String getDeviceCountry(@NonNull final Context context) {
		final Locale locale = context.getResources().getConfiguration().getLocales().get(0);
		final String country = locale.getCountry();
		if (country.length() != 2) {
			return null;
		}
		return country.toUpperCase(Locale.ROOT);
	}
}
