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
package org.deegree.tools.security;

import static java.util.Arrays.asList;
import static org.deegree.framework.util.CollectionUtils.containsAllEqual;
import static org.deegree.framework.util.CollectionUtils.containsEqual;
import static org.deegree.framework.util.JavaUtils.generateToString;
import static org.deegree.framework.xml.XMLTools.getNodesAsQualifiedNames;
import static org.deegree.framework.xml.XMLTools.getNodesAsStringList;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.xml.sax.SAXException;

/**
 * <code>SecDBCleaner</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SecDBCleaner {

    private static final NamespaceContext nsContext = getNamespaceContext();

    private Connection conn;

    private Statement stmt;

    /**
     * @param jdbc
     * @param user
     * @param pass
     */
    public SecDBCleaner( String jdbc, String user, String pass ) {
        try {
            Class.forName( "org.postgresql.Driver" );
        } catch ( ClassNotFoundException e ) {
            System.out.println( "PostgreSQL driver could not be loaded. Make sure it's on the classpath." );
        }
        try {
            Class.forName( "oracle.jdbc.driver.OracleDriver" );
        } catch ( ClassNotFoundException e ) {
            System.out.println( "Oracle driver could not be loaded. Make sure it's on the classpath." );
        }
        try {
            this.conn = DriverManager.getConnection( jdbc, user, pass );
            this.stmt = conn.createStatement();
        } catch ( SQLException e ) {
            System.out.println( "Database access failed: " + e.getLocalizedMessage() );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        }
    }

    private void checkLayers( String originalService ) {
        String service = originalService;
        if ( service.indexOf( "?" ) == -1 ) {
            service = service + "?request=capabilities&service=WMS&version=1.1.1";
        }
        try {
            stmt.execute( "select id from sec_secured_object_types where name = 'Layer'" );
            ResultSet set = stmt.getResultSet();
            int id = 1;
            while ( set.next() ) {
                id = set.getInt( "id" );
            }
            set.close();

            URL url = new URL( service );
            XMLFragment doc = new XMLFragment( url );
            HashSet<String> layers = new HashSet<String>( getNodesAsStringList( doc.getRootElement(), "//Layer/Name",
                                                                                nsContext ) );

            stmt.execute( "select sec_securable_objects.id as id,sec_securable_objects.name as name from sec_secured_objects,sec_securable_objects where sec_secured_objects.fk_secured_object_types = "
                          + id + " and sec_secured_objects.id = sec_securable_objects.id" );
            set = stmt.getResultSet();
            while ( set.next() ) {
                String lname = set.getString( "name" );
                int lid = set.getInt( "id" );
                if ( !layers.contains( lname )
                     && !( lname.indexOf( ":" ) != -1 && layers.contains( lname.substring( lname.lastIndexOf( "]" ) + 2 ) ) ) ) {
                    System.out.println( "The layer with name '" + lname + "' and id " + lid
                                        + " was not found in the WMS." );
                }
            }
            set.close();

        } catch ( SQLException e ) {
            System.out.println( "Database access failed: " + e.getLocalizedMessage() );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        } catch ( MalformedURLException e ) {
            System.out.println( "The service URL '" + service + "' was not valid." );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        } catch ( IOException e ) {
            System.out.println( "The service URL '" + service + "' was not valid." );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        } catch ( SAXException e ) {
            System.out.println( "The service URL '" + service + "' was not valid." );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        } catch ( XMLParsingException e ) {
            System.out.println( "The service URL '" + service + "' was not valid." );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        }
    }

    private void checkFeaturetypes( String originalService ) {
        String service = originalService;
        if ( service.indexOf( "?" ) == -1 ) {
            service = service + "?request=GetCapabilities&service=WFS&version=1.1.0";
        }
        try {
            stmt.execute( "select id from sec_secured_object_types where name = 'Featuretype'" );
            ResultSet set = stmt.getResultSet();
            int id = 2;
            while ( set.next() ) {
                id = set.getInt( "id" );
            }
            set.close();

            URL url = new URL( service );
            XMLFragment doc = new XMLFragment( url );

            QualifiedName[] qnames = getNodesAsQualifiedNames( doc.getRootElement(), "//wfs:FeatureType/wfs:Name",
                                                               nsContext );
            HashSet<QualifiedName> types = new HashSet<QualifiedName>( asList( qnames ) );

            stmt.execute( "select sec_securable_objects.id as id,sec_securable_objects.name as name from sec_secured_objects,sec_securable_objects where sec_secured_objects.fk_secured_object_types = "
                          + id + " and sec_secured_objects.id = sec_securable_objects.id" );
            set = stmt.getResultSet();
            while ( set.next() ) {
                String name = set.getString( "name" );
                QualifiedName fname = new QualifiedName( name );
                QualifiedName fname2 = new QualifiedName( name.substring( name.lastIndexOf( "]" ) + 2 ) );
                int fid = set.getInt( "id" );
                if ( !containsEqual( types, fname ) && !containsEqual( types, fname2 ) ) {
                    System.out.println( "The feature type with name '" + name + "' and id " + fid
                                        + " was not found in the WFS." );
                }
            }
            set.close();
        } catch ( SQLException e ) {
            System.out.println( "Database access failed: " + e.getLocalizedMessage() );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        } catch ( MalformedURLException e ) {
            System.out.println( "The service URL '" + service + "' was not valid." );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        } catch ( IOException e ) {
            System.out.println( "The service URL '" + service + "' was not valid." );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        } catch ( SAXException e ) {
            System.out.println( "The service URL '" + service + "' was not valid." );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        } catch ( XMLParsingException e ) {
            System.out.println( "The service URL '" + service + "' was not valid." );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        }
    }

    private void findDuplicateRoles() {
        try {
            stmt.execute( "select id from sec_roles" );
            ResultSet set = stmt.getResultSet();

            HashSet<Role> roles = new HashSet<Role>();
            while ( set.next() ) {
                Role r = new Role();
                r.id = set.getInt( "id" );
                roles.add( r );
            }
            set.close();

            for ( Role r : roles ) {
                stmt.execute( "select name, title from sec_securable_objects where id = " + r.id );
                set = stmt.getResultSet();

                while ( set.next() ) {
                    r.name = set.getString( "name" );
                    r.title = set.getString( "title" );
                }

                set.close();
            }

            for ( Role role : roles ) {
                stmt.execute( "select fk_privileges,constraints from sec_jt_roles_privileges where fk_roles = "
                              + role.id );
                set = stmt.getResultSet();

                while ( set.next() ) {
                    Privilege p = new Privilege();
                    p.id = set.getInt( "fk_privileges" );
                    p.constraint = set.getString( "constraints" );
                    role.privileges.add( p );
                }

                set.close();

                stmt.execute( "select fk_securable_objects,fk_rights,constraints from sec_jt_roles_secobjects where fk_roles = "
                              + role.id );
                set = stmt.getResultSet();

                while ( set.next() ) {
                    Right r = new Right();
                    r.id = set.getInt( "fk_securable_objects" );
                    r.right = set.getInt( "fk_rights" );
                    r.constraint = set.getString( "constraints" );
                    role.rights.add( r );
                }

                set.close();
            }

            HashSet<Role> newRoles = new HashSet<Role>();

            for ( Role r : roles ) {
                if ( containsEqual( newRoles, r ) ) {
                    inner: for ( Role o : newRoles ) {
                        if ( o.equals( r ) ) {
                            System.out.println( "Role with id " + r.id + " and name '" + r.name
                                                + "' is a duplicate of " + o.id + " with name '" + o.name + "'." );
                            break inner;
                        }
                    }
                } else {
                    newRoles.add( r );
                }
            }

        } catch ( SQLException e ) {
            System.out.println( "Database access failed: " + e.getLocalizedMessage() );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        }
    }

    static class Role {
        int id;

        String name, title;

        HashSet<Privilege> privileges = new HashSet<Privilege>();

        HashSet<Right> rights = new HashSet<Right>();

        @Override
        public boolean equals( Object o ) {
            if ( !( o instanceof Role ) ) {
                return false;
            }
            Role r = (Role) o;
            return containsAllEqual( privileges, r.privileges ) && containsAllEqual( r.privileges, privileges )
                   && containsAllEqual( rights, r.rights ) && containsAllEqual( r.rights, rights );
        }

        /**
         * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
         * distribution and is relatively fast. It is created from field <b>f</b> as follows:
         * <ul>
         * <li>boolean -- code = (f ? 0 : 1)</li>
         * <li>byte, char, short, int -- code = (int)f</li>
         * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
         * <li>float -- code = Float.floatToIntBits(f);</li>
         * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
         * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
         * <li>Array -- Apply above rules to each element</li>
         * </ul>
         * <p>
         * Combining the hash code(s) computed above: result = 37 * result + code;
         * </p>
         * 
         * @return (int) ( result >>> 32 ) ^ (int) result;
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            // the 2nd millionth prime, :-)
            long code = 32452843;
            code = id * 37 + code;
            for ( Privilege p : privileges ) {
                code = p.hashCode() * 37 + code;
            }
            for ( Right r : rights ) {
                code = r.hashCode() * 37 + code;
            }
            return (int) ( code >>> 32 ) ^ (int) code;
        }

        @Override
        public String toString() {
            return generateToString( this );
        }
    }

    static class Privilege {
        int id;

        String constraint;

        @Override
        public boolean equals( Object o ) {
            if ( !( o instanceof Privilege ) ) {
                return false;
            }
            Privilege p = (Privilege) o;
            return id == p.id
                   && ( ( constraint == p.constraint ) || ( constraint != null && constraint.equals( p.constraint ) ) );
        }

        /**
         * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
         * distribution and is relatively fast. It is created from field <b>f</b> as follows:
         * <ul>
         * <li>boolean -- code = (f ? 0 : 1)</li>
         * <li>byte, char, short, int -- code = (int)f</li>
         * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
         * <li>float -- code = Float.floatToIntBits(f);</li>
         * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
         * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
         * <li>Array -- Apply above rules to each element</li>
         * </ul>
         * <p>
         * Combining the hash code(s) computed above: result = 37 * result + code;
         * </p>
         * 
         * @return (int) ( result >>> 32 ) ^ (int) result;
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            // the 2nd millionth prime, :-)
            long code = 32452843;
            code = id * 37 + code;
            if ( constraint != null ) {
                code = constraint.hashCode() * 37 + code;
            }
            return (int) ( code >>> 32 ) ^ (int) code;
        }

        @Override
        public String toString() {
            return generateToString( this );
        }
    }

    static class Right {
        int id;

        int right;

        String constraint;

        @Override
        public boolean equals( Object o ) {
            if ( !( o instanceof Right ) ) {
                return false;
            }
            Right r = (Right) o;
            return r.id == id && r.right == right
                   && ( ( constraint == r.constraint ) || ( constraint != null && constraint.equals( r.constraint ) ) );
        }

        /**
         * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
         * distribution and is relatively fast. It is created from field <b>f</b> as follows:
         * <ul>
         * <li>boolean -- code = (f ? 0 : 1)</li>
         * <li>byte, char, short, int -- code = (int)f</li>
         * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
         * <li>float -- code = Float.floatToIntBits(f);</li>
         * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
         * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
         * <li>Array -- Apply above rules to each element</li>
         * </ul>
         * <p>
         * Combining the hash code(s) computed above: result = 37 * result + code;
         * </p>
         * 
         * @return (int) ( result >>> 32 ) ^ (int) result;
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            // the 2nd millionth prime, :-)
            long code = 32452843;
            code = id * 37 + code;
            code = right * 37 + code;
            if ( constraint != null ) {
                code = constraint.hashCode() * 37 + code;
            }
            return (int) ( code >>> 32 ) ^ (int) code;
        }

        @Override
        public String toString() {
            return generateToString( this );
        }
    }

    private static void printUsage() {
        System.out.println( "Usage:" );
        System.out.println( "java -cp deegree2.jar org.deegree.tools.security.SecDBCleaner <command> <options>" );
        System.out.println();
        System.out.println( "Commands:" );
        System.out.println( "  checkRoles         - checks for duplicate roles" );
        System.out.println( "  checkLayers        - checks for existance of layers" );
        System.out.println( "  checkFeaturetypes  - checks for existance of feature types" );
        System.out.println();
        System.out.println( "Options:" );
        System.out.println( "  -d <jdbc URL>      -  database connection URL" );
        System.out.println( "  -u <DB username>   -  database user name" );
        System.out.println( "  -p <DB password>   -  database password" );
        System.out.println( "  -s <service URL>   -  service URL against which to check for layers/feature types (only for checkLayers/checkFeaturetypes)" );
        System.out.println();
        System.out.println( "Commands are case insensitive." );
        System.exit( 0 );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {

        if ( args.length == 0 ) {
            printUsage();
        }

        String url = null, user = null, pass = "", service = null;

        int pos = 1;
        while ( args.length > pos ) {
            if ( args[pos].equals( "-d" ) ) {
                ++pos;
                if ( args.length > pos ) {
                    url = args[pos];
                }
                ++pos;
            } else if ( args[pos].equals( "-u" ) ) {
                ++pos;
                if ( args.length > pos ) {
                    user = args[pos];
                }
                ++pos;
            } else if ( args[pos].equals( "-p" ) ) {
                ++pos;
                if ( args.length > pos ) {
                    pass = args[pos];
                }
                ++pos;
            } else if ( args[pos].equals( "-s" ) ) {
                ++pos;
                if ( args.length > pos ) {
                    service = args[pos];
                }
                ++pos;
            } else {
                System.out.println( "Ignoring unknown parameter '" + args[pos++] + "'." );
            }
        }

        if ( url == null ) {
            System.out.println( "Database URL must be given. PostGIS example: jdbc:postgresql://localhost/security" );
            printUsage();
        }
        if ( user == null ) {
            System.out.println( "Database user must be given." );
            printUsage();
        }
        if ( service == null
             && ( args[0].equalsIgnoreCase( "checklayers" ) || args[0].equalsIgnoreCase( "checkfeaturetypes" ) ) ) {
            System.out.println( "Service must be given for checkLayers & checkFeaturetypes." );
            printUsage();
        }

        SecDBCleaner cleaner = new SecDBCleaner( url, user, pass );
        if ( args[0].equalsIgnoreCase( "checkroles" ) ) {
            cleaner.findDuplicateRoles();
        } else if ( args[0].equalsIgnoreCase( "checklayers" ) ) {
            cleaner.checkLayers( service );
        } else if ( args[0].equalsIgnoreCase( "checkfeaturetypes" ) ) {
            cleaner.checkFeaturetypes( service );
        } else {
            System.out.println( "Unknown command '" + args[0] + "'" );
            printUsage();
        }

        try {
            cleaner.stmt.close();
            cleaner.conn.close();
        } catch ( SQLException e ) {
            System.out.println( "Database access failed: " + e.getLocalizedMessage() );
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        }
    }

}
