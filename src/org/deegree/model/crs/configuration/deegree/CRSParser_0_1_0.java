//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.crs.configuration.deegree;

import java.util.Properties;

import org.deegree.commons.xml.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * The <code>CRSParser_0_1_0</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class CRSParser_0_1_0 extends CRSParser {

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
    public CRSParser_0_1_0( DeegreeCRSProvider provider, Properties properties, Element rootElement ) {
        super( provider, rootElement );
    }

    /**
     * @param provider
     * @param properties
     */
    public CRSParser_0_1_0( DeegreeCRSProvider provider, Properties properties ) {
        super( provider, properties, "definitions", CommonNamespaces.CRSNS );
        // TODO Auto-generated constructor stub
    }
}
