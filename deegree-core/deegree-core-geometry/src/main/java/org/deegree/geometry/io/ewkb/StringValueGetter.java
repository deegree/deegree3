/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2004 Paul Ramsey, pramsey@refractions.net
 Copyright (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 Copyright (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 Copyright (C) 2018-2023 Sebastian Baumhekel, sebastian.baumhekel@gmail.com
 Copyright (C) 2026 grit graphische Informationstechnik Beratungsgesellschaft mbH

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

 e-mail: info@deegree.org

 website: https://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.geometry.io.ewkb;

import org.deegree.cs.coordinatesystems.ICRS;

import java.util.function.IntFunction;

/**
 * Allows reading values from a string.
 *
 * @author Sebastian Baumhekel
 */
public class StringValueGetter extends ValueGetter {

	private int position;

	private final String value;

	/**
	 * Constructs an instance.
	 * @param value value as hex string
	 */
	public StringValueGetter(Axis axis, IntFunction<ICRS> sridToCrs, String value) {
		super(axis, sridToCrs);
		this.value = value;
	}

	@Override
	protected int getNextByte() {
		// get current position (and respect that every byte consists of 2 hex characters)
		int index = position * 2;
		// then advance the position to the next byte
		position++;
		return ((EWKBUtils.toHexByte(value.charAt(index)) << 4) | EWKBUtils.toHexByte(value.charAt(index + 1)));
	}

}
