//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2013 by
 IDgis bv

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

 IDgis bv
 Boomkamp 16, 7461 AX Rijssen
 The Netherlands
 http://www.idgis.nl/

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

/**
 * <code>XMLInputFactoryUtils</code>
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
package org.deegree.commons.xml.stax;

import javax.xml.stream.XMLInputFactory;

public class XMLInputFactoryUtils {

    /**
     * Creates an XMLInputFactory with unsafe features disabled. Such an XMLInputFactory is to be used for parsing
     * untrusted xml such as incoming post requests.
     * 
     * @return
     */
    public static XMLInputFactory newSafeInstance() {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        inputFactory.setProperty( XMLInputFactory.SUPPORT_DTD, false );

        return inputFactory;
    }
}
