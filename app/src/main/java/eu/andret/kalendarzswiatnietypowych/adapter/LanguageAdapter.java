package eu.andret.kalendarzswiatnietypowych.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.Language;
import eu.andret.kalendarzswiatnietypowych.util.Util;
import java9.util.concurrent.CompletableFuture;
import java9.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.Value;

public class LanguageAdapter extends ArrayAdapter<Language> {
	private static class ViewHolder {
		CheckedTextView checkedTextView;
	}

	public LanguageAdapter(final Context context, final List<Language> locale) {
		super(context, R.layout.adapter_language, locale);
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert inflater != null;
			convertView = inflater.inflate(R.layout.adapter_language, parent, false);
			holder = new ViewHolder();
			holder.checkedTextView = convertView.findViewById(R.id.adapter_language_checked_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Language language = getItem(position);

		final Dialog progressDialog = new Dialog(getContext());
		progressDialog.setContentView(R.layout.layout_loading_dialog);
		progressDialog.setTitle(getContext().getString(R.string.downloading_data));
		progressDialog.setCancelable(false);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		if (preferences.getString(MainActivity.SELECTED_LANGUAGE, "").equals(language.getCode())) {
			((ListView) parent).setItemChecked(position, true);
		}

		holder.checkedTextView.setText(language.getName());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		holder.checkedTextView.setOnClickListener(checkedTextView -> {
			((ListView) parent).setItemChecked(position, true);
			preferences.edit()
					.putString(MainActivity.SELECTED_LANGUAGE, language.getCode())
					.apply();
			final HolidaysDBHelper holidaysDBHelper = new HolidaysDBHelper(getContext());
			if (holidaysDBHelper.languageExists(language.getCode())) {
				return;
			}
			if (!Util.isConnection(getContext())) {
				return;
			}
			progressDialog.show();
			CompletableFuture.supplyAsync(new Downloader(language), executorService)
					.thenAccept(holidayDays -> {
						holidaysDBHelper.insertLanguage(language);
						holidaysDBHelper.update(holidayDays, language);
						holidaysDBHelper.close();
						progressDialog.dismiss();
					});
		});
		return convertView;
	}

	@Value
	public static class Downloader implements Supplier<List<HolidayDay>> {
		@NonNull
		Language language;

		@SneakyThrows
		@NonNull
		@Override
		public List<HolidayDay> get() {
			final HttpsURLConnection con = (HttpsURLConnection)
					new URL("https://api.unusualcalendar.net/holiday/" + language.getCode())
							.openConnection();
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			final StringBuilder result = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			final JSONArray jsonArray = new JSONArray(result.toString());
			final List<HolidayDay> data = new ArrayList<>();
			final int jsonLength = jsonArray.length();
			for (int j = 0; j < jsonLength; j++) {
				final JSONObject object = jsonArray.getJSONObject(j);
				final int day = object.getInt("day");
				final int month = object.getInt("month");
				final List<Holiday> holidays = new ArrayList<>();
				final JSONArray objectData = object.getJSONArray("holidays");
				for (int k = 0; k < objectData.length(); k++) {
					final JSONObject currObj = objectData.getJSONObject(k);
					holidays.add(new Holiday(
							currObj.getInt("id"),
							currObj.getString("name"),
							currObj.getBoolean("usual"),
							currObj.getString("link")));
				}
				data.add(new HolidayDay(month, day, holidays));
			}
			return data;
		}
	}
}
