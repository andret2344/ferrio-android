package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

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
import eu.andret.kalendarzswiatnietypowych.activity.LanguageActivity;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.Language;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.Util;
import lombok.Value;

public class LanguageAdapter extends ArrayAdapter<Language> {
	private final Util util;

	private static class ViewHolder {
		CheckedTextView view;
	}

	public LanguageAdapter(final Context context, final List<Language> locale) {
		super(context, R.layout.adapter_language, locale);
		util = new Util(getContext());
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
		holder.view.setText(language.getName());
		final ExecutorService executorService = Executors.newFixedThreadPool(16);
		holder.view.setOnClickListener(v -> {
			if (util.isConnection()) {
				try {
					final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setCancelable(false);
					builder.setView(R.layout.layout_loading_dialog);
					final AlertDialog dialog = builder.create();
					dialog.show();
					final Future<List<HolidayDay>> future = executorService.submit(new Downloader(language));
					final List<HolidayDay> data = future.get();
					dialog.dismiss();
					((LanguageActivity) getContext()).getListView().setItemChecked(position, true);
					final HolidaysDBHelper instance = HolidaysDBHelper.getInstance(getContext());
					if (!instance.languageExists(language.getCode())) {
						instance.insertLanguage(language);
					}
					instance.update(data, language);
					HolidayCalendar.getInstance(getContext()).refresh();
				} catch (final ExecutionException | InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			} else {
				util.createAlert(R.string.caution, R.string.no_internet);
			}
		});
		return convertView;
	}

	@Value
	public class Downloader implements Callable<List<HolidayDay>> {
		Language language;

		@Override
		public List<HolidayDay> call() throws Exception {
			final HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.unusualcalendar.net/holiday/" + language.getCode()).openConnection();
			final InputStream in = con.getInputStream();
			final int length = con.getHeaderFieldInt("Content-Length", -1);

			final byte[] bytes = new byte[length];
			for (int i = 0; i < length; i++) {
				if (!util.isConnection()) {
					util.createAlert(R.string.caution, R.string.no_internet);
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
				final HolidayDay curr = HolidayCalendar.getInstance(getContext()).getMonth(month).new HolidayDay(day, holidays);
				final JSONArray objectData = object.getJSONArray("holidays");
				for (int k = 0; k < objectData.length(); k++) {
					final JSONObject currObj = objectData.getJSONObject(k);
					curr.new Holiday(currObj.getInt("id"), currObj.getString("name"), currObj.getBoolean("usual"), currObj.getString("link"));
				}
				data.add(curr);
			}
			return data;
		}
	}
}
