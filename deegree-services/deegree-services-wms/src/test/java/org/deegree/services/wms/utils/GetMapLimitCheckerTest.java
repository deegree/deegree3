/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.utils;

import org.deegree.commons.ows.exception.OWSException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link GetMapLimitChecker}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class GetMapLimitCheckerTest {

	private final GetMapLimitChecker checker = new GetMapLimitChecker();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void checkWidthWithNegativeValue() throws OWSException {
		thrown.expect(OWSException.class);
		thrown.expectMessage("Width must be positive.");
		checker.checkWidth(-1, null);
	}

	@Test
	public void checkWidthWithZero() throws OWSException {
		thrown.expect(OWSException.class);
		thrown.expectMessage("Width must be positive.");
		checker.checkWidth(0, null);
	}

	@Test
	public void checkWidthTooLarge() throws OWSException {
		thrown.expect(OWSException.class);
		thrown.expectMessage("Width out of range. Maximum width: 1024");
		checker.checkWidth(1025, 1024);
	}

	@Test
	public void checkWidthNoLimit() throws OWSException {
		checker.checkWidth(1025, null);
	}

	@Test
	public void checkWidthInRange() throws OWSException {
		checker.checkWidth(1024, 1024);
	}

	@Test
	public void checkHeightWithNegativeValue() throws OWSException {
		thrown.expect(OWSException.class);
		thrown.expectMessage("Height must be positive.");
		checker.checkHeight(-1, null);
	}

	@Test
	public void checkHeightWithZero() throws OWSException {
		thrown.expect(OWSException.class);
		thrown.expectMessage("Height must be positive.");
		checker.checkHeight(0, null);
	}

	@Test
	public void checkHeightTooLarge() throws OWSException {
		thrown.expect(OWSException.class);
		thrown.expectMessage("Height out of range. Maximum height: 1024");
		checker.checkHeight(1025, 1024);
	}

	@Test
	public void checkHeightNoLimit() throws OWSException {
		checker.checkHeight(1025, null);
	}

	@Test
	public void checkHeightInRange() throws OWSException {
		checker.checkHeight(1024, 1024);
	}

	@Test
	public void checkLayerCountInRange() throws OWSException {
		checker.checkLayerCount(10, 10);
	}

	@Test
	public void checkLayerCountTooLarge() throws OWSException {
		thrown.expect(OWSException.class);
		thrown.expectMessage("Too many layers requested. Maximum number of layers: 10");
		checker.checkLayerCount(100, 10);
	}

	@Test
	public void checkLayerCountNoLimit() throws OWSException {
		checker.checkLayerCount(100, null);
	}

}
