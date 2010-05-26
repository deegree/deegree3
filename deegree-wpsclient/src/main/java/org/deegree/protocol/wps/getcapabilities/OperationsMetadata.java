//$HeadURL: https://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.wps.getcapabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * TODO add class documentation here
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle $
 * 
 * @version $Revision: $, $Date: $
 */
public class OperationsMetadata {

    private Map<String, Operation> operations = new HashMap<String, Operation>();

    private String parameter;

    private String constraint;

    private String extendedCapabilities;

    private String metaData;

    public Map<String, Operation> getOperations() {
        // TODO parse operations from ows:OperationsMetadata element
        return operations;
    }

    public void setOperation( Map<String, Operation> operations ) {
        this.operations = operations;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter( String parameter ) {
        this.parameter = parameter;
    }

    public String getConstraint() {
        return constraint;
    }

    public void setConstraint( String constraint ) {
        this.constraint = constraint;
    }

    public String getExtendedCapabilities() {
        return extendedCapabilities;
    }

    public void setExtendedCapabilities( String extendedCapabilities ) {
        this.extendedCapabilities = extendedCapabilities;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData( String metaData ) {
        this.metaData = metaData;
    }

    public Operation getOperationByName( String name ) {
        Operation op = null;
        if ( operations.containsKey( name ) ) {
            op = operations.get( name );

        }
        return op;
    }

}
