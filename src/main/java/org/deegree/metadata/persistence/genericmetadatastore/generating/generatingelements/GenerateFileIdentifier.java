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
package org.deegree.metadata.persistence.genericmetadatastore.generating.generatingelements;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenerateFileIdentifier {

    private final OMFactory factory;

    private final OMNamespace namespaceGMD;

    private final OMNamespace namespaceGCO;

    private GenerateFileIdentifier( OMFactory factory ) {
        this.factory = factory;
        namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "gmd" );
        namespaceGCO = factory.createOMNamespace( "http://www.isotc211.org/2005/gco", "gco" );
    }

    public static GenerateFileIdentifier newInstance( OMFactory factory ) {

        return new GenerateFileIdentifier( factory );
    }

    public OMElement createFileIdentifierElement( String id ) {
        OMElement omFileIdentifier = factory.createOMElement( "fileIdentifier", namespaceGMD );
        OMElement omFileCharacterString = factory.createOMElement( "CharacterString", namespaceGCO );
        omFileIdentifier.addChild( omFileCharacterString );
        omFileCharacterString.setText( id );

        return omFileIdentifier;
    }

}
