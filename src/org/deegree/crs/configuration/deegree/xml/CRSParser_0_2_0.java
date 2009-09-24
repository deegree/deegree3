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

package org.deegree.crs.configuration.deegree.xml;


import java.util.Properties;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.CommonNamespaces;

/**
 * The <code>CRSParser_0_2_0</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CRSParser_0_2_0 extends CRSParser {

    /**
     *
     */
    private static final long serialVersionUID = 5619333696132594126L;

    /**
     * Another constructor, which is used for the automatic loading of the crs definitions file 0_1_0.
     *
     * @param provider
     *            to be used for callback.
     * @param properties
     * @param rootElement
     *            to be used as configuration backend.
     */
    public CRSParser_0_2_0( DeegreeCRSProvider provider, Properties properties, OMElement rootElement ) {
        super( provider, rootElement );
    }

    /**
     * @param provider
     * @param properties
     */
    public CRSParser_0_2_0( DeegreeCRSProvider provider, Properties properties ) {
        super( provider, properties, "definitions", CommonNamespaces.CRSNS );
        // TODO Auto-generated constructor stub
    }

}
