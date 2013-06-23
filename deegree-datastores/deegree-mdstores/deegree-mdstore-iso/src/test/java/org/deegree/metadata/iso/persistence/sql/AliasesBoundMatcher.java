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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Checks the binding of the aliases in a sql snippet
 * 
 * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class AliasesBoundMatcher extends BaseMatcher<String> {

    @Override
    public void describeTo( Description description ) {
        description.appendText( "SQL snippet contains only bounded aliases!" );
    }

    @Override
    public boolean matches( Object item ) {
        String sql = (String) item;
        List<String> availableAliases = extractAvailableAliases( sql );
        List<String> usedAliases = extractUsedAliases( sql );
        return allUsedAliasesAreAvailable( availableAliases, usedAliases );
    }

    /**
     * <pre>
     * FROM idxtb_main X1 
     *   LEFT OUTER JOIN idxtb_operatesondata X2 ON X1.id=X2.fk_main 
     *   WHERE (X1.fileidentifier = ? OR X1.parentid = ?
     * </pre>
     * 
     * => extracts X1 and X2
     **/
    private List<String> extractAvailableAliases( String sql ) {
        String regex = "\\sX\\d ";
        return findAll( sql, regex );
    }

    /**
     * <pre>
     * FROM idxtb_main X1 
     *   LEFT OUTER JOIN idxtb_operatesondata X2 ON X1.id=X2.fk_main 
     *   WHERE (X1.fileidentifier = ? OR X1.parentid = ? OR X3.parentid = ?)
     * </pre>
     * 
     * => extracts X1, X2, X3
     **/
    private List<String> extractUsedAliases( String sql ) {
        String regex = "\\sX\\d\\.";
        List<String> matchingAliases = findAll( sql, regex );
        return removeTrailingDots( matchingAliases );
    }

    protected List<String> removeTrailingDots( List<String> matchingAliases ) {
        List<String> normalised = new ArrayList<String>();
        for ( String matchingAlias : matchingAliases ) {
            normalised.add( matchingAlias.replace( ".", "" ) );
        }
        return normalised;
    }

    private boolean allUsedAliasesAreAvailable( List<String> availableAliases, List<String> usedAliases ) {
        return availableAliases.containsAll( usedAliases );
    }

    protected List<String> findAll( String sql, String regex ) {
        List<String> aliases = new ArrayList<String>();
        Matcher matcher = Pattern.compile( regex ).matcher( sql );
        while ( matcher.find() )
            aliases.add( matcher.group().trim() );
        return aliases;
    }
}