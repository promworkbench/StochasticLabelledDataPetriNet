package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.awt.Color;

public enum ColorScheme {

	//Source http://colorbrewer2.org/ 
	// to light new Color(247, 251, 255), new Color(222, 235, 247), 
	BLUE_SINGLE_HUE(new Color[] { new Color(158, 202, 225), new Color(107, 174, 214), new Color(66, 146, 198), new Color(33, 113, 181),
			new Color(8, 81, 156) }, new Color(8, 48, 107)),

	//Source http://colorbrewer2.org/
	GREY_SINGLE_HUE(new Color[] { new Color(189, 189, 189), new Color(150, 150, 150), new Color(115, 115, 115),
			new Color(82, 82, 82) }, new Color(37, 37, 37));

	private final Color[] scheme;
	private final Color defaultColor;

	private ColorScheme(Color[] scheme, Color defaultColor) {
		this.scheme = scheme;
		this.defaultColor = defaultColor;
	}

	public Color[] getColors() {
		return scheme;
	}

	public Color getColor(int index) {
		return getColor(index, defaultColor);
	}

	public Color getColor(int index, Color defaultColor) {
		if (index >= scheme.length) {
			return defaultColor;
		}
		return scheme[index];
	}

	public Color getColorFromGradient(double factor) {
		return getColorFromGradient(factor, defaultColor);
	}

	public Color getColorFromGradient(double factor, Color defaultColor) {
		return getColorFromGradient(factor, scheme, defaultColor);
	}

	public static Color getColorFromGradient(double factor, Color[] colorScheme, Color defaultColor) {
		if (factor >= 1.0) {
			// Special case
			return defaultColor;
		}
		double bucketSize = 1.0f / colorScheme.length;
		int maxIndex = colorScheme.length - 1;
		int minIndex = 0;
		int bucket = Math.min(maxIndex, Math.max(minIndex, (int) Math.floor(factor / bucketSize)));
		return colorScheme[bucket];
	}

}