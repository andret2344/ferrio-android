package eu.andret.kalendarzswiatnietypowych.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.LanguageActivity;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.LanguagePacket;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class LanguageAdapter extends ArrayAdapter<LanguagePacket> {
	private final List<LanguagePacket> locale;
	private static final Map<LanguagePacket, Downloader> threads = new HashMap<>();
	private final Util util;
	private ViewHolder holder;

	public static class ViewHolder {
		TextView name;
		TextView count;
		TextView date;
		ProgressBar progress;
		ImageView download;
		ImageView update;
		public RadioButton selected;
		LinearLayout main;
	}

	public LanguageAdapter(Context context, List<LanguagePacket> locale) {
		super(context, R.layout.adapter_language, locale);
		this.locale = locale;
		util = new Util(getContext());
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert inflater != null;
			convertView = inflater.inflate(R.layout.adapter_language, parent, false);
			holder = new ViewHolder();
			holder.name = convertView.findViewById(R.id.adapter_language_text_name);
			holder.count = convertView.findViewById(R.id.adapter_language_text_count);
			holder.progress = convertView.findViewById(R.id.adapter_language_progress_downloading);
			holder.download = convertView.findViewById(R.id.adapter_language_image_download);
			holder.selected = convertView.findViewById(R.id.adapter_language_radiobutton_selected);
			holder.main = convertView.findViewById(R.id.adapter_language_linear_main);
			holder.update = convertView.findViewById(R.id.adapter_language_image_update);
			holder.date = convertView.findViewById(R.id.adapter_language_text_date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String lang = locale.get(position).getLocale().getDisplayName();
		String l = lang.substring(0, 1).toUpperCase(locale.get(position).getLocale()) + lang.substring(1).toLowerCase(locale.get(position).getLocale());
		holder.name.setText(l);
		holder.count.setText(locale.get(position).getTranslated() + "/" + ((LanguageActivity) getContext()).getMax());
		holder.update.setVisibility(locale.get(position).isUpdate() ? View.VISIBLE : View.INVISIBLE);
		holder.download.setVisibility(View.VISIBLE);
		holder.selected.setVisibility(View.INVISIBLE);
		if (locale.get(position).isDownloaded()) {
			holder.progress.setVisibility(View.GONE);
			holder.date.setVisibility(View.VISIBLE);
			if (locale.get(position).getDate() != null) {
				holder.date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(locale.get(position).getDate()));
			}
			holder.download.setVisibility(View.INVISIBLE);
			holder.selected.setVisibility(View.VISIBLE);
			holder.main.setOnClickListener(v -> {
				for (int i = 0; i < ((ListView) parent).getCount(); i++) {
					View view = parent.getChildAt(i);
					RadioButton radio = view.findViewById(R.id.adapter_language_radiobutton_selected);
					radio.setChecked(false);
				}
				SharedPreferences prefs = Data.getPreferences(getContext(), Data.Prefs.LANGUAGE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("selected", locale.get(position).getId());
				editor.apply();
				// HolidayCalendar.getInstance(getContext()).refresh();
				holder.selected.setChecked(true);
			});
			holder.main.setOnLongClickListener(v -> {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				CharSequence[] colors = new CharSequence[]{getContext().getResources().getString(R.string.delete_language)};
				builder.setItems(colors, (dialog, which) -> {
					if (which == 0) {
						HolidaysDBHelper.getInstance(getContext()).remove(locale.get(position));
						holder.main.setOnLongClickListener(null);
						holder.main.setOnClickListener(null);
						holder.selected.setChecked(false);
						holder.download.setVisibility(View.VISIBLE);
						holder.selected.setVisibility(View.INVISIBLE);
						holder.progress.setVisibility(View.INVISIBLE);
						holder.date.setVisibility(View.GONE);
						SharedPreferences prefs = Data.getPreferences(getContext(), Data.Prefs.LANGUAGE);
						if (prefs.getInt("selected", -1) == locale.get(position).getId()) {
							SharedPreferences.Editor editor = prefs.edit();
							editor.putInt("selected", -1);
							editor.apply();
							HolidayCalendar.getInstance(getContext()).refresh();
						}
					}
				});
				builder.show();
				return false;
			});

			holder.update.setOnClickListener(v -> {
				if (util.isConnection()) {
					holder.update.setVisibility(View.INVISIBLE);
					Toast.makeText(getContext(), getContext().getResources().getString(R.string.updating) + ": " + l, Toast.LENGTH_SHORT).show();
					new Downloader(holder, locale.get(position), position, parent).execute();
				} else {
					util.createAlert(R.string.caution, R.string.no_internet);
				}
			});
		} else {
			holder.main.setOnClickListener(null);
		}
		holder.download.setOnClickListener(v -> {
			if (util.isConnection()) {
				Toast.makeText(getContext(), getContext().getResources().getString(R.string.downloading_language) + ": " + l, Toast.LENGTH_SHORT).show();
				Downloader d = new Downloader(holder, locale.get(position), position, parent);
				threads.put(locale.get(position), d);
				d.execute();
			} else {
				util.createAlert(R.string.caution, R.string.no_internet);
			}
		});

		SharedPreferences prefs = Data.getPreferences(getContext(), Data.Prefs.LANGUAGE);
		holder.selected.setChecked(prefs.getInt("selected", -1) == locale.get(position).getId());

		return convertView;
	}

	public ViewHolder getHolder() {
		return holder;
	}

	public static void cancelAllTasks() {
		for (Entry<LanguagePacket, Downloader> entry : threads.entrySet()) {
			entry.getValue().cancel(true);
		}
	}

	public class Downloader extends AsyncTask<String, Integer, String> {
		private final LanguagePacket locale;
		private final ViewHolder holder;
		private final Handler handler = new Handler();
		private final int position;
		private final View parent;

		Downloader(ViewHolder holder, LanguagePacket locale, int position, View parent) {
			this.holder = holder;
			this.locale = locale;
			this.position = position;
			this.parent = parent;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			holder.download.setVisibility(View.INVISIBLE);
			holder.progress.setVisibility(View.VISIBLE);
			holder.date.setVisibility(View.GONE);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			holder.progress.setIndeterminate(false);
			holder.progress.setProgress(0);
			Toast.makeText(getContext(), R.string.downloaded, Toast.LENGTH_SHORT).show();
			// HolidayCalendar.getInstance(getContext()).refresh();
			threads.remove(locale);
			holder.date.setVisibility(View.VISIBLE);
			locale.setDate(new Date());
			holder.date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(locale.getDate()));
		}

		@Override
		protected String doInBackground(String... params) {
			String json;
			try {
				// holder.progress.getIndeterminateDrawable().setColorFilter(Color.rgb(52, 178, 255),
				// PorterDuff.Mode.SRC_ATOP);
				HttpURLConnection con = (HttpURLConnection) new URL("https://andret.eu/uhc/api/get.php").openConnection();
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				PrintStream ps = new PrintStream(con.getOutputStream());
				ps.print("language=" + locale.getId());

				InputStream in = con.getInputStream();
				Map<String, List<String>> m = con.getHeaderFields();
				String tmp = null;

				if (m.containsKey("content-length")) {
					List<String> l = m.get("content-length");
					if (l.size() != 0) {
						String s = l.get(0);
						if (s != null && !s.isEmpty()) {
							tmp = s;
						}
					}
				}
				long length = tmp == null ? -1 : Long.parseLong(tmp);
				handler.post(() -> {
					if (length < 0) {
						holder.progress.setIndeterminate(true);
						holder.progress.setProgress(0);
					} else {
						holder.progress.setIndeterminate(false);
						holder.progress.setProgress(0);
					}
					holder.progress.setMax(100);
				});
				ps.close();

				byte[] bytes = new byte[(int) length];
				int last = 0;
				for (int i = 0; i < length; i++) {
					if (isCancelled() && !util.isConnection()) {
						handler.post(() -> {
							holder.download.setVisibility(View.VISIBLE);
							holder.progress.setVisibility(View.INVISIBLE);
						});
						if (!util.isConnection()) {
							util.createAlert(R.string.caution, R.string.no_internet);
						}
						return null;
					}
					bytes[i] = (byte) in.read();
					int curr = (int) (100 * i / length);
					if (curr != last) {
						try {
							Thread.sleep(10);
							publishProgress(curr);
						} catch (InterruptedException ex) {
							Log.getStackTraceString(ex);
							throw ex;
						}
						last = curr;
					}
				}
				in.close();
				json = new String(bytes, "UTF-8");
				handler.post(() -> {
					holder.progress.setIndeterminate(true);
					holder.progress.getIndeterminateDrawable().setColorFilter(Color.rgb(200, 20, 20), PorterDuff.Mode.SRC_ATOP);
				});
				JSONObject jsonObject = new JSONObject(json);
				boolean update = Boolean.parseBoolean(String.valueOf(jsonObject.get("result")));
				if (update) {
					JSONArray jsonArray = jsonObject.getJSONArray("holidays");
					List<HolidayDay> data = new ArrayList<>();
					int jsonLength = jsonArray.length();
					for (int j = 0; j < jsonLength; j++) {
						JSONObject object = jsonArray.getJSONObject(j);
						int day = object.getInt("day");
						int month = object.getInt("month");
						List<Holiday> holidays = new ArrayList<>();
						HolidayDay curr = HolidayCalendar.getInstance(getContext()).getMonth(month).new HolidayDay(day, holidays);
						JSONArray objectData = object.getJSONArray("holidays");
						for (int k = 0; k < objectData.length(); k++) {
							JSONObject currObj = objectData.getJSONObject(k);
							holidays.add(curr.new Holiday(currObj.getInt("id"), currObj.getString("text"), currObj.getString("usual").equals("1"), currObj.getString("external_link")));
						}
						data.add(curr);
					}
					HolidaysDBHelper.getInstance(getContext()).insertLanguage(locale);
					HolidaysDBHelper.getInstance(getContext()).update(data, locale);
					HolidayCalendar.getInstance(getContext()).refresh();
					handler.post(() -> {
						synchronized (holder) {
							holder.progress.setVisibility(View.GONE);
							holder.selected.setVisibility(View.VISIBLE);
						}
					});
				}
			} catch (Exception ex) {
				Log.getStackTraceString(ex);
			}
			holder.main.setOnLongClickListener(v -> {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				CharSequence[] colors = new CharSequence[]{getContext().getResources().getString(R.string.delete_language)};
				builder.setItems(colors, (dialog, which) -> {
					if (which == 0) {
						HolidaysDBHelper.getInstance(getContext()).remove(LanguageAdapter.this.locale.get(position));
						holder.main.setOnLongClickListener(null);
						holder.selected.setChecked(false);
						holder.download.setVisibility(View.VISIBLE);
						holder.selected.setVisibility(View.INVISIBLE);
						holder.progress.setVisibility(View.INVISIBLE);
						holder.date.setVisibility(View.GONE);
						SharedPreferences prefs = Data.getPreferences(getContext(), Data.Prefs.LANGUAGE);
						if (prefs.getInt("selected", -1) == LanguageAdapter.this.locale.get(position).getId()) {
							SharedPreferences.Editor editor = prefs.edit();
							editor.putInt("selected", -1);
							editor.apply();
						}
					}
				});
				builder.show();
				return false;
			});
			holder.main.setOnClickListener(v -> {
				for (int i = 0; i < ((ListView) parent).getCount(); i++) {
					View view = ((ListView) parent).getChildAt(i);
					RadioButton radio = view.findViewById(R.id.adapter_language_radiobutton_selected);
					radio.setChecked(false);
				}
				SharedPreferences prefs = Data.getPreferences(getContext(), Data.Prefs.LANGUAGE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("selected", LanguageAdapter.this.locale.get(position).getId());
				editor.apply();
				holder.selected.setChecked(true);
			});
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			holder.progress.setProgress(values[0]);
		}
	}
}
