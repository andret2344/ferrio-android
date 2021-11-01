package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.LanguageAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.Language;
import eu.andret.kalendarzswiatnietypowych.utils.Util;
import lombok.SneakyThrows;

public class LanguageActivity extends AppCompatActivity {
	private Dialog progressDialog;
	private ListView listView;

	@SneakyThrows
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Util util = new Util(this);
		util.applyTheme();
		setContentView(R.layout.activity_language);
		progressDialog = new Dialog(this);

		util.createAd(R.id.language_adview_bottom);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		listView = findViewById(R.id.language_list_languages);
		listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		if (util.isConnection()) {
			progressDialog.setTitle(getResources().getString(R.string.downloading_data));
			progressDialog.setCancelable(false);
			progressDialog.show();
			final ExecutorService executorService = Executors.newSingleThreadExecutor();
			final Future<List<Language>> future = executorService.submit(new LanguageActivity.Downloader());
			final List<Language> languages = future.get();
			progressDialog.dismiss();
			listView.setAdapter(new LanguageAdapter(this, languages));
		}
	}

	public ListView getListView() {
		return listView;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		HolidayCalendar.getInstance(this).refresh();
		NavUtils.navigateUpFromSameTask(this);
	}

	@Override
	protected void onPause() {
		progressDialog.cancel();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		progressDialog.cancel();
		super.onDestroy();
	}

	private static class Downloader implements Callable<List<Language>> {
		@Override
		public List<Language> call() throws IOException, JSONException {
			final HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.unusualcalendar.net/language/").openConnection();
			con.setRequestMethod("GET");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			final StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
			reader.close();
			final String result = stringBuilder.toString();
			if (result.isEmpty()) {
				return Collections.emptyList();
			}
			final JSONArray jsonArray = new JSONArray(result);
			final List<Language> languages = new ArrayList<>();
			for (int i = 0; i < jsonArray.length(); i++) {
				final JSONObject languageObject = jsonArray.getJSONObject(i);
				languages.add(new Language(languageObject.getString("language"), languageObject.getString("uniLanguage")));
			}
			return languages;
		}
	}
}
