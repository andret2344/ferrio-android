package eu.andret.kalendarzswiatnietypowych.widget;

import androidx.annotation.NonNull;

import eu.andret.kalendarzswiatnietypowych.R;

public class TransparentWidgetProvider extends BaseWidgetProvider {

	@Override
	protected int getLayoutResId() {
		return R.layout.widget_transparent;
	}

	@NonNull
	@Override
	protected Class<? extends BaseWidgetProvider> getProviderClass() {
		return TransparentWidgetProvider.class;
	}
}
