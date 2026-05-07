package eu.andret.kalendarzswiatnietypowych.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.databinding.ActivityWidgetConfigBinding;

public class WidgetConfigActivity extends AppCompatActivity {
	private static final int FONT_SIZE_OFFSET_RANGE = 8;

	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private ActivityWidgetConfigBinding binding;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		binding = ActivityWidgetConfigBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		final Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			appWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}

		binding.activityWidgetConfigPickerOffset.setMinValue(0);
		binding.activityWidgetConfigPickerOffset.setMaxValue(60);
		binding.activityWidgetConfigPickerOffset.setWrapSelectorWheel(false);
		binding.activityWidgetConfigPickerOffset.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

		final String[] displayValues = new String[61];
		for (int i = 0; i <= 60; i++) {
			displayValues[i] = String.valueOf(i - 30);
		}
		binding.activityWidgetConfigPickerOffset.setDisplayedValues(displayValues);

		final int fontSizeCount = FONT_SIZE_OFFSET_RANGE * 2 + 1;
		binding.activityWidgetConfigPickerFontSize.setMinValue(0);
		binding.activityWidgetConfigPickerFontSize.setMaxValue(fontSizeCount - 1);
		binding.activityWidgetConfigPickerFontSize.setWrapSelectorWheel(false);
		binding.activityWidgetConfigPickerFontSize.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

		final String[] fontSizeValues = new String[fontSizeCount];
		for (int i = 0; i < fontSizeCount; i++) {
			final int delta = i - FONT_SIZE_OFFSET_RANGE;
			if (delta == 0) {
				fontSizeValues[i] = getString(R.string.widget_config_font_size_system);
			} else if (delta > 0) {
				fontSizeValues[i] = "+" + delta;
			} else {
				fontSizeValues[i] = String.valueOf(delta);
			}
		}
		binding.activityWidgetConfigPickerFontSize.setDisplayedValues(fontSizeValues);

		// Load existing prefs for reconfiguration
		final int savedOffset = WidgetPrefs.getDaysOffset(this, appWidgetId);
		final boolean savedColorized = WidgetPrefs.isColorized(this, appWidgetId);
		final int savedFontSizeOffset = WidgetPrefs.getFontSizeOffset(this, appWidgetId);

		binding.activityWidgetConfigPickerOffset.setValue(savedOffset + 30);
		binding.activityWidgetConfigSwitchColorized.setChecked(savedColorized);
		final int clampedFontSizeOffset = Math.max(-FONT_SIZE_OFFSET_RANGE,
				Math.min(FONT_SIZE_OFFSET_RANGE, savedFontSizeOffset));
		binding.activityWidgetConfigPickerFontSize.setValue(clampedFontSizeOffset + FONT_SIZE_OFFSET_RANGE);

		binding.activityWidgetConfigButtonSave.setOnClickListener(v -> saveAndFinish());
	}

	private void saveAndFinish() {
		final int daysOffset = binding.activityWidgetConfigPickerOffset.getValue() - 30;
		final boolean colorized = binding.activityWidgetConfigSwitchColorized.isChecked();
		final int fontSizeOffset = binding.activityWidgetConfigPickerFontSize.getValue() - FONT_SIZE_OFFSET_RANGE;

		WidgetPrefs.save(this, appWidgetId, daysOffset, colorized, fontSizeOffset);

		final Context appContext = getApplicationContext();
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(appContext);
		final AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
		if (info != null) {
			BaseWidgetProvider.updateSingleWidget(appContext, appWidgetManager, appWidgetId, info.initialLayout);
		}

		final Intent resultIntent = new Intent();
		resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(RESULT_OK, resultIntent);
		finish();
	}
}
