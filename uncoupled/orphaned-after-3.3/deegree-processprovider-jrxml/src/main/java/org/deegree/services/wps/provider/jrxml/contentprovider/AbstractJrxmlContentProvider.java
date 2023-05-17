/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wps.provider.jrxml.contentprovider;

import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsCodeType;
import static org.deegree.services.wps.provider.jrxml.JrxmlUtils.getAsLanguageStringType;

import java.math.BigInteger;
import java.util.Map;

import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.workspace.Workspace;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public abstract class AbstractJrxmlContentProvider implements JrxmlContentProvider {

    protected final Workspace workspace;

    public AbstractJrxmlContentProvider( Workspace workspace ) {
        this.workspace = workspace;
    }

    protected void addInput( ProcessletInputDefinition input, Map<String, ParameterDescription> parameterDescriptions,
                             String id, int max, int min ) {
        input.setIdentifier( getAsCodeType( id ) );
        String t = id;
        if ( parameterDescriptions.containsKey( id ) ) {
            input.setAbstract( getAsLanguageStringType( parameterDescriptions.get( id ).getDescription() ) );
            if ( parameterDescriptions.get( id ).getTitle() != null ) {
                t = parameterDescriptions.get( id ).getTitle();
            }
        }
        input.setTitle( getAsLanguageStringType( t ) );
        if ( max > -1 )
            input.setMaxOccurs( BigInteger.valueOf( max ) );
        if ( min > -1 )
            input.setMinOccurs( BigInteger.valueOf( min ) );

    }
}
