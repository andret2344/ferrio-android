package eu.andret.kalendarzswiatnietypowych.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CancellableRequestTest {

	private OkHttpClient client;

	@Before
	public void setUp() {
		client = new OkHttpClient();
	}

	private Call newCall() {
		// A real OkHttp Call we never execute. Cancellation is observable via isCanceled().
		return client.newCall(new Request.Builder().url("http://example.invalid/").build());
	}

	@Test
	public void attachThenCancel_cancelsTheAttachedCall() {
		final CancellableRequest request = new CancellableRequest();
		final Call call = newCall();
		request.attach(call);

		request.cancel();

		assertThat(call.isCanceled()).isTrue();
	}

	@Test
	public void cancelThenAttach_cancelsTheCallOnAttach() {
		final CancellableRequest request = new CancellableRequest();
		final Call call = newCall();
		request.cancel();

		request.attach(call);

		assertThat(call.isCanceled()).isTrue();
	}

	@Test
	public void doubleCancel_afterAttach_isIdempotent() {
		final CancellableRequest request = new CancellableRequest();
		final Call call = newCall();
		request.attach(call);

		request.cancel();
		request.cancel();

		assertThat(call.isCanceled()).isTrue();
	}

	@Test
	public void doubleCancel_beforeAttach_cancelsCallOnAttach() {
		final CancellableRequest request = new CancellableRequest();
		final Call call = newCall();
		request.cancel();
		request.cancel();

		request.attach(call);

		assertThat(call.isCanceled()).isTrue();
	}

	@Test
	public void neverCancelled_doesNotCancelTheCall() {
		final CancellableRequest request = new CancellableRequest();
		final Call call = newCall();
		request.attach(call);

		assertThat(call.isCanceled()).isFalse();
	}
}
