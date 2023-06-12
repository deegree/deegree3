/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.iso.persistence.queryable;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Properties;

import org.deegree.commons.config.ResourceInitException;
import org.slf4j.Logger;

/**
 * Replaces written codes (as string) with the code number.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class InspireDataThemeNormalizer implements QueryableConverter {

	private static final Logger LOG = getLogger(InspireDataThemeNormalizer.class);

	private static final String pattern = "GEMET[\\s-]*INSPIRE themes.*";

	private Properties props;

	public InspireDataThemeNormalizer() throws ResourceInitException {
		props = new Properties();
		try {
			props.load(InspireDataThemeNormalizer.class.getResourceAsStream("inspireThemes.properties"));
		}
		catch (IOException e) {
			throw new ResourceInitException("Could not load properties file 'inspireThemes.properties'", e);
		}
	}

	@Override
	public String convert(String toConvert) {
		String converted = toConvert;
		if (props != null) {
			converted = props.getProperty(toConvert, toConvert);
			LOG.debug("Replace {} with {}.", toConvert, converted);
		}
		return converted;
	}

}
