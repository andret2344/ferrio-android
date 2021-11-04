package eu.andret.kalendarzswiatnietypowych.entity;

import lombok.Value;

@Value
public class Language implements Comparable<Language> {
	String name;
	String code;

	@Override
	public int compareTo(final Language language) {
		return name.compareTo(language.name);
	}
}
