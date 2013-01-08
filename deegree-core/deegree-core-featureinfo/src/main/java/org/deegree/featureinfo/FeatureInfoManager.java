//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.featureinfo;

import static org.deegree.featureinfo.templating.TemplatingUtils.runTemplate;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.feature.Feature;
import org.deegree.featureinfo.serializing.FeatureInfoGmlWriter;
import org.deegree.featureinfo.serializing.FeatureInfoSerializer;
import org.deegree.featureinfo.serializing.XsltFeatureInfoSerializer;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;

/**
 * Responsible for managing feature info output formats and their serializers.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureInfoManager {

    private static final Logger LOG = getLogger( FeatureInfoManager.class );

    private final HashSet<String> defaultGMLGFIFormats = new LinkedHashSet<String>();

    private final HashMap<String, FeatureInfoSerializer> featureInfoSerializers = new HashMap<String, FeatureInfoSerializer>();

    private final LinkedHashMap<String, String> supportedFeatureInfoFormats = new LinkedHashMap<String, String>();

    public FeatureInfoManager( boolean addDefaultFormats ) {
        if ( addDefaultFormats ) {
            defaultGMLGFIFormats.add( "application/gml+xml; version=2.1" );
            defaultGMLGFIFormats.add( "application/gml+xml; version=3.0" );
            defaultGMLGFIFormats.add( "application/gml+xml; version=3.1" );
            defaultGMLGFIFormats.add( "application/gml+xml; version=3.2" );
            defaultGMLGFIFormats.add( "text/xml; subtype=gml/2.1.2" );
            defaultGMLGFIFormats.add( "text/xml; subtype=gml/3.0.1" );
            defaultGMLGFIFormats.add( "text/xml; subtype=gml/3.1.1" );
            defaultGMLGFIFormats.add( "text/xml; subtype=gml/3.2.1" );
            supportedFeatureInfoFormats.put( "application/vnd.ogc.gml", "" );
            supportedFeatureInfoFormats.put( "text/xml", "" );
            supportedFeatureInfoFormats.put( "text/plain", "" );
            supportedFeatureInfoFormats.put( "text/html", "" );
        }
    }

    public void addOrReplaceFormat( String format, String file ) {
        defaultGMLGFIFormats.remove( format );
        supportedFeatureInfoFormats.put( format, file );
    }

    public void addOrReplaceXsltFormat( String format, URL xsltUrl, GMLVersion version, DeegreeWorkspace workspace ) {
        defaultGMLGFIFormats.remove( format );
        XsltFeatureInfoSerializer xslt = new XsltFeatureInfoSerializer( version, xsltUrl, workspace );
        featureInfoSerializers.put( format, xslt );
    }

    public void finalizeConfiguration() {
        for ( String f : defaultGMLGFIFormats ) {
            supportedFeatureInfoFormats.put( f, "" );
        }
    }

    public Set<String> getSupportedFormats() {
        return supportedFeatureInfoFormats.keySet();
    }

    public void serializeFeatureInfo( FeatureInfoParams params )
                            throws IOException {

        String format = params.getFormat();
        FeatureInfoSerializer serializer = featureInfoSerializers.get( format );
        if ( serializer != null ) {
            serializer.serialize( params.getNsBindings(), params.getFeatureCollection(), params.getOutputStream() );
            return;
        }

        String fiFile = supportedFeatureInfoFormats.get( format );
        if ( fiFile != null && !fiFile.isEmpty() ) {
            runTemplate( params.getOutputStream(), fiFile, params.getFeatureCollection(), params.isWithGeometries() );
        } else if ( isGml( format ) ) {
            handleGmlOutput( format, params );
        } else if ( format.equalsIgnoreCase( "text/plain" ) ) {
            handlePlainTextOutput( params );
        } else if ( format.equalsIgnoreCase( "text/html" ) ) {
            runTemplate( params.getOutputStream(), null, params.getFeatureCollection(), params.isWithGeometries() );
        } else {
            throw new IOException( "FeatureInfo format '" + format + "' is unknown." );   
        }
    }

    private boolean isGml( String format ) {
        return format.equalsIgnoreCase( "application/vnd.ogc.gml" ) || format.equalsIgnoreCase( "text/xml" )
               || defaultGMLGFIFormats.contains( format.toLowerCase() );
    }

    private void handlePlainTextOutput( FeatureInfoParams params )
                            throws UnsupportedEncodingException {
        PrintWriter out = new PrintWriter( new OutputStreamWriter( params.getOutputStream(), "UTF-8" ) );
        for ( Feature f : params.getFeatureCollection() ) {
            out.println( f.getName().getLocalPart() + ":" );
            for ( Property p : f.getProperties() ) {
                out.println( "  " + p.getName().getLocalPart() + ": " + p.getValue() );
            }
            out.println();
        }
        out.close();
    }

    private void handleGmlOutput( String format, FeatureInfoParams params ) {
        try {
            XMLStreamWriter xmlWriter = params.getXmlWriter();

            // for more than just quick 'hacky' schemaLocation attributes one should use a proper WFS
            HashMap<String, String> bindings = new HashMap<String, String>();
            String ns = determineNamespace( params );
            if ( ns != null ) {
                bindings.put( ns, params.getSchemaLocation() );
                if ( !params.getNsBindings().containsValue( ns ) ) {
                    params.getNsBindings().put( "app", ns );
                }
            }
            if ( !params.getNsBindings().containsKey( "app" ) ) {
                params.getNsBindings().put( "app", "http://www.deegree.org/app" );
            }
            bindings.put( "http://www.opengis.net/wfs", "http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd" );

            GMLVersion gmlVersion = getGmlVersion( format );

            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlWriter );
            gmlWriter.setOutputCrs( params.getCrs() );
            gmlWriter.setNamespaceBindings( params.getNsBindings() );
            gmlWriter.setExportGeometries( params.isWithGeometries() );
            new FeatureInfoGmlWriter( gmlVersion ).export( params.getFeatureCollection(), gmlWriter,
                                                           ns == null ? params.getSchemaLocation() : null, bindings );
        } catch ( Throwable e ) {
            LOG.warn( "Error when writing GetFeatureInfo GML response '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
    }

    private String determineNamespace( FeatureInfoParams params ) {
        String ns = params.getFeatureType() == null ? null : params.getFeatureType().getName().getNamespaceURI();
        if ( ns != null && ns.isEmpty() ) {
            ns = null;
        }
        return ns;
    }

    private GMLVersion getGmlVersion( String format ) {
        GMLVersion gmlVersion = GMLVersion.GML_2;
        if ( format.endsWith( "3.0" ) || format.endsWith( "3.0.1" ) ) {
            gmlVersion = GMLVersion.GML_30;
        }
        if ( format.endsWith( "3.1" ) || format.endsWith( "3.1.1" ) ) {
            gmlVersion = GMLVersion.GML_31;
        }
        if ( format.endsWith( "3.2" ) || format.endsWith( "3.2.1" ) ) {
            gmlVersion = GMLVersion.GML_32;
        }
        return gmlVersion;
    }

}
