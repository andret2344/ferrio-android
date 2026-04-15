package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public final class NetworkMonitor extends LiveData<Boolean> {
	private static final String TAG = "Ferrio-NetworkMonitor";
	private final ConnectivityManager connectivityManager;
	private final ConnectivityManager.NetworkCallback networkCallback;

	public NetworkMonitor(@NonNull final Context context) {
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		networkCallback = new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(@NonNull final Network network) {
				postValue(true);
			}

			@Override
			public void onLost(@NonNull final Network network) {
				postValue(isCurrentlyConnected());
			}

			@Override
			public void onUnavailable() {
				postValue(false);
			}
		};
		setValue(isCurrentlyConnected());
	}

	@MainThread
	@Override
	protected void onActive() {
		final NetworkRequest request = new NetworkRequest.Builder()
				.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
				.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
				.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
				.addTransportType(NetworkCapabilities.TRANSPORT_VPN)
				.build();
		connectivityManager.registerNetworkCallback(request, networkCallback);
	}

	@MainThread
	@Override
	protected void onInactive() {
		try {
			connectivityManager.unregisterNetworkCallback(networkCallback);
		} catch (final Exception ex) {
			Log.w(TAG, "Failed to unregister network callback", ex);
		}
	}

	private boolean isCurrentlyConnected() {
		final Network active = connectivityManager.getActiveNetwork();
		if (active == null) {
			return false;
		}
		final NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(active);
		return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
				&& (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
				|| caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
				|| caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
				|| caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
	}
}
