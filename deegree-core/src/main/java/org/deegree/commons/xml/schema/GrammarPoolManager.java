//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.xml.schema;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages pre-populated <code>GrammarPool</code> instances to minimize fetching and parsing of XML schema documents
 * over the internet.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
class GrammarPoolManager {

    private static final Logger LOG = LoggerFactory.getLogger( GrammarPoolManager.class );

    /** Property identifier: symbol table. */
    public static final String SYMBOL_TABLE = Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    /** Property identifier: grammar pool. */
    public static final String GRAMMAR_POOL = Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    /** Honour all schema locations feature id (http://apache.org/xml/features/honour-all-schemaLocations). */
    protected static final String HONOUR_ALL_SCHEMA_LOCATIONS_ID = "http://apache.org/xml/features/honour-all-schemaLocations";

    // a larg(ish) prime to use for a symbol table to be shared among potentially man parsers. Start one as close to 2K
    // (20 times larger than normal) and see what happens...
    private static final int BIG_PRIME = 2039;

    private static final Map<String, GrammarPool> idToPool = new HashMap<String, GrammarPool>();

    /**
     * Returns a {@link GrammarPool} that contains the preparsed schemas from the specified URIs.
     * 
     * @param schemaUris
     * @return a pre-populated and locked <code>GrammarPool</code> object
     * @throws IOException
     * @throws XNIException
     */
    synchronized static GrammarPool getGrammarPool( String... schemaUris )
                            throws XNIException, IOException {

        String id = "";
        SortedSet<String> sortedUris = new TreeSet<String>( Arrays.asList( schemaUris ) );
        for ( String uri : sortedUris ) {
            id += ":" + uri;
        }
        LOG.debug( "Looking up grammar pool for combined URI id: '" + id + "'." );

        GrammarPool pool = idToPool.get( id );
        if ( pool == null ) {
            pool = createGrammarPool( schemaUris );
            idToPool.put( id, pool );
        }
        return pool;
    }

    private static GrammarPool createGrammarPool( String[] schemaUris )
                            throws XNIException, IOException {

        if ( LOG.isDebugEnabled() ) {
            String s = "";
            for ( int i = 0; i < schemaUris.length; i++ ) {
                s += "'" + schemaUris[i] + "'";
                if ( i != schemaUris.length - 1 ) {
                    s += ", ";
                }
            }
            LOG.debug( "Creating grammar pool for schemas: {" + s + "}." );
        }

        XMLEntityResolver resolver = new RedirectingEntityResolver();
        SymbolTable sym = new SymbolTable( BIG_PRIME );

        XMLGrammarPreparser preparser = new XMLGrammarPreparser( sym );
        GrammarPool grammarPool = new GrammarPool( sym );
        preparser.registerPreparser( XMLGrammarDescription.XML_SCHEMA, null );

        preparser.setProperty( GRAMMAR_POOL, grammarPool );
        preparser.setEntityResolver( resolver );
        preparser.setFeature( NAMESPACES_FEATURE_ID, true );
        preparser.setFeature( VALIDATION_FEATURE_ID, true );
        preparser.setFeature( SCHEMA_VALIDATION_FEATURE_ID, true );
        preparser.setFeature( SCHEMA_FULL_CHECKING_FEATURE_ID, true );
        // NOTE: don't set to true, or validation of WFS GetFeature responses will fail (Xerces error?)!
        preparser.setFeature( HONOUR_ALL_SCHEMA_LOCATIONS_ID, false );

        // populate the pool with all schemaUris
        for ( String schemaUri : schemaUris ) {
            preparser.preparseGrammar( XMLGrammarDescription.XML_SCHEMA, new XMLInputSource( null, schemaUri, null ) );
        }

        // prevent any more adds to the pool
        grammarPool.lockPool();
        return grammarPool;
    }
}
