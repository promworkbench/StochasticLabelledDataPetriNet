package org.processmining.stochasticlabelleddatapetrinet.weights.fitting.weka;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class WekaUtil {

	private WekaUtil() {
	}

	//Cache for attribute keys as URLEncoding is quite expensive
	private static final ConcurrentMap<String, String> ESCAPE_CACHE = new ConcurrentHashMap<>(32, 0.75f, 4);
	private static final int ATTRIBUTE_NAME_CACHE_SIZE = 4096;

	public static String fixVarName(final String varName) {
		try {
			String fixedName = ESCAPE_CACHE.get(varName);
			if (fixedName == null) {
				// Quite naive eviction strategy, but this is fine for normal logs with not so many attribute names
				if (ESCAPE_CACHE.size() > ATTRIBUTE_NAME_CACHE_SIZE) {
					ESCAPE_CACHE.clear();
				}
				String preparedForUriEncoding = replaceNonUriEncodedChars(varName);
				String uriEncoded = URLEncoder.encode(preparedForUriEncoding, "utf-8");
				fixedName = uriEncoded.replace('%', '$');
				// Strings are immutable, so it does not matter which one is returned
				ESCAPE_CACHE.put(varName, fixedName);
			}
			return fixedName;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not escape variable " + varName, e);
		}
	}

	public static String wekaUnescape(String varName) {
		try {
			return URLDecoder.decode(varName.replace('$', '%'), "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not unescape variable " + varName, e);
		}
	}

	public static String replaceNonUriEncodedChars(String varName) {
		char charArray[] = varName.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			switch (charArray[i]) {
				case '-' :
				case '.' :
				case '~' :
				case '*' :
					charArray[i] = '$';
					break;
				default :
			}
		}
		return new String(charArray);
	}

}
