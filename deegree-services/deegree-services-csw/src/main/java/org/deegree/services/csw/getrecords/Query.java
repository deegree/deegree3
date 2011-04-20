//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.services.csw.getrecords;

import javax.xml.namespace.QName;

import org.deegree.filter.Filter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class Query {

    private ReturnableElement elementSetName = null;

    private String[] elementName = null;

    private Filter constraint = null;

    private ConstraintLanguage constraintLanguage = null;

    private SortProperty[] sortProps = null;

    private QName[] typeNames = null;

    public Query( ReturnableElement elementSetName, String[] elementName, Filter constraint,
                  ConstraintLanguage constraintLanguage, SortProperty[] sortProps, QName[] typeNames ) {
        this.elementSetName = elementSetName;
        this.elementName = elementName;
        this.constraint = constraint;
        this.constraintLanguage = constraintLanguage;
        this.sortProps = sortProps;
        this.typeNames = typeNames;
    }

    public ReturnableElement getElementSetName() {
        return elementSetName;
    }

    public String[] getElementName() {
        if ( elementName == null )
            return new String[0];
        return elementName;
    }

    public Filter getConstraint() {
        return constraint;
    }

    public ConstraintLanguage getConstraintLanguage() {
        return constraintLanguage;
    }

    public SortProperty[] getSortProps() {
        if ( sortProps == null )
            return new SortProperty[0];
        return sortProps;
    }

    public QName[] getTypeNames() {
        if ( typeNames == null )
            return new QName[0];
        return typeNames;
    }
}
