/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.galdosinc.glib.gml.schema;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.deegree.commons.xml.schema.RedirectingEntityResolver;

public final class GrammarUtil {

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    public static XMLGrammarPreparser getGrammarParser() {
        XMLGrammarPreparser grammerParser = new XMLGrammarPreparser();
        grammerParser.setFeature( NAMESPACES_FEATURE_ID, true );
        grammerParser.setFeature( VALIDATION_FEATURE_ID, true );
        grammerParser.setFeature( SCHEMA_VALIDATION_FEATURE_ID, true );
        grammerParser.setFeature( SCHEMA_FULL_CHECKING_FEATURE_ID, true );
        grammerParser.setFeature( "http://apache.org/xml/features/honour-all-schemaLocations", false );
        grammerParser.registerPreparser( "http://www.w3.org/2001/XMLSchema", null );
        grammerParser.registerPreparser( "http://www.w3.org/TR/REC-xml", null );
        return grammerParser;
    }

    public static Grammar parseGrammar( URL grammarUrl )
                            throws IOException {
        return parseGrammar( grammarUrl, null );
    }

    public static Grammar parseGrammar( URL grammarUrl, XMLGrammarPool grammarPool )
                            throws IOException {
        return parseGrammar( grammarUrl, grammarPool, null );
    }

    public static Grammar parseGrammar( URL grammarUrl, XMLGrammarPool grammarPool, XMLErrorHandler errorHandler )
                            throws IOException {
        return parseGrammar( grammarUrl.toExternalForm(), new InputStreamReader( grammarUrl.openStream() ),
                             grammarPool, errorHandler );
    }

    public static Grammar parseGrammar( File grammarFile )
                            throws IOException {
        return parseGrammar( grammarFile, null );
    }

    public static Grammar parseGrammar( File grammarFile, XMLGrammarPool grammarPool )
                            throws IOException {
        return parseGrammar( grammarFile, grammarPool, null );
    }

    public static Grammar parseGrammar( File grammarFile, XMLGrammarPool grammarPool, XMLErrorHandler errorHandler )
                            throws IOException {
        return parseGrammar( grammarFile.getAbsolutePath(), new FileReader( grammarFile ), grammarPool, errorHandler );
    }

    public static Grammar parseGrammar( String baseUri, String grammarText )
                            throws IOException {
        return parseGrammar( baseUri, grammarText, null );
    }

    public static Grammar parseGrammar( String baseUri, String grammarText, XMLGrammarPool grammarPool )
                            throws IOException {
        return parseGrammar( baseUri, grammarText, grammarPool );
    }

    public static Grammar parseGrammar( String baseUri, String grammarText, XMLGrammarPool grammarPool,
                                        XMLErrorHandler errorHandler )
                            throws IOException {
        return parseGrammar( baseUri, new StringReader( grammarText ), grammarPool, errorHandler );
    }

    public static Grammar parseGrammar( String baseUri, Reader in, XMLGrammarPool grammarPool,
                                        XMLErrorHandler errorHandler )
                            throws IOException {
        String encoding = null;
        XMLGrammarPreparser grammerParser = getGrammarParser();
        if ( grammarPool != null ) {
            grammerParser.setGrammarPool( grammarPool );
        }
        if ( errorHandler != null ) {
            grammerParser.setErrorHandler( errorHandler );
        }
        grammerParser.setEntityResolver( new RedirectingEntityResolver() );
        return grammerParser.preparseGrammar( "http://www.w3.org/2001/XMLSchema", new XMLInputSource( baseUri, baseUri,
                                                                                                      baseUri, in,
                                                                                                      encoding ) );
    }

}
