package eu.andret.kalendarzswiatnietypowych.widget;

import androidx.annotation.NonNull;

import eu.andret.kalendarzswiatnietypowych.R;

public class WidgetProvider extends BaseWidgetProvider {

	@Override
	protected int getLayoutResId() {
		return R.layout.widget;
	}

	@NonNull
	@Override
	protected Class<? extends BaseWidgetProvider> getProviderClass() {
		return WidgetProvider.class;
	}
}
