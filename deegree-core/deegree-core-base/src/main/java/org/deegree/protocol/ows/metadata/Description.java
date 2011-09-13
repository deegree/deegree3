//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.ows.metadata;

import static java.util.Collections.emptyList;

import java.util.List;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;

/**
 * Encapsulates descriptive information on an object described in OGC web service metadata.
 * <p>
 * Data model has been designed to capture the expressiveness of all OWS specifications and versions and was verified
 * against the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 * </p>
 * <p>
 * From OWS Common 2.0: <cite>Human-readable descriptive information for the object it is included within. This type
 * shall be extended if needed for specific OWS use to include additional metadata for each type of information. This
 * type shall not be restricted for a specific OWS to change the multiplicity (or optionality) of some elements. If the
 * xml:lang attribute is not included in a Title, Abstract or Keyword element, then no language is specified for that
 * element unless specified by another means. All Title, Abstract and Keyword elements in the same Description that
 * share the same xml:lang attribute value represent the description of the parent object in that language. Multiple
 * Title or Abstract elements shall not exist in the same Description with the same xml:lang attribute value unless
 * otherwise specified.</cite>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Description {

    private final String name;

    private List<LanguageString> titles;

    private List<LanguageString> abstracts;

    private List<Pair<List<LanguageString>, CodeType>> keywords;

    public Description( String name, List<LanguageString> titles, List<LanguageString> abstracts,
                        List<Pair<List<LanguageString>, CodeType>> keywords ) {
        this.name = name;
        if ( titles != null ) {
            this.titles = titles;
        } else {
            this.titles = emptyList();
        }
        if ( abstracts != null ) {
            this.abstracts = abstracts;
        } else {
            this.abstracts = emptyList();
        }
        if ( keywords != null ) {
            this.keywords = keywords;
        } else {
            this.keywords = emptyList();
        }
    }

    /**
     * Returns the name of the object.
     * 
     * @deprecated This information is only provided by some older OWS specifications (e.g. WFS 1.0.0). Newer service
     *             specifications don't define it.
     * @return name of the object, can be <code>null</code>
     */
    public String getName() {
        return name;
    }

    /**
     * @return title, never <code>null</code>
     */
    public List<LanguageString> getTitles() {
        return titles;
    }

    public void setTitles( List<LanguageString> titles ) {
        this.titles = titles;
    }

    /**
     * @return serviceAbstract, never <code>null</code>
     */
    public List<LanguageString> getAbstracts() {
        return abstracts;
    }

    public void setAbstracts( List<LanguageString> abstracts ) {
        this.abstracts = abstracts;
    }

    /**
     * @return keywords, never <code>null</code>
     */
    public List<Pair<List<LanguageString>, CodeType>> getKeywords() {
        return keywords;
    }

    public void setKeywords( List<Pair<List<LanguageString>, CodeType>> keywords ) {
        this.keywords = keywords;
    }
}
