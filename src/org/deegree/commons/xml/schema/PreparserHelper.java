//$Id$
/* --------------------------------------------------------------------------
 * Copyright 2009 by:
 *
 * Markus Schneider
 * Heerstraße 10
 * 53111 Bonn
 * Germany
 * 
 * e-Mail: markus@beefcafe.de
 * --------------------------------------------------------------------------*/
package org.deegree.commons.xml.schema;

import java.io.IOException;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xs.XSModel;
import org.deegree.commons.xml.XMLProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * TODO comment me
 * 
 * @author <a href="mailto:markus@beefcafe.de">Markus Schneider</a>
 * 
 * @version $Revision$
 */
public class PreparserHelper {

    private static final Logger LOG = LoggerFactory.getLogger( PreparserHelper.class );

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
    public static final int BIG_PRIME = 2039;

    // default settings

    /** Default Schema full checking support (false). */
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

    /** Default honour all schema locations (false). */
    protected static final boolean DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS = false;

    public static void main( String[] args )
                            throws XNIException, IOException {

        XMLEntityResolver resolver = new MyEntityResolver();
        SymbolTable sym = new SymbolTable( BIG_PRIME );
        XMLGrammarPreparser preparser = new XMLGrammarPreparser( sym );
        XMLGrammarPool grammarPool = new MyGrammarPool();
        preparser.registerPreparser( XMLGrammarDescription.XML_SCHEMA, null );

        preparser.setProperty( GRAMMAR_POOL, grammarPool );
        preparser.setEntityResolver( resolver );
        preparser.setFeature( NAMESPACES_FEATURE_ID, true );
        preparser.setFeature( VALIDATION_FEATURE_ID, true );
        preparser.setFeature( SCHEMA_VALIDATION_FEATURE_ID, true );
        preparser.setFeature( SCHEMA_FULL_CHECKING_FEATURE_ID, true );
        preparser.setFeature( HONOUR_ALL_SCHEMA_LOCATIONS_ID, true );
        System.out.println( "A: " + grammarPool );
        preparser.preparseGrammar( XMLGrammarDescription.XML_SCHEMA,
                                   new XMLInputSource( null, "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd", null ) );
        System.out.println( "B: " + grammarPool );
        grammarPool.lockPool();

        XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
        DOMConfiguration config = schemaLoader.getConfig();
        // create and register DOMErrorHandler
        DOMErrorHandler errorHandler = new DOMErrorHandler() {
            @SuppressWarnings("synthetic-access")
            public boolean handleError( DOMError domError ) {
                switch ( domError.getSeverity() ) {
                case DOMError.SEVERITY_WARNING: {
                    LOG.debug( "DOM warning: " + domError.getMessage() );
                    break;
                }
                case DOMError.SEVERITY_ERROR:
                case DOMError.SEVERITY_FATAL_ERROR: {
                    String msg = "Severe error in schema document (line: " + domError.getLocation().getLineNumber()
                                 + ", column: " + domError.getLocation().getColumnNumber() + ") "
                                 + domError.getMessage();
                    throw new XMLProcessingException( msg );
                }
                }
                return false;
            }
        };
        schemaLoader.setEntityResolver( resolver );
        schemaLoader.setProperty( XMLSchemaLoader.XMLGRAMMAR_POOL, grammarPool );
        config.setParameter( "error-handler", errorHandler );
        // set validation feature
        config.setParameter( "validate", Boolean.TRUE );

        long before = System.currentTimeMillis();
        XSModel xmlSchema = schemaLoader.loadURI( "file:/home/markus/workspace/d3_commons/test/org/deegree/feature/gml/schema/Philosopher_typesafe.xsd" );
        System.out.println( xmlSchema.getNamespaces().item( 0 ) );
        long elapsed = System.currentTimeMillis() - before;
        System.out.println( elapsed + " ms" );

        System.out.println( schemaLoader.getProperty( XMLSchemaLoader.XMLGRAMMAR_POOL ) );
    }
}

class MyGrammarPool extends XMLGrammarPoolImpl {

    public Grammar[] retrieveInitialGrammarSet( String grammarType ) {
        System.out.println( "retrieveInitialGrammarSet" );
        return new Grammar[0];
    }

    @Override
    public Grammar getGrammar( XMLGrammarDescription desc ) {
        System.out.println( "HEY, get me: " + desc );
        return super.getGrammar( desc );
    }

    @Override
    public Grammar retrieveGrammar( XMLGrammarDescription desc ) {
        System.out.println( "HEY, retrieve me: " + desc );
        return super.retrieveGrammar( desc );
    }

    public String toString() {
        String s = "";
        for ( XMLGrammarPoolImpl.Entry entry : fGrammars ) {
            if ( entry != null ) {
                s += "- " + entry.desc + "\n";
            }
        }
        return s;
    }
}

class MyEntityResolver implements XMLEntityResolver {

    @Override
    public XMLInputSource resolveEntity( XMLResourceIdentifier identifier )
                            throws XNIException, IOException {
        System.out.println( "Resolving: " + identifier );
        return new XMLInputSource( identifier );
    }

}
