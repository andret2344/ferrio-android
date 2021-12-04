package eu.andret.kalendarzswiatnietypowych.entity;

import lombok.NonNull;
import lombok.Value;

@Value
public class Language implements Comparable<Language> {
	@NonNull
	String name;
	@NonNull
	String code;
	@NonNull
	String url;

	@Override
	public int compareTo(final Language language) {
		return name.compareTo(language.name);
	}
}
