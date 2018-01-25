package eu.andret.kalendarzswiatnietypowych.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.MyWidgetProvider;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.DrawerAdapter;
import eu.andret.kalendarzswiatnietypowych.adapters.MonthFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.adapters.SearchHolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerImage;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerItem;
import eu.andret.kalendarzswiatnietypowych.drawer.ViewItem;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.LanguagePacket;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class MainActivity extends AppCompatActivity {
	private Util util;
	private static MainActivity instance;
	private final Calendar calendar = Calendar.getInstance();
	private DrawerLayout navigationDrawer;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private ViewPager pager;
	private ListView list;
	private PowerManager.WakeLock wakeLock;
	private LinearLayout preloaderLayout;
	private int counter = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		instance = this;
		String f = getIntent().getStringExtra("from");
		if (f != null && (f.equals("widget") || f.equals("notification"))) {
			Intent i = new Intent(this, DayActivity.class);
			i.putExtra("from", "calendar");
			i.putExtra("day", getIntent().getIntExtra("day", 1));
			i.putExtra("month", getIntent().getIntExtra("month", 1));
			startActivity(i);
		}
		Intent i = new Intent(this, SurveyActivity.class);
		i.putExtra("title", "tytul");
		StringBuilder pytanie = new StringBuilder();
		for (int j = 0; j < 100; j++) {
			pytanie.append("\nLinia no ").append(j);
		}
		i.putExtra("content", pytanie.toString());
		i.putExtra("type", 0);
		i.putExtra("answers", new String[]{"Odp", "Dluga odpowiedz jakas tam Dluga odpowiedz jakas tam Dluga odpowiedz jakas tam", "Kolejna krotka", "A", "B", "C", "D", "E"});
		// startActivity(i);
		super.onCreate(savedInstanceState);
		
		util = new Util(this);
		util.applyTheme();
		
		ViewGroup v = (ViewGroup) getWindow().getDecorView().getRootView();
		preloaderLayout = new LinearLayout(this);
		preloaderLayout.setOrientation(LinearLayout.VERTICAL);
		preloaderLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		preloaderLayout.setBackgroundColor(Data.MyColor.BLACK);
		v.addView(preloaderLayout);
		
		ImageView image = new ImageView(this);
		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		imageParams.gravity = Gravity.CENTER;
		image.setLayoutParams(imageParams);
		image.setImageResource(R.drawable.ic_app_logo);
		preloaderLayout.addView(image);

		HolidaysDBHelper.getInstance(this).test1();

		TextView text = new TextView(this);
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		textParams.gravity = Gravity.CENTER;
		text.setGravity(Gravity.CENTER);
		text.setText(R.string.app_name);
		text.setLayoutParams(textParams);
		text.setTextSize(getResources().getDimension(R.dimen.drawer_list_name_text));
		// preloaderLayout.addView(text);
		
		ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
		LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		progressParams.gravity = Gravity.CENTER;
		progress.setLayoutParams(progressParams);
		preloaderLayout.addView(progress);
		
		// Log.d("AC", "density=" + getApplicationContext().getResources().getDisplayMetrics().density);
		
		setContentView(R.layout.activity_main);
		PowerManager pm = (PowerManager) MainActivity.this.getSystemService(Context.POWER_SERVICE);
		assert pm != null;
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wakeLock.acquire(2*60*1000L /*é0 minutes*/);
		
		list = findViewById(R.id.main_list_results);
		
		setUpNavigationDrawer();
		pager = findViewById(R.id.main_pager_months);
		pager.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager()));
		pager.setCurrentItem(calendar.get(Calendar.MONTH));
		getSupportActionBar().setTitle(new Util(instance).getMonth(pager.getCurrentItem()));
		pager.setOffscreenPageLimit(12);
		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				getSupportActionBar().setTitle(new Util(instance).getMonth(position));
			}
			
			@Override
			public void onPageSelected(int position) {}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
		
		util.createAd(R.id.main_adview_bottom);
		// util.createNotification("UHC", "Today is", R.drawable.ic_launcher, getIntent(), false);
		
		// startService(new Intent(this, NotificationService.class));
		update();
		
	}
	
	@Override
	public void onBackPressed() {
		if (navigationDrawer.isDrawerOpen(Gravity.START)) {
			navigationDrawer.closeDrawer(Gravity.START);
		} else {
			wakeLock.release();
			super.onBackPressed();
		}
	}
	
	public void setUpNavigationDrawer() {
		TypedArray ta = obtainStyledAttributes(R.styleable.images);
		List<ViewItem> data = new ArrayList<>();
		data.add(new NavigationDrawerImage(ContextCompat.getDrawable(this, R.drawable.ic_launcher), null));
		data.add(new NavigationDrawerItem(R.string.settings, ta.getDrawable(R.styleable.images_settings), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
			}
		}));
		
		// data.add(
		new NavigationDrawerItem(R.string.menu_favourites, ta.getDrawable(R.styleable.images_star), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			
			}
		});
		// );
		
		data.add(new NavigationDrawerItem(R.string.languages, ta.getDrawable(R.styleable.images_translate), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), LanguageActivity.class));
			}
		}));
		
		data.add(new NavigationDrawerItem(R.string.rate, ta.getDrawable(R.styleable.images_thumb_up), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String packet = "eu.andret.kalendarzswiatnietypowych";
				Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packet));
				boolean marketFound = false;
				
				final List<ResolveInfo> otherApps = getPackageManager().queryIntentActivities(rateIntent, 0);
				for (ResolveInfo otherApp : otherApps) {
					if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {
						ActivityInfo otherAppActivity = otherApp.activityInfo;
						ComponentName componentName = new ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name);
						rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						rateIntent.setComponent(componentName);
						startActivity(rateIntent);
						marketFound = true;
						break;
					}
				}
				
				if (!marketFound) {
					Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packet));
					startActivity(webIntent);
				}
			}
		}));
		
		data.add(new NavigationDrawerItem(R.string.about_calendar, ta.getDrawable(R.styleable.images_event), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				util.createAlert(R.string.about_calendar, R.string.about_calendar_text);
			}
		}));
		
		data.add(new NavigationDrawerItem(R.string.about_holidays, ta.getDrawable(R.styleable.images_format_quote), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				util.createAlertWithImage(R.drawable.holidays, R.string.about_holidays, R.string.about_holidays_text);
			}
		}));
		
		data.add(new NavigationDrawerItem(R.string.about_app, ta.getDrawable(R.styleable.images_info), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				util.createAlert(R.string.about_app, R.string.about_app_text);
			}
		}));
		
		// data.add(
		new NavigationDrawerItem(R.string.recomend_also, ta.getDrawable(R.styleable.images_myfeasts), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String pck = "eu.deyanix.myfeasts";
				Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pck));
				boolean marketFound = false;
				
				final List<ResolveInfo> otherApps = getPackageManager().queryIntentActivities(rateIntent, 0);
				for (ResolveInfo otherApp : otherApps) {
					if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {
						ActivityInfo otherAppActivity = otherApp.activityInfo;
						ComponentName componentName = new ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name);
						rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						rateIntent.setComponent(componentName);
						startActivity(rateIntent);
						marketFound = true;
						break;
					}
				}
				
				if (!marketFound) {
					Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pck));
					startActivity(webIntent);
				}
			}
		});
		// );
		
		navigationDrawer = findViewById(R.id.main_drawer_main);
		drawerList = findViewById(R.id.main_list_drawer);
		drawerToggle = new ActionBarDrawerToggle(this, navigationDrawer, R.string.drawer_open, R.string.drawer_close);
		navigationDrawer.addDrawerListener(drawerToggle);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		drawerList.setAdapter(new DrawerAdapter(this, data));
		ta.recycle();
	}
	
	public void attemptLanguages(final List<LanguagePacket> list) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final String[] languages = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			String name = list.get(i).getLocale().getDisplayName();
			name = name.substring(0, 1).toUpperCase(list.get(i).getLocale()) + name.substring(1).toLowerCase(list.get(i).getLocale());
			languages[i] = name;
		}
		alert.setTitle("Select language");
		alert.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				Intent i = new Intent(instance, LanguageActivity.class);
				i.putExtra("lang", list.get(item).getId());
				i.putExtra("pos", item);
				dialog.dismiss();
				startActivity(i);
			}
		});
		alert.show();
		
		// if (HolidaysDBHelper.getInstance(this).getExistingLanguagesIds().isEmpty()) {
		// AlertDialog.Builder alert = new AlertDialog.Builder(this);
		// alert.setTitle(R.string.caution);
		// alert.setMessage(R.string.no_languages);
		// alert.setCancelable(false);
		// alert.setNegativeButton(R.string.download, new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// startActivity(new Intent(instance, LanguageActivity.class));
		// }
		// });
		//
		// alert.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// instance.finish();
		// }
		// });
		// alert.show();
		// }
	}
	
