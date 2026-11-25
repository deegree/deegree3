package org.deegree.commons.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class MapUtilsTests {

	@Test
	public void testCalcScaleDenominatorAndScaleHint() {
		double scaleDenom = 500;
		double scaleHint = MapUtils.calcScaleHint(scaleDenom);
		assertEquals(0.1979898987322333, scaleHint, 0d);

		double scaleDenomFromHint = MapUtils.calcScaleDenominator(scaleHint);
		assertEquals(scaleDenom, scaleDenomFromHint, 0d);
	}

}
