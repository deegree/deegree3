//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.tools.xml;

import static java.lang.System.out;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.xml.DOMPrinter.nodeToString;
import static org.deegree.framework.xml.XMLTools.getNode;
import static org.deegree.framework.xml.XMLTools.getNodes;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.util.Pair;
import org.deegree.framework.util.StringPair;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <code>SimpleValidator</code> is a simple xpath based "validator". It can be used to crudely
 * check XML documents for existing nodes. A sample rule file can be found right beneath in this
 * package.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SimpleValidator {

    static final NamespaceContext nsContext = getNamespaceContext();

    private static final ILogger LOG = getLogger( SimpleValidator.class );

    private LinkedList<Rule> rules;

    /**
     * Initializes from configuration URL. File should be encoded in UTF-8.
     *
     * @param config
     * @throws IOException
     */
    public SimpleValidator( URL config ) throws IOException {
        rules = new LinkedList<Rule>();

        BufferedReader in = new BufferedReader( new InputStreamReader( config.openStream() ) );

        StringBuffer buf = new StringBuffer( 65536 );
        String s;
        while ( ( s = in.readLine() ) != null ) {
            if ( s.startsWith( "#" ) ) {
                continue;
            }
            buf.append( s ).append( " " );
        }

        in.close();

        StringTokenizer tok = new StringTokenizer( buf.toString() );
        String cur = tok.nextToken();
        while ( tok.hasMoreTokens() ) {
            Pair<Rule, String> p = parseRule( tok, cur );
            cur = p.second;
            rules.add( p.first );
        }

        LOG.logDebug( "Parsed rule file with " + rules.size() + " rules." );
    }

    private static Pair<Rule, String> parseRule( StringTokenizer tok, String first )
                            throws IOException {
        String id = first;
        String s = tok.nextToken();

        if ( s.equalsIgnoreCase( "if" ) ) {
            String test = tok.nextToken();
            if ( !tok.nextToken().equalsIgnoreCase( "then" ) ) {
                throw new IOException( "Missing 'then' after " + test + "." );
            }

            Pair<Rule, String> then = parseRule( tok, tok.nextToken() );

            return new Pair<Rule, String>( new Rule( id, test, then.first ), then.second );
        }

        if ( s.equalsIgnoreCase( "oneof" ) ) {
            String base = tok.nextToken();
            List<String> choices = new LinkedList<String>();

            s = tok.nextToken();
            while ( tok.hasMoreTokens() && s.equalsIgnoreCase( "choice" ) ) {
                choices.add( tok.nextToken() );
                if ( tok.hasMoreTokens() ) {
                    s = tok.nextToken();
                }
            }

            return new Pair<Rule, String>( new Rule( id, base, choices ), s );
        }

        boolean isBoolean = false;

        if ( s.equalsIgnoreCase( "istrue" ) ) {
            s = tok.nextToken();
            // if ( !s.startsWith( "boolean(" ) ) {
            // s = "boolean(" + s + ")";
            // }
            isBoolean = true;
        }

        String next = tok.hasMoreTokens() ? tok.nextToken() : null;
        return new Pair<Rule, String>( new Rule( id, s, isBoolean ), next );
    }

    /**
     * @param n
     * @return a list of errors. A pair will include the id of the failed rule, and the context node
     *         as string (if applicable) or null (if not).
     */
    public LinkedList<StringPair> validate( Node n ) {
        LinkedList<StringPair> list = new LinkedList<StringPair>();

        for ( Rule r : rules ) {
            if ( !r.eval( n ) ) {
                list.addAll( r.errors );
            }
        }

        return list;
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        if ( args.length < 2 ) {
            out.println( "Usage:" );
            out.println( "org.deegree.tools.xml.SimpleValidator <rulesfile> <xmlfiletovalidate> [-v]" );
            return;
        }

        boolean verbose = args.length > 2 && args[2].equals( "-v" );

        try {
            XMLFragment doc = new XMLFragment( new File( args[1] ) );
            SimpleValidator val = new SimpleValidator( new File( args[0] ).toURI().toURL() );

            LinkedList<StringPair> errors = val.validate( doc.getRootElement() );
            if ( errors.size() > 0 ) {
                out.println( "Errors:" );
                for ( StringPair p : errors ) {
                    if ( verbose ) {
                        out.println( p );
                    } else {
                        out.print( p.first );
                        if ( errors.indexOf( p ) != errors.size() - 1 ) {
                            out.print( ", " );
                        }
                    }
                }
                if ( !verbose ) {
                    out.println();
                }
            } else {
                out.println( "All rules passed." );
            }
        } catch ( MalformedURLException e ) {
            out.println( "Error: one of the files is not a valid filename." );
            out.println( "Usage:" );
            out.println( "org.deegree.tools.xml.SimpleValidator <rulesfile> <xmlfiletovalidate> [-v]" );
        } catch ( IOException e ) {
            out.println( "Error: second file cannot be read." );
            out.println( "Usage:" );
            out.println( "org.deegree.tools.xml.SimpleValidator <rulesfile> <xmlfiletovalidate> [-v]" );
        } catch ( SAXException e ) {
            out.println( "Error: second file is not parsable XML." );
            out.println( "Usage:" );
            out.println( "org.deegree.tools.xml.SimpleValidator <rulesfile> <xmlfiletovalidate> [-v]" );
        }
    }

    private static class Rule {

        String id;

        List<StringPair> errors;

        private String xpath;

        private Rule then;

        private List<String> choices;

        private boolean isBoolean;

        Rule( String id, String xpath ) {
            this.xpath = xpath;
            this.id = id;
            errors = new LinkedList<StringPair>();
        }

        Rule( String id, String xpath, Rule then ) {
            this( id, xpath );
            this.then = then;
        }

        Rule( String id, String base, List<String> choices ) {
            this( id, base );
            this.choices = choices;
        }

        Rule( String id, String xpath, boolean isBoolean ) {
            this( id, xpath );
            this.isBoolean = isBoolean;
        }

        boolean eval( Node n ) {
            try {

                if ( isBoolean ) {
                    try {
                        XPath xpath = new DOMXPath( this.xpath );
                        xpath.setNamespaceContext( nsContext );
                        boolean res = xpath.booleanValueOf( n );
                        if ( !res ) {
                            errors.add( new StringPair( id, "The IsTrue expression evaluated to false." ) );
                        }
                        return res;
                    } catch ( JaxenException e ) {
                        errors.add( new StringPair( id, "The xpath expression contained an error: "
                                                        + e.getLocalizedMessage() ) );
                        return false;
                    }
                }

                if ( choices != null ) {
                    boolean isOk = false;

                    Node baseNode = getNode( n, xpath, nsContext );

                    if ( baseNode == null ) {
                        errors.add( new StringPair( id, "(node not found)" ) );
                        return false;
                    }

                    for ( String x : choices ) {
                        Node tmp = getNode( baseNode, x, nsContext );
                        isOk = isOk || tmp != null;
                    }

                    if ( !isOk ) {
                        errors.add( new StringPair( id, nodeToString( baseNode, "UTF-8" ) ) );
                    }

                    return isOk;
                }

                List<Node> tmps = getNodes( n, xpath, nsContext );

                if ( then == null ) {
                    if ( tmps.size() == 0 ) {
                        errors.add( new StringPair( id, null ) );
                    }
                    return tmps.size() != 0;
                }

                if ( tmps.size() == 0 ) {
                    return true;
                }

                boolean res = true;

                for ( Node tmp : tmps ) {
                    if ( then.eval( tmp ) ) {
                        continue;
                    }

                    res = false;

                    errors.add( new StringPair( then.id, nodeToString( tmp, "UTF-8" ) ) );
                }

                return res;
            } catch ( XMLParsingException e ) {
                return false;
            }
        }

    }

}