//	public void attemptSurveys() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					String deviceId = Secure.getString(instance.getContentResolver(), Secure.ANDROID_ID);
//					String data = "id=" + deviceId;
//					byte[] dataBytes = data.getBytes("UTF-8");
//
//					HttpURLConnection conn = (HttpURLConnection) new URL("http://uhc.polishgames.net/survey.php").openConnection();
//					conn.setRequestMethod("POST");
//					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//					conn.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));
//					conn.setDoOutput(true);
//					conn.getOutputStream().write(dataBytes);
//					conn.getOutputStream().close();
//					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//					String result = reader.readLine();
//					JSONObject json = new JSONObject(result);
//					boolean response = json.getBoolean("result");
//					if (!response && util.isConnection()) {
//						final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//						LayoutInflater adbInflater = LayoutInflater.from(MainActivity.this);
//						final View alertSurveyLayout = adbInflater.inflate(R.layout.alert_survey, null, false);
//						alert.setView(alertSurveyLayout);
//						alert.setTitle(R.string.caution);
//						alert.setMessage(R.string.question);
//						final RadioGroup rg = alertSurveyLayout.findViewById(R.id.alert_survey_radiogroup_selection);
//						alert.setNegativeButton(R.string.send, new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								new Thread(new Runnable() {
//									@Override
//									public void run() {
//										try {
//											String deviceId = Secure.getString(instance.getContentResolver(), Secure.ANDROID_ID);
//											String data = "id=" + deviceId;
//											data += "&answer=" + rg.indexOfChild(rg.findViewById(rg.getCheckedRadioButtonId()));
//											byte[] dataBytes = data.getBytes("UTF-8");
//
//											HttpURLConnection conn = (HttpURLConnection) new URL("http://uhc.polishgames.net/survey.php").openConnection();
//											conn.setRequestMethod("POST");
//											conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//											conn.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));
//											conn.setDoOutput(true);
//											conn.getOutputStream().write(dataBytes);
//											conn.getOutputStream().close();
//											BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//											String result = reader.readLine();
//											reader.close();
//											SharedPreferences pref = Data.getPreferences(instance, Data.Prefs.SURVEY);
//											SharedPreferences.Editor editor = pref.edit();
//											editor.putBoolean("answered", true);
//											editor.apply();
//										} catch (Exception ex) {
//											ex.printStackTrace();
//										}
//									}
//								}).start();
//							}
//						});
//						runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								final AlertDialog alertDialog = alert.show();
//								alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
//
//								rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//									@Override
//									public void onCheckedChanged(RadioGroup group, int checkedId) {
//										alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
//									}
//								});
//
//							}
//						});
//					}
//					reader.close();
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//		}).start();
//	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (drawerToggle != null) {
			drawerToggle.syncState();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		wakeLock.acquire(2*60*1000L /*10 minutes*/);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		wakeLock.release();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem searchItem = menu.findItem(R.id.menu_main_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();
		final ArrayList<HolidayDay> originalList = new ArrayList<>(HolidayCalendar.getInstance(this).getAllDays());
		final ArrayList<HolidayDay> list = new ArrayList<>(originalList);
		final SharedPreferences theme = Data.getPreferences(this, Data.Prefs.THEME);
		final SearchHolidayAdapter adapter = new SearchHolidayAdapter(this, list);
		instance.list.setAdapter(adapter);
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				list.clear();
				if (newText == null || newText.equals("")) {
					list.clear();
					list.addAll(originalList);
					instance.list.setVisibility(View.INVISIBLE);
					instance.pager.setVisibility(View.VISIBLE);
				} else {
					instance.list.setVisibility(View.VISIBLE);
					instance.pager.setVisibility(View.INVISIBLE);
					for (int i = 0; i < originalList.size(); i++) {
						HolidayDay ho = originalList.get(i);
						List<Holiday> holidaysTmpList = ho.getHolidaysList(theme.getBoolean(getResources().getString(R.string.settings_usual_holidays), false));
						for (Holiday hd : holidaysTmpList) {
							if (hd.getText().toLowerCase(Locale.getDefault()).contains(newText.toLowerCase(Locale.getDefault()))) {
								try {
									HolidayDay hday = (HolidayDay) ho.clone();

								hday.getHolidays().clear();
								for (Holiday h : holidaysTmpList) {
									if (h.getText().toLowerCase(Locale.getDefault()).contains(newText.toLowerCase(Locale.getDefault()))) {
										hday.getHolidays().add(h);
									}
								}
								list.add(hday);
								break;
								} catch (CloneNotSupportedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				Collections.sort(list);
				adapter.notifyDataSetChanged();
				return false;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_main_today:
				Calendar cal = Calendar.getInstance();
				pager.setCurrentItem(cal.get(Calendar.MONTH));
				break;
			default:
				break;
		}
		return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}
	
	public void update() {
		int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), MyWidgetProvider.class));
		new MyWidgetProvider().onUpdate(this, AppWidgetManager.getInstance(this), ids);
		pager.invalidate();
		pager.refreshDrawableState();
		SharedPreferences theme = Data.getPreferences(this, Data.Prefs.THEME);
		Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getResources().getString(R.string.settings_theme_app), "1")));
		navigationDrawer.setBackgroundColor(color.background);
		drawerList.setBackgroundColor(color.background);
		findViewById(R.id.main_relative_main).setBackgroundColor(color.background);
	}
	
	public static MainActivity getInstance() {
		return instance;
	}
	
	public void set(int id, boolean smooth) {
		pager.setCurrentItem(id, smooth);
	}
	
	@SuppressLint("StaticFieldLeak")
	public void dismissPreloader() {
		if (counter < 12) {
			counter++;
			return;
		}
		final List<LanguagePacket> languages = new ArrayList<>();
		final AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
		fadeOut.setStartOffset(1500);
		fadeOut.setDuration(500);
		fadeOut.setFillAfter(false);
		fadeOut.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				preloaderLayout.setVisibility(View.INVISIBLE);
				if (HolidaysDBHelper.getInstance(instance).getExistingLanguagesIds().isEmpty() && util.isConnection()) {
					attemptLanguages(languages);
					// attemptSurveys();
				}
			}
		});
		if (HolidaysDBHelper.getInstance(this).getExistingLanguagesIds().isEmpty() && util.isConnection()) {
			new AsyncTask<Void, Integer, String>() {
				@Override
				protected String doInBackground(Void... params) {
					try {
						HttpURLConnection con = (HttpURLConnection) new URL("https://andret.eu/uhc/api/lang.php").openConnection();
						con.setDoOutput(true);
						PrintStream ps = new PrintStream(con.getOutputStream());
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
							List<Integer> existing = HolidaysDBHelper.getInstance(MainActivity.this).getExistingLanguagesIds();
							JSONArray jsonArray = jsonObject.getJSONArray("languages");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject currObj = jsonArray.getJSONObject(i);
								Locale loc = new Locale(currObj.getString("name"));
								languages.add(new LanguagePacket(currObj.getInt("id"), loc, currObj.getInt("translated"), existing.contains(currObj.getInt("id")), false));
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Collections.sort(languages);
					preloaderLayout.startAnimation(fadeOut);
				}
			}.execute();
		} else {
			preloaderLayout.startAnimation(fadeOut);
		}
	}
}
