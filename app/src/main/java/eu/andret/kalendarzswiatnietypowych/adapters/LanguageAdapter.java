package eu.andret.kalendarzswiatnietypowych.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.Language;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.Util;
import lombok.Value;

public class LanguageAdapter extends ArrayAdapter<Language> {
	private static class ViewHolder {
		CheckedTextView view;
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
			holder.view = convertView.findViewById(R.id.adapter_language_checked_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Language language = getItem(position);

		final SharedPreferences prefs = Data.getPreferences(getContext(), Data.Prefs.LANGUAGE);
		if (prefs.getString(MainActivity.SELECTED_LANGUAGE, "").equals(language.getCode())) {
			((ListView) parent).setItemChecked(position, true);
		}

		holder.view.setText(language.getName());
		final ExecutorService executorService = Executors.newFixedThreadPool(16);
		holder.view.setOnClickListener(v -> {
			if (Util.isConnection(getContext())) {
				try {
					final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setCancelable(false);
					builder.setView(R.layout.layout_loading_dialog);
					final AlertDialog dialog = builder.create();
					dialog.show();
					final Future<List<HolidayDay>> future = executorService.submit(new Downloader(language));
					final List<HolidayDay> data = future.get();
					dialog.dismiss();
					final SharedPreferences.Editor editor = prefs.edit();
					editor.putString(MainActivity.SELECTED_LANGUAGE, language.getCode());
					editor.apply();
					((ListView) parent).setItemChecked(position, true);
					final HolidaysDBHelper holidaysDBHelper = new HolidaysDBHelper(getContext());
					if (!holidaysDBHelper.languageExists(language.getCode())) {
						holidaysDBHelper.insertLanguage(language);
					}
					holidaysDBHelper.update(data, language);
					holidaysDBHelper.close();
				} catch (final ExecutionException | InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			} else {
				final SharedPreferences.Editor editor = prefs.edit();
				editor.putString(MainActivity.SELECTED_LANGUAGE, language.getCode());
				editor.apply();
				((ListView) parent).setItemChecked(position, true);
			}
		});
		return convertView;
	}

	@Value
	public class Downloader implements Callable<List<HolidayDay>> {
		@NonNull
		Language language;

		@NonNull
		@Override
		public List<HolidayDay> call() throws Exception {
			final HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.unusualcalendar.net/holiday/" + language.getCode()).openConnection();
			final InputStream in = con.getInputStream();
			final int length = con.getHeaderFieldInt("Content-Length", -1);

			final byte[] bytes = new byte[length];
			for (int i = 0; i < length; i++) {
				if (!Util.isConnection(getContext())) {
					Util.createAlert(getContext(), R.string.caution, R.string.no_internet);
					return Collections.emptyList();
				}
				bytes[i] = (byte) in.read();
			}
			in.close();
			final String json = new String(bytes, StandardCharsets.UTF_8);
			final JSONArray jsonArray = new JSONArray(json);
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
				final HolidayDay curr = new HolidayDay(month, day, holidays);
				data.add(curr);
			}
			return data;
		}
	}
}
