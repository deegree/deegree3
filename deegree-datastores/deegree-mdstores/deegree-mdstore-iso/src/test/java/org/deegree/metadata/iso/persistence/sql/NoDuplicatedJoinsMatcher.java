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
package org.deegree.metadata.iso.persistence.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Chaecks if a sql snippet does not contain duplicated joins
 * 
 * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class NoDuplicatedJoinsMatcher extends BaseMatcher<String> {

    @Override
    public void describeTo( Description description ) {
        description.appendText( "sql snippet does not contain duplicated joins" );
    }

    @Override
    public boolean matches( Object item ) {
        String sql = removeOtherThanJoins( (String) item );
        List<String> joins = parseJoins( sql );
        for ( String join : joins ) {
            for ( String joinInner : joins ) {
                if ( joinsAreNotTheSame( joins, join, joinInner ) ) {
                    if ( joinsAreEqual( join, joinInner ) )
                        return false;
                }
            }
        }

        return true;
    }

    private boolean joinsAreNotTheSame( List<String> joins, String join, String joinInner ) {
        return joins.indexOf( join ) != joins.indexOf( joinInner );
    }

    private List<String> parseJoins( String sql ) {
        List<String> joins = new ArrayList<String>();
        String[] split = sql.split( "LEFT OUTER JOIN" );
        for ( String join : split ) {
            if ( join != null && join.trim().length() > 0 ) {
                joins.add( "LEFT OUTER JOIN " + join.trim() );
            }
        }
        return joins;
    }

    private String removeOtherThanJoins( String sql ) {
        sql = removeBeginToFirstJoin( sql );
        sql = removeFromLastJoin( sql );
        return sql;
    }

    private String removeFromLastJoin( String sql ) {
        int whereIndex = sql.indexOf( "WHERE" );
        return sql.substring( 0, whereIndex );
    }

    private String removeBeginToFirstJoin( String sql ) {
        int joinIndex = sql.indexOf( "LEFT OUTER JOIN" );
        return sql.substring( joinIndex );
    }

    private boolean joinsAreEqual( String join, String joinInner ) {
        String regex = createRegexFromJoin( join );
        return Pattern.matches( regex, joinInner );
    }

    private String createRegexFromJoin( String join ) {
        String regex = join.replaceAll( "\\sX\\d*\\s", " X\\\\d " );
        regex = regex.replaceAll( "=X\\d*\\.", "=X\\\\d\\\\." );
        return regex;
    }

}