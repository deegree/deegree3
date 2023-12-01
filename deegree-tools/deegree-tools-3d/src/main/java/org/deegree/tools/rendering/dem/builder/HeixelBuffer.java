package org.deegree.tools.rendering.dem.builder;

import java.util.HashMap;
import java.util.Map;

class HeixelBuffer {

	private final Map<String, Float> xyToHeight = new HashMap<String, Float>();

	Float getHeight(float x, float y) {
		String s = Math.round(x) + "," + Math.round(y);
		return xyToHeight.get(s);
	}

	public void putHeight(float x, float y, float height) {
		String s = Math.round(x) + "," + Math.round(y);
		xyToHeight.put(s, height);
	}

	@Override
	public String toString() {
		return "heixels: " + xyToHeight.size();
	}

}
