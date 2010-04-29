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
package org.deegree.client.mdeditor.configuration;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class Utils {
    
    public static String beginLowerCaseId( String id ) {
        char c = Character.toLowerCase( id.charAt( 0 ) );
        return c + id.substring( 1 );
    }

    public static String beginUpperCaseId( String id ) {
        char c = Character.toUpperCase( id.charAt( 0 ) );
        return c + id.substring( 1 );
    }

    public static String createBeanName( String groupId ) {
        char c = Character.toUpperCase( groupId.charAt( 0 ) );
        return c + groupId.substring( 1 ) + "Bean";
    }

    public static String getEL( String groupId, String prop ) {
        return "#{" + beginLowerCaseId( createBeanName( groupId ) ) + "." + prop + "}";
    }

    public static String getELNot( String groupId, String prop ) {
        return "#{!" + beginLowerCaseId( createBeanName( groupId ) ) + "." + prop + "}";
    }

    public static String getVisibilityProp( String id ) {
        return beginLowerCaseId( id ) + "Visibility";
    }

    public static String getVisibilityMethode( String id ) {
        return "is" + beginUpperCaseId( id ) + "Visibility";
    }

    public static String getRequiredProp( String id ) {
        return beginLowerCaseId( id ) + "Required";
    }

    public static String getRequiredMethode( String id ) {
        return "is" + beginUpperCaseId( id ) + "Required";
    }

    public static String getHelpProp( String id ) {
        return beginLowerCaseId( id ) + "Help";
    }

    public static String getHelpMethode( String id ) {
        return "is" + beginUpperCaseId( id ) + "Help";
    }

    public static String getJavaPropertyName( String id ) {
        return beginUpperCaseId( id );
    }
}
