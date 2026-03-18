package eu.andret.kalendarzswiatnietypowych.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import eu.andret.kalendarzswiatnietypowych.R;

public class WidgetConfigActivity extends AppCompatActivity {
	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private NumberPicker pickerOffset;
	private MaterialSwitch switchColorized;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		setContentView(R.layout.activity_widget_config);

		final Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			appWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}

		pickerOffset = findViewById(R.id.activity_widget_config_picker_offset);
		switchColorized = findViewById(R.id.activity_widget_config_switch_colorized);
		final MaterialButton buttonSave = findViewById(R.id.activity_widget_config_button_save);

		pickerOffset.setMinValue(0);
		pickerOffset.setMaxValue(60);
		pickerOffset.setWrapSelectorWheel(false);
		pickerOffset.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

		final String[] displayValues = new String[61];
		for (int i = 0; i <= 60; i++) {
			displayValues[i] = String.valueOf(i - 30);
		}
		pickerOffset.setDisplayedValues(displayValues);

		// Load existing prefs for reconfiguration
		final int savedOffset = WidgetPrefs.getDaysOffset(this, appWidgetId);
		final boolean savedColorized = WidgetPrefs.isColorized(this, appWidgetId);

		pickerOffset.setValue(savedOffset + 30);
		switchColorized.setChecked(savedColorized);

		buttonSave.setOnClickListener(v -> saveAndFinish());
	}

	private void saveAndFinish() {
		final int daysOffset = pickerOffset.getValue() - 30;
		final boolean colorized = switchColorized.isChecked();

		WidgetPrefs.save(this, appWidgetId, daysOffset, colorized);

		final Context appContext = getApplicationContext();
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(appContext);
		final int layoutResId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		BaseWidgetProvider.updateSingleWidget(appContext, appWidgetManager, appWidgetId, layoutResId);

		final Intent resultIntent = new Intent();
		resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(RESULT_OK, resultIntent);
		finish();
	}
}
