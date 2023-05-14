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
package org.deegree.commons.metadata;

import static org.deegree.commons.utils.CollectionUtils.map;

import java.util.List;

import org.deegree.commons.metadata.description.jaxb.KeywordsType;
import org.deegree.commons.metadata.description.jaxb.LanguageStringType;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.Pair;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class MetadataJAXBConverter {

	public static final Mapper<LanguageString, LanguageStringType> LANG_LANG_MAPPER = new Mapper<LanguageString, LanguageStringType>() {
		@Override
		public LanguageString apply(LanguageStringType u) {
			return new LanguageString(u.getValue(), u.getLang());
		}
	};

	/**
	 * Maps a string to language string.
	 */
	public static final Mapper<LanguageString, String> LANG_MAPPER = new Mapper<LanguageString, String>() {
		@Override
		public LanguageString apply(String u) {
			return new LanguageString(u, null);
		}
	};

	public static final Mapper<CodeType, org.deegree.commons.metadata.description.jaxb.CodeType> CODETYPE_MAPPER = new Mapper<CodeType, org.deegree.commons.metadata.description.jaxb.CodeType>() {
		@Override
		public CodeType apply(org.deegree.commons.metadata.description.jaxb.CodeType u) {
			if (u == null) {
				return null;
			}
			return new CodeType(u.getValue(), u.getCodeSpace());
		}
	};

	public static final Mapper<Pair<List<LanguageString>, CodeType>, KeywordsType> KW_MAPPER = new Mapper<Pair<List<LanguageString>, CodeType>, KeywordsType>() {
		@Override
		public Pair<List<LanguageString>, CodeType> apply(KeywordsType u) {
			return new Pair<List<LanguageString>, CodeType>(map(u.getKeyword(), LANG_LANG_MAPPER),
					CODETYPE_MAPPER.apply(u.getType()));
		}
	};

}
