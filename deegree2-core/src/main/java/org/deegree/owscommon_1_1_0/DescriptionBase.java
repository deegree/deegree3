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

package org.deegree.owscommon_1_1_0;

import java.util.List;

/**
 * <code>IdentificationBase</code> super class of all description elements.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class DescriptionBase {

    private final List<String> titles;

    private final List<String> abstracts;

    private final List<Keywords> keywords;

    /**
     * @param titles
     *            a list of titles.
     * @param abstracts
     *            a list of abstracts.
     * @param keywords
     *            a list of keywords
     */
    public DescriptionBase( List<String> titles, List<String> abstracts, List<Keywords> keywords ) {
        this.titles = titles;
        this.abstracts = abstracts;
        this.keywords = keywords;
    }

    /**
     * @return the titles.
     */
    public final List<String> getTitles() {
        return titles;
    }

    /**
     * @return the abstracts.
     */
    public final List<String> getAbstracts() {
        return abstracts;
    }

    /**
     * @return the keywords.
     */
    public final List<Keywords> getKeywords() {
        return keywords;
    }

}
