package eu.andret.kalendarzswiatnietypowych;

import android.app.Application;

import eu.andret.kalendarzswiatnietypowych.persistance.AppRepository;

public class UHCApplication extends Application {
	private AppRepository appRepository;

	@Override
	public void onCreate() {
		super.onCreate();
		appRepository = new AppRepository(this);
	}

	public AppRepository getAppRepository() {
		return appRepository;
	}
}
