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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.LanguageAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Language;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.Util;
import java9.util.concurrent.CompletableFuture;
import java9.util.function.Supplier;
import lombok.SneakyThrows;

public class LanguageActivity extends AppCompatActivity {
	private Dialog progressDialog;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_language);

		progressDialog = new Dialog(this);
		progressDialog.setContentView(R.layout.layout_loading_dialog);
		progressDialog.setTitle(getString(R.string.downloading_data));
		progressDialog.setCancelable(false);

		Optional.of(this)
				.map(AppCompatActivity::getSupportActionBar)
				.ifPresent(actionBar -> actionBar.setDisplayHomeAsUpEnabled(true));
		final HolidaysDBHelper holidaysDBHelper = new HolidaysDBHelper(this);
		final Set<Language> languages = holidaysDBHelper.getLanguages();
		holidaysDBHelper.close();

		final ListView listView = findViewById(R.id.language_list_languages);
		listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		if (Util.isConnection(this)) {
			CompletableFuture.supplyAsync(new Downloader(), executorService)
					.thenAccept(languageList -> {
						languages.addAll(languageList);
						progressDialog.dismiss();
					});
		}
		listView.setAdapter(new LanguageAdapter(this, new ArrayList<>(languages)));

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

	private class Downloader implements Supplier<List<Language>> {
		@SneakyThrows
		@Override
		public List<Language> get() {
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
