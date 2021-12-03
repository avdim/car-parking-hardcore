package com.murkitty.parking;
public enum Language {
	en,
	ru;
public static Language get(String lang) {
	for(Language language : values()) {
		if(language.name().equals(lang)) {
			return language;
		}
	}
	return en;
}
public static Language get2(String lang) {
	if(lang != null) {
		if(lang.toLowerCase().equals("ru")) {
			return ru;
		}
	}
	return en;
}
}
