package eu.andret.kalendarzswiatnietypowych.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.LanguageAdapter;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.LanguagePacket;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class LanguageActivity extends AppCompatActivity {
	private final List<LanguagePacket> languages = new ArrayList<>();
	private ListView listView;
	private ProgressDialog progressDialog;
	private int max;
    private final Downloader downloader = new LanguageActivity.Downloader(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Util util = new Util(this);
		util.applyTheme();
		setContentView(R.layout.activity_language);
		progressDialog = new ProgressDialog(this);
		util.createAd(R.id.language_adview_bottom);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		listView = findViewById(R.id.language_list_languages);
		listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		List<LanguagePacket> existing = HolidaysDBHelper.getInstance(this).getExistingLanguages();
		if (util.isConnection()) {
			long t = HolidaysDBHelper.getInstance(this).getLastUpdate();
			if (t == -1) {
				downloader.execute();
			} else {
				downloader.execute(t);
			}
		} else {
			for (LanguagePacket lp : existing) {
				lp.setDate(new Date(HolidaysDBHelper.getInstance(LanguageActivity.this).getLastUpdateDate(lp).getTime() * 1000));
			}
			listView.setAdapter(new LanguageAdapter(this, existing));
		}
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				return true;
			}
		});
		listView.setItemsCanFocus(false);
		int id = getIntent().getIntExtra("lang", -1);
		int pos = getIntent().getIntExtra("pos", -1);
		if (id != -1) {
			LanguageAdapter a = (LanguageAdapter) listView.getAdapter();
//			for (LanguagePacket lp : existing) {
//
//			}
			// a.new Downloader(a.getHolder(), id, pos, listView);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			boolean found = false;
			for (int i = 0; i < listView.getChildCount(); i++) {
				LanguageAdapter.ViewHolder holder = (LanguageAdapter.ViewHolder) listView.getChildAt(i).getTag();
				if (holder.selected.isChecked() && holder.selected.getVisibility() == View.VISIBLE) {
					found = true;
					break;
				}
			}
			if (found) {
				HolidayCalendar.getInstance(this).refresh();
				NavUtils.navigateUpFromSameTask(this);
			} else {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(R.string.caution);
				alert.setMessage(R.string.nothing_chosen);
				alert.setPositiveButton(R.string.no, null);
				alert.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						HolidayCalendar.getInstance(LanguageActivity.this).refresh();
						NavUtils.navigateUpFromSameTask(LanguageActivity.this);
					}
				});
				alert.show();
			}
		}
	}
	
	public int getMax() {
		return max;
	}

	@Override
	protected void onPause() {
		LanguageAdapter.cancelAllTasks();
		progressDialog.cancel();
		downloader.cancel(true);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		LanguageAdapter.cancelAllTasks();
		progressDialog.cancel();
		downloader.cancel(true);
		super.onDestroy();
	}
	
	private static class Downloader extends AsyncTask<Long, Void, String> {
		private final ThreadLocal<LanguageActivity> activity = new ThreadLocal<>();

		Downloader(LanguageActivity activity) {
			this.activity.set(activity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activity.get().progressDialog.setMessage(activity.get().getResources().getString(R.string.downloading_data));
			activity.get().progressDialog.setCancelable(false);
			activity.get().progressDialog.show();
		}
		
		@Override
		protected String doInBackground(Long... params) {
			try {
				HttpURLConnection con = (HttpURLConnection) new URL("https://andret.eu/uhc/api/lang.php").openConnection();
				con.setDoOutput(true);
				PrintStream ps = new PrintStream(con.getOutputStream());
				if (params.length == 1) {
					ps.print("date=" + params[0]);
				}
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String result = reader.readLine();
				reader.close();
				ps.close();
				if (result == null || result.equals("")) {
					return "{\"result\":false}";
				}
				return result;
			} catch (IOException ex) {
				ex.printStackTrace();
				return "{\"result\":false}";
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				JSONObject jsonObject = new JSONObject(result);
				boolean update = Boolean.parseBoolean(String.valueOf(jsonObject.get("result")));
				if (update) {
					activity.get().max = Integer.parseInt(jsonObject.getString("max"));
					List<Integer> existing = HolidaysDBHelper.getInstance(activity.get()).getExistingLanguagesIds();
					JSONArray jsonArray = jsonObject.getJSONArray("languages");
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject currObj = jsonArray.getJSONObject(i);
						Locale loc = new Locale(currObj.getString("name"));
						LanguagePacket lp;
						if (existing.contains(currObj.getInt("id")) && !currObj.isNull("updated")) {
							boolean updated = currObj.getString("updated").equals("1") || currObj.getInt("updated") == 1;
							lp = new LanguagePacket(currObj.getInt("id"), loc, currObj.getInt("translated"), existing.contains(currObj.getInt("id")), updated);
							lp.setDate(new Date(HolidaysDBHelper.getInstance(activity.get()).getLastUpdateDate(lp).getTime() * 1000));
						} else {
							lp = new LanguagePacket(currObj.getInt("id"), loc, currObj.getInt("translated"), existing.contains(currObj.getInt("id")), false);
						}
						activity.get().languages.add(lp);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			activity.get().progressDialog.cancel();
			Collections.sort(activity.get().languages);
			activity.get().listView.setAdapter(new LanguageAdapter(activity.get(), activity.get().languages));
		}
	}
}
