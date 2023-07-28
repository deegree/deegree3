/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.commons.ows.metadata;

import static org.deegree.commons.metadata.MetadataJAXBConverter.KW_MAPPER;
import static org.deegree.commons.metadata.MetadataJAXBConverter.LANG_LANG_MAPPER;
import static org.deegree.commons.utils.CollectionUtils.map;

import java.util.List;

import org.deegree.commons.metadata.description.jaxb.KeywordsType;
import org.deegree.commons.metadata.description.jaxb.LanguageStringType;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class DescriptionConverter {

	public static Description fromJaxb(List<LanguageStringType> titles, List<LanguageStringType> abstracts,
			List<KeywordsType> keywords) {
		Description desc = new Description(null, null, null, null);
		if (titles != null) {
			desc.setTitles(map(titles, LANG_LANG_MAPPER));
		}
		if (abstracts != null) {
			desc.setAbstracts(map(abstracts, LANG_LANG_MAPPER));
		}
		if (keywords != null) {
			desc.setKeywords(map(keywords, KW_MAPPER));
		}
		return desc;
	}

}
