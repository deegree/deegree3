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
package org.deegree.tools.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: buesching $
 * 
 * @version $Revision: 1.1 $, $Date: 2011-03-02 12:59:21 $
 */
public class Table {

    private String select;

    private String name;

    private List<Table> tables = new ArrayList<Table>();

    private List<String> variables = new ArrayList<String>();

    private List<Pair<String, ICRS>> geometryColumns = new ArrayList<Pair<String, ICRS>>();

    /**
     * 
     * @param name
     * @param select
     * @throws UnknownCRSException
     */
    Table( String name, String select, List<Pair<String, String>> geometryColumns ) throws UnknownCRSException {
        this.name = name;
        this.select = select.trim();

        String[] tmp = StringUtils.split( select, " " );
        for ( String value : tmp ) {
            if ( value.startsWith( "$" ) ) {
                variables.add( value );
            }
        }
        for ( Pair<String, String> pair : geometryColumns ) {
            ICRS crs = CRSManager.getCRSRef( pair.second );
            Pair<String, ICRS> p = new Pair<String, ICRS>( pair.first, crs );
            this.geometryColumns.add( p );
        }
    }

    /**
     * @return the select
     */
    public String getSelect() {
        return select;
    }

    /**
     * @param select
     *            the select to set
     */
    public void setSelect( String select ) {
        this.select = select;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return the tables
     */
    public List<Table> getTables() {
        return tables;
    }

    /**
     * @return the variables
     */
    public List<String> getVariables() {
        return variables;
    }

    /**
     * @return
     */
    public Pair<String, ICRS> getGeometryColumn( String field ) {
        for ( Pair<String, ICRS> p : geometryColumns ) {
            if ( p.first.equals( field ) ) {
                return p;
            }
        }
        return null;
    }
}
