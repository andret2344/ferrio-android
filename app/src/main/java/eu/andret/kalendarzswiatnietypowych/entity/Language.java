package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Language implements Comparable<Language> {
	@NonNull
	private final String name;
	@NonNull
	private final String code;
	@NonNull
	private final String url;

	public Language(@NonNull final String name, @NonNull final String code, @NonNull final String url) {
		this.name = name;
		this.code = code;
		this.url = url;
	}

	@NonNull
	public String getName() {
		return name;
	}

	@NonNull
	public String getCode() {
		return code;
	}

	@NonNull
	public String getUrl() {
		return url;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Language language = (Language) o;
		return Objects.equals(name, language.name) && Objects.equals(code, language.code) && Objects.equals(url, language.url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, code, url);
	}

	@NonNull
	@Override
	public String toString() {
		return "Language{" +
				"name='" + name + '\'' +
				", code='" + code + '\'' +
				", url='" + url + '\'' +
				'}';
	}

	@Override
	public int compareTo(final Language language) {
		return name.compareTo(language.name);
	}
}
