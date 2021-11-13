package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerImage;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerItem;
import eu.andret.kalendarzswiatnietypowych.drawer.ViewItem;
import eu.andret.kalendarzswiatnietypowych.util.Data;

public class DrawerAdapter extends ArrayAdapter<ViewItem> {
	public DrawerAdapter(final Context context, final List<ViewItem> values) {
		super(context, R.layout.drawer_list_item, values);
	}

	private static class ViewHolder {
		private TextView name;
		private ImageView icon;
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert inflater != null;
			convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
			holder = new ViewHolder();
			holder.name = convertView.findViewById(R.id.drawer_item_text_name);
			holder.icon = convertView.findViewById(R.id.drawer_item_image_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Data.ColorSet color = Data.getColors(getContext());
		holder.name.setTextColor(color.getForegroundColor());
		convertView.setBackgroundColor(color.getBackgroundColor());
		final ViewItem v = getItem(position);
		if (v instanceof NavigationDrawerItem) {
			final NavigationDrawerItem ndi = (NavigationDrawerItem) v;
			holder.icon.setImageDrawable(ndi.getIcon());
			holder.name.setText(ndi.getName());
			convertView.setOnClickListener(ndi.getListener());
		} else if (v instanceof NavigationDrawerImage) {
			final NavigationDrawerImage ndi = (NavigationDrawerImage) v;
			holder.icon.setImageDrawable(ndi.getImage());
			holder.icon.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			convertView.setOnClickListener(ndi.getListener());
		}
		return convertView;
	}
}
