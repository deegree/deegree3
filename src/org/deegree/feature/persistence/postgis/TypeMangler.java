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
package org.deegree.feature.persistence.postgis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

import org.deegree.commons.types.datetime.Date;
import org.deegree.feature.Property;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBWriter;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.Predicate;

import com.vividsolutions.jts.io.ParseException;

/**
 * Converts between types used by the feature model and PostGIS data types.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TypeMangler {

    /**
     * Converts the given {@link Property} value to an object suitable for PostgreSQL/PostGIS.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    public static Object toPostGIS( Object value )
                            throws SQLException {
        Object pgObject = null;
        if ( value instanceof Geometry ) {
            try {
                pgObject = WKBWriter.write( (Geometry) value );
            } catch ( ParseException e ) {
                throw new SQLException( e.getMessage(), e );
            }
        } else if ( value instanceof Date ) {
            pgObject = new java.sql.Date( ( (Date) value ).getDate().getTime() );
        } else if ( value instanceof BigInteger ) {
            pgObject = Integer.parseInt( value.toString() );
        } else if ( value instanceof BigDecimal ) {
            pgObject = ( (BigDecimal) value ).doubleValue();
        } else {
            pgObject = value;
        }
        return pgObject;
    }

    // static Object getDeegreeType (Object postGISType) {
    //        
    // }
    public static void main( String[] args )
                            throws JaxenException {

        String xPath = "app:placeOfDeath[3]/app:Place/app:country/app:Country";
        NamespaceContext nsContext = null;

        BaseXPath jaxenXpath = new BaseXPath( xPath, null );
        jaxenXpath.setNamespaceContext( nsContext );
        Expr rootExpr = jaxenXpath.getRootExpr();
        System.out.println( rootExpr );
        if ( rootExpr instanceof LocationPath ) {
            LocationPath lp = (LocationPath) rootExpr;
            for ( Object step : lp.getSteps() ) {
                NameStep ns = (NameStep) step;
                List predicates = ns.getPredicates();
                for ( Object predicate : predicates ) {
                    Predicate pred =  (Predicate) predicate;
                    System.out.println (pred.getExpr().getClass().getName());
                }
                System.out.println ("local name: " + ns.getLocalName());
                System.out.println ("prefix: " + ns.getPrefix());
                break;
            }
        }
    }
}
