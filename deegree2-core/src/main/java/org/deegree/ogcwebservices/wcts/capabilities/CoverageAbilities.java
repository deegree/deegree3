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

package org.deegree.ogcwebservices.wcts.capabilities;

import java.util.List;

import org.deegree.framework.util.Pair;
import org.w3c.dom.Element;

/**
 * <code>CoverageAbilities</code> specify coverage transformation abilities of WCTS server, this class encapsulates
 * them.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class CoverageAbilities {

    private final List<Pair<String, String>> coverageTypes;
    private final List<InputOutputFormat> coverageFormats;
    private final List<Element> interPolationMethods;

    /**
     * @param coverageTypes
     * @param coverageFormats
     * @param interPolationMethods
     */
    public CoverageAbilities( List<Pair<String, String>> coverageTypes, List<InputOutputFormat> coverageFormats,
                              List<Element> interPolationMethods ) {
                                this.coverageTypes = coverageTypes;
                                this.coverageFormats = coverageFormats;
                                this.interPolationMethods = interPolationMethods;
    }

    /**
     * @return the coverageTypes a list of &lt;value,codeType &gt; pairs.
     */
    public final List<Pair<String, String>> getCoverageTypes() {
        return coverageTypes;
    }

    /**
     * @return the coverageFormats.
     */
    public final List<InputOutputFormat> getCoverageFormats() {
        return coverageFormats;
    }

    /**
     * @return the interPolationMethods.
     */
    public final List<Element> getInterPolationMethods() {
        return interPolationMethods;
    }

}
