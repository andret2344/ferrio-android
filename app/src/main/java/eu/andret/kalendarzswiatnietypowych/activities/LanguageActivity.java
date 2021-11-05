package eu.andret.kalendarzswiatnietypowych.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.LanguageAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Language;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class LanguageActivity extends AppCompatActivity {
	private Dialog progressDialog;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_language);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		final HolidaysDBHelper holidaysDBHelper = new HolidaysDBHelper(this);
		final Set<Language> languages = holidaysDBHelper.getLanguages();
		holidaysDBHelper.close();

		final ListView listView = findViewById(R.id.language_list_languages);
		listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		if (Util.isConnection(this)) {
			try {
				progressDialog = new Dialog(this);
				progressDialog.setTitle(getResources().getString(R.string.downloading_data));
				progressDialog.setCancelable(false);
				progressDialog.show();
				final ExecutorService executorService = Executors.newSingleThreadExecutor();
				final Future<List<Language>> future = executorService.submit(new Downloader());
				languages.addAll(future.get());
				progressDialog.dismiss();
			} catch (final InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
			}
		}
		listView.setAdapter(new LanguageAdapter(this, new ArrayList<>(languages)));

		MobileAds.initialize(this);
		final AdView adView = findViewById(R.id.language_adview_bottom);
		adView.loadAd(new AdRequest.Builder().build());
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

	private class Downloader implements Callable<List<Language>> {
		@Override
		public List<Language> call() throws IOException, JSONException {
			final HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.unusualcalendar.net/language/").openConnection();
			final InputStream in = con.getInputStream();
			final int length = con.getHeaderFieldInt("Content-Length", -1);

			final byte[] bytes = new byte[length];
			for (int i = 0; i < length; i++) {
				if (!Util.isConnection(LanguageActivity.this)) {
					Util.createAlert(LanguageActivity.this, R.string.caution, R.string.no_internet);
					return Collections.emptyList();
				}
				bytes[i] = (byte) in.read();
			}
			in.close();
			final String result = new String(bytes, StandardCharsets.UTF_8);
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
