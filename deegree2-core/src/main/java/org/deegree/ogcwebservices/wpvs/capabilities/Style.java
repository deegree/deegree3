//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.ogcwebservices.wpvs.capabilities;

import org.deegree.model.metadata.iso19115.Keywords;

/**
 * This class represents a style object.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class Style {

    private String name;

	private String title;

	private String abstract_;

	private Keywords[] keywords;

	private Identifier identifier;

	private LegendURL[] legendURLs;

	private StyleSheetURL styleSheetURL;

	private StyleURL styleURL;

	/**
	 * Creates a new style object from the given parameters.
	 *
	 * @param name
	 * @param title
	 * @param abstract_
	 * @param keywords
	 * @param identifier
	 * @param legendURLs
	 * @param styleSheetURL
	 * @param styleURL
	 */
	public Style( String name, String title, String abstract_, Keywords[] keywords,
				Identifier identifier, LegendURL[] legendURLs, StyleSheetURL styleSheetURL,
				StyleURL styleURL ) {
        this.name = name;
		this.title = title;
		this.abstract_ = abstract_;
		this.keywords = keywords;
		this.identifier = identifier;
		this.legendURLs = legendURLs;
		this.styleSheetURL = styleSheetURL;
		this.styleURL = styleURL;
    }

	/**
	 * @return Returns the abstract_.
	 */
	public String getAbstract() {
		return abstract_;
	}

	/**
	 * @return Returns the identifier.
	 */
	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * @return Returns the keywords.
	 */
	public Keywords[] getKeywords() {
		return keywords;
	}

	/**
	 * @return Returns the legendURLs.
	 */
	public LegendURL[] getLegendURLs() {
		return legendURLs;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the styleSheetURL.
	 */
	public StyleSheetURL getStyleSheetURL() {
		return styleSheetURL;
	}

	/**
	 * @return Returns the styleURL.
	 */
	public StyleURL getStyleURL() {
		return styleURL;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

}
