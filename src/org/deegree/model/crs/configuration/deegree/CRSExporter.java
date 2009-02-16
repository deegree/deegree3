//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.crs.configuration.deegree;

import static org.deegree.commons.xml.CommonNamespaces.CRSNS;
import static org.deegree.model.crs.projections.ProjectionUtils.EPS11;

import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.FormattingXMLStreamWriter;
import org.deegree.model.crs.CRSIdentifiable;
import org.deegree.model.crs.components.Axis;
import org.deegree.model.crs.components.Ellipsoid;
import org.deegree.model.crs.components.GeodeticDatum;
import org.deegree.model.crs.components.PrimeMeridian;
import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.coordinatesystems.CompoundCRS;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.coordinatesystems.GeocentricCRS;
import org.deegree.model.crs.coordinatesystems.GeographicCRS;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.projections.Projection;
import org.deegree.model.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.model.crs.projections.conic.LambertConformalConic;
import org.deegree.model.crs.projections.cylindric.TransverseMercator;
import org.deegree.model.crs.transformations.helmert.Helmert;
import org.deegree.model.crs.transformations.polynomial.PolynomialTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CRSExporter</code> exports to the old version format (no version attribute).
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class CRSExporter {

    private static Logger LOG = LoggerFactory.getLogger( CRSExporter.class );

    /**
     * 
     * @param properties
     *            to read configuration from.
     */
    public CRSExporter( Properties properties ) {
        // nothing yet.
    }
    
    public CRSExporter( ) {

    }

    /**
     * Export the given list of CoordinateSystems into the crs-definition format.
     * 
     * @param writer
     * @param crsToExport
     */
    public void export( Writer writer, List<CoordinateSystem> crsToExport ) {
    	if ( crsToExport != null ) {
            if ( crsToExport.size() != 0 ) {
                LOG.debug( "Trying to export: " + crsToExport.size() + " coordinate systems." );
                
                //XMLFragment frag = new XMLFragment( new QName( "crs", "definitions", CommonNamespaces.CRSNS ) );
                //Element root = frag.getRootElement();

                // initializing the XML writer
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );
                XMLStreamWriter xmlWriter = null ;
				try {
					xmlWriter = new FormattingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );
				} catch (XMLStreamException e1) {
					e1.printStackTrace();
				}
                
                //LinkedList<String> exportedIDs = new LinkedList<String>();
                Set<Ellipsoid> ellipsoids = new HashSet<Ellipsoid>();
                Set<GeodeticDatum> datums = new HashSet<GeodeticDatum>();
                Set<GeocentricCRS> geocentrics = new HashSet<GeocentricCRS>();
                Set<GeographicCRS> geographics = new HashSet<GeographicCRS>();
                Set<ProjectedCRS> projecteds = new HashSet<ProjectedCRS>();
                Set<CompoundCRS> compounds = new HashSet<CompoundCRS>();
                Set<PrimeMeridian> primeMeridians = new HashSet<PrimeMeridian>();
                Set<Helmert> wgs84s = new HashSet<Helmert>(); 
                
                for ( CoordinateSystem crs : crsToExport ) {
                	if ( crs != null ) {
                		GeodeticDatum d = (GeodeticDatum) crs.getDatum();
                		datums.add( d );
                		ellipsoids.add( d.getEllipsoid() );

                		if ( crs.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
                			geocentrics.add( (GeocentricCRS) crs );
                			//                        export( (GeocentricCRS) crs, root, exportedIDs );
                		} else if ( crs.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                			geographics.add( (GeographicCRS) crs );
                			//                        export( (GeographicCRS) crs, root, exportedIDs ); 
                		} else if ( crs.getType() == CoordinateSystem.PROJECTED_CRS ) {
                			projecteds.add( (ProjectedCRS) crs );
                			//                        export( (ProjectedCRS) crs, root, exportedIDs );
                		} else if ( crs.getType() == CoordinateSystem.COMPOUND_CRS ) {
                			compounds.add( (CompoundCRS) crs );
                			//                        export( (CompoundCRS) crs, root, exportedIDs );
                		}

                		primeMeridians.add( d.getPrimeMeridian() );
                		wgs84s.add( d.getWGS84Conversion() );

                	}
                }
                
                initDocument( xmlWriter );
                for( Ellipsoid e : ellipsoids) {
                	export( e , xmlWriter);
                }                
                for ( GeodeticDatum d : datums ) {
                	export( d, xmlWriter );
                }
                for ( ProjectedCRS projected : projecteds ) {
                	export( projected, xmlWriter );
                }
                for ( GeographicCRS geographic : geographics ) {
                	export( geographic, xmlWriter );
                }
                for ( CompoundCRS compound : compounds ) {
                	export( compound, xmlWriter );
                }
                for ( GeocentricCRS geocentric : geocentrics) {
                	export( geocentric, xmlWriter );
                }
                for ( PrimeMeridian pm : primeMeridians ) {
                	export( pm, xmlWriter );
                }
                for ( Helmert wgs84 : wgs84s ) {
                	export( wgs84, xmlWriter );
                }                

                try {
                	xmlWriter.writeEndElement(); // </crs:definitions>
                	xmlWriter.writeEndDocument();
                	xmlWriter.flush();
                	xmlWriter.close();
                } catch (XMLStreamException e1) {
                	e1.printStackTrace();
                }
                                
//                root.normalize();
//                Document validDoc = createValidDocument( root );
//                try {
//                    org.deegree.commons.xml.XMLFragment frag2 = new XMLFragment( validDoc, "http://www.deegree.org/crs" );
//                    writer.write( frag2.getAsPrettyString() );
//                } catch ( MalformedURLException e ) {
//                    LOG.error( "Could not export crs definitions because: " + e.getMessage(), e );
//                } catch ( XMLProcessingException e ) {
//                    LOG.error( "Could not export crs definitions because: " + e.getMessage(), e );
//                } catch ( IOException e ) {
//                    LOG.error( "Could not export crs definitions because: " + e.getMessage(), e );
//                }
            } else {
                LOG.warn( "No coordinate system were given (list.size() == 0)." );
            }
        } else {
            LOG.error( "No coordinate system were given (list == null)." );
        }
    }

    /*
     *  Open an XML document from stream for exporting
     *  
     *  @param xmlWriter
     *  			xml stream for exporting
     */
    private void initDocument(XMLStreamWriter xmlWriter) {
    	try {
    		xmlWriter.writeStartDocument();
    		xmlWriter.writeStartElement( CRSNS, "definitions" );
    		xmlWriter.writeNamespace( CommonNamespaces.CRS_PREFIX , CommonNamespaces.CRSNS);
    		xmlWriter.writeNamespace( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS);
    		xmlWriter.writeAttribute( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS,
    				"schemaLocation",
    		"http://www.deegree.org/crs /home/ionita/workspace/d3_commons/resources/schema/crsdefinition.xsd" );
    		xmlWriter.writeAttribute( "version", "0.1.0");
    	} catch (XMLStreamException e) {
    		e.printStackTrace();
    	}
    	
//    	private Document createValidDocument( Element root ) {
//            // List<Element> lastInput = new LinkedList<Element>( 100 );
//            try {
//                List<Element> valid = XMLTools.getElements( root, PRE + "ellipsoid", nsContext );
//                valid.addAll( XMLTools.getElements( root, PRE + "geodeticDatum", nsContext ) );
//                valid.addAll( XMLTools.getElements( root, PRE + "projectedCRS", nsContext ) );
//                valid.addAll( XMLTools.getElements( root, PRE + "geographicCRS", nsContext ) );
//                valid.addAll( XMLTools.getElements( root, PRE + "compoundCRS", nsContext ) );
//                valid.addAll( XMLTools.getElements( root, PRE + "geocentricCRS", nsContext ) );
//                valid.addAll( XMLTools.getElements( root, PRE + "primeMeridian", nsContext ) );
//                valid.addAll( XMLTools.getElements( root, PRE + "wgs84Transformation", nsContext ) );
//                Document doc = XMLTools.create();
//                Element newRoot = doc.createElementNS( CommonNamespaces.CRSNS, PRE + "definitions" );
//                newRoot = (Element) doc.importNode( newRoot, false );
//                newRoot = (Element) doc.appendChild( newRoot );
//                for ( int i = 0; i < valid.size(); ++i ) {
//                    Element el = valid.get( i );
//                    el = (Element) doc.importNode( el, true );
//                    newRoot.appendChild( el );
//                }
//                XMLTools.appendNSBinding( newRoot, CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS );
//                newRoot.setAttributeNS(
//                                        CommonNamespaces.XSINS,
//                                        "xsi:schemaLocation",
//                                        "http://www.deegree.org/crs c:/windows/profiles/rutger/EIGE~VO5/eclipse-projekte/coordinate_systems/resources/schema/crsdefinition.xsd" );
//                return doc;
//            } catch ( XMLParsingException xmle ) {
//                xmle.printStackTrace();
//            }
//            return root.getOwnerDocument();
//        }
	}

	/**
     * Export the confInvo to it's appropriate deegree-crs-definitions form.
     * 
     * @param wgs84
     *            to be exported
     * @param xmlWriter
     *            to export to.
     */
    private void export(Helmert wgs84, XMLStreamWriter xmlWriter) {
    	try {
    		xmlWriter.writeStartElement( CRSNS, "wgs84Transformation" );
    		exportIdentifiable( wgs84, xmlWriter );
    		// xAxisTranslation element
    		xmlWriter.writeStartElement( CRSNS, "xAxisTranslation" );
    		xmlWriter.writeCharacters( Double.toString( wgs84.dx ) );
    		xmlWriter.writeEndElement();
    		// yAxisTranslation element
    		xmlWriter.writeStartElement( CRSNS, "yAxisTranslation" );
    		xmlWriter.writeCharacters( Double.toString( wgs84.dy ) );
    		xmlWriter.writeEndElement();
    		// zAxisTranslation element
    		xmlWriter.writeStartElement( CRSNS, "zAxisTranslation" );
    		xmlWriter.writeCharacters( Double.toString( wgs84.dz ) );
    		xmlWriter.writeEndElement();
    		// xAxisRotation element
    		xmlWriter.writeStartElement( CRSNS, "xAxisRotation" );
    		xmlWriter.writeCharacters( Double.toString( wgs84.ex ) );
    		xmlWriter.writeEndElement();
    		// yAxisRotation element
    		xmlWriter.writeStartElement( CRSNS, "yAxisRotation" );
    		xmlWriter.writeCharacters( Double.toString( wgs84.ey ) );
    		xmlWriter.writeEndElement();
    		// zAxisRotation element
    		xmlWriter.writeStartElement( CRSNS, "zAxisRotation" );
    		xmlWriter.writeCharacters( Double.toString( wgs84.ez ) );
    		xmlWriter.writeEndElement();
    		// scaleDifference element
    		xmlWriter.writeStartElement( CRSNS, "scaleDifference" );
    		xmlWriter.writeCharacters( Double.toString( wgs84.ppm ) );
    		xmlWriter.writeEndElement();
    		
    		xmlWriter.writeEndElement();
    	} catch (XMLStreamException e) {
    		e.printStackTrace();
    	}

//        private void export( Helmert confInvo, Element rootNode, final List<String> exportedIds ) {
//            if ( !exportedIds.contains( confInvo.getIdentifier() ) ) {
//                Element convElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "wgs84Transformation" );
//                exportIdentifiable( confInvo, convElement );
//
//                XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "xAxisTranslation",
//                                        Double.toString( confInvo.dx ) );
//                XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "yAxisTranslation",
//                                        Double.toString( confInvo.dy ) );
//                XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "zAxisTranslation",
//                                        Double.toString( confInvo.dz ) );
//                XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "xAxisRotation",
//                                        Double.toString( confInvo.ex ) );
//                XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "yAxisRotation",
//                                        Double.toString( confInvo.ey ) );
//                XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "zAxisRotation",
//                                        Double.toString( confInvo.ez ) );
//                XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "scaleDifference",
//                                        Double.toString( confInvo.ppm ) );
//
//                // Add the ids to the exportedID list.
//                for ( String eID : confInvo.getIdentifiers() ) {
//                    exportedIds.add( eID );
//                }
//
//                // finally add the WGS84-Transformation node to the rootnode.
//                rootNode.appendChild( convElement );
//            }
//
//        }	
   	}

    /**
     * Export the PrimeMeridian to it's appropriate deegree-crs-definitions form.
     * 
     * @param pm
     *            PrimeMeridian to be exported
     * @param xmlWriter
     *            to export to.
     */
	private void export(PrimeMeridian pm, XMLStreamWriter xmlWriter) {
		try {
			xmlWriter.writeStartElement( CRSNS, "primeMeridian" );

			exportIdentifiable( pm, xmlWriter );
			// units element
			export( pm.getAngularUnit(), xmlWriter );
			// longitude element
			xmlWriter.writeStartElement( CRSNS, "longitude");
			xmlWriter.writeCharacters( Double.toString( pm.getLongitude() ) );
			xmlWriter.writeEndElement();
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
//		private void export( PrimeMeridian pMeridian, Element rootNode, final List<String> exportedIds ) {
//            if ( !exportedIds.contains( pMeridian.getIdentifier() ) ) {
//                Element meridianElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "primeMeridian" );
//                exportIdentifiable( pMeridian, meridianElement );
//                export( pMeridian.getAngularUnit(), meridianElement );
//                XMLTools.appendElement( meridianElement, CommonNamespaces.CRSNS, PRE + "longitude",
//                                        Double.toString( pMeridian.getLongitude() ) );
//
//                // Add the ids to the exportedID list.
//                for ( String eID : pMeridian.getIdentifiers() ) {
//                    exportedIds.add( eID );
//                }
//
//                // finally add the prime meridian node to the rootnode.
//                rootNode.appendChild( meridianElement );
//            }
//        }
	}

	/**
     * Export the compoundCRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param compoundCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geographic CRS to.
     */
    private void export(CompoundCRS crs, XMLStreamWriter xmlWriter) {
    	try {
    		xmlWriter.writeStartElement( CRSNS , "compoundCRS" );

    		exportIdentifiable( crs, xmlWriter );
    		CoordinateSystem underCRS = crs.getUnderlyingCRS();
    		// usedCRS element
    		xmlWriter.writeStartElement( CRSNS , "usedCRS" );
    		xmlWriter.writeCharacters( underCRS.getIdentifier() );
    		xmlWriter.writeEndElement();
    		// heightAxis element
    		Axis heightAxis = crs.getHeightAxis();
    		export( heightAxis, "heightAxis", xmlWriter );
    		// defaultHeight element
    		double axisHeight = crs.getDefaultHeight();
    		xmlWriter.writeStartElement( CRSNS , "defaultHeight" );
    		xmlWriter.writeCharacters( Double.toString( axisHeight ) );
    		xmlWriter.writeEndElement();
    		xmlWriter.writeEndElement();
    	} catch (XMLStreamException e) {
    		e.printStackTrace();
    	}
    	
//        private void export( CompoundCRS compoundCRS, Element rootNode, List<String> exportedIds ) {
//            if ( !exportedIds.contains( compoundCRS.getIdentifier() ) ) {
//                Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "compoundCRS" );
//                exportIdentifiable( compoundCRS, crsElement );
//                CoordinateSystem underLyingCRS = compoundCRS.getUnderlyingCRS();
//                if ( underLyingCRS.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
//                    export( (GeographicCRS) underLyingCRS, rootNode, exportedIds );
//                } else if ( underLyingCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
//                    export( (ProjectedCRS) underLyingCRS, rootNode, exportedIds );
//                }
//
//                // Add a reference from the geographicCRS element to the projectedCRS element.
//                XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedCRS", underLyingCRS.getIdentifier() );
//                export( compoundCRS.getHeightAxis(), crsElement );
//
//                XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "defaultHeight",
//                                        Double.toString( compoundCRS.getDefaultHeight() ) );
//
//                // Add the ids to the exportedID list.
//                for ( String eID : compoundCRS.getIdentifiers() ) {
//                    exportedIds.add( eID );
//                }
//                // finally add the crs node to the rootnode.
//                rootNode.appendChild( crsElement );
//            }
//        }		
	}
    
    /**
     * Export the projected CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param projectedCRS
     *            to be exported
     * @param xmlWriter
     *            to export the projected CRS to.
     */
	private void export(ProjectedCRS projectedCRS, XMLStreamWriter xmlWriter) {
		try {
    		xmlWriter.writeStartElement( CRSNS, "projectedCRS" );
    		exportAbstractCRS( (CoordinateSystem) projectedCRS, xmlWriter );
    		
    		xmlWriter.writeStartElement(CRSNS, "usedGeographicCRS" );
    		xmlWriter.writeCharacters( projectedCRS.getGeographicCRS().getIdentifier() );
    		xmlWriter.writeEndElement();
    		
    		// projection
    		export( projectedCRS.getProjection(), xmlWriter );

    		xmlWriter.writeEndElement();
    	} catch (XMLStreamException e) {
    		e.printStackTrace();
    	}
						
//		if ( !exportedIds.contains( projectedCRS.getIdentifier() ) ) {
//            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "projectedCRS" );
//            exportAbstractCRS( projectedCRS, crsElement );
//            GeographicCRS underLyingCRS = projectedCRS.getGeographicCRS();
//            export( underLyingCRS, rootNode, exportedIds );
//
//            // Add a reference from the geographicCRS element to the projectedCRS element.
//            XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedGeographicCRS",
//                                    underLyingCRS.getIdentifier() );
//
//            export( projectedCRS.getProjection(), crsElement );
//
//            // Add the ids to the exportedID list.
//            for ( String eID : projectedCRS.getIdentifiers() ) {
//                exportedIds.add( eID );
//            }
//            // finally add the crs node to the rootnode.
//            rootNode.appendChild( crsElement );
//        }
	}

	/**
     * Export the projection to it's appropriate deegree-crs-definitions form.
     * 
     * @param projection
     *            to be exported
     * @param xmlWriter
     *            to export the projection to.
     */
	private void export(Projection projection, XMLStreamWriter xmlWriter) {
		try {
			xmlWriter.writeStartElement( CRSNS, "projection" );

			String implName = projection.getImplementationName();
			xmlWriter.writeStartElement( CRSNS, "" +  implName );
			// latitudeOfNaturalOrigin
			xmlWriter.writeStartElement( CRSNS, "latitudeOfNaturalOrigin" );
			xmlWriter.writeAttribute( "inDegrees", "true" );
			xmlWriter.writeCharacters( Double.toString( Math.toDegrees( projection.getProjectionLatitude() ) ) );
			xmlWriter.writeEndElement();
			// longitudeOfNaturalOrigin
			xmlWriter.writeStartElement( CRSNS, "longitudeOfNaturalOrigin" );
			xmlWriter.writeAttribute( "inDegrees", "true" );
			xmlWriter.writeCharacters( Double.toString( Math.toDegrees( projection.getProjectionLongitude() ) ) );
			xmlWriter.writeEndElement();
			// scaleFactor element
			xmlWriter.writeStartElement( CRSNS, "scaleFactor" );
			xmlWriter.writeCharacters( Double.toString( projection.getScale() ) );
			xmlWriter.writeEndElement();
			// falseEasting element
			xmlWriter.writeStartElement( CRSNS, "falseEasting" );
			xmlWriter.writeCharacters( Double.toString( projection.getFalseEasting() ) );
			xmlWriter.writeEndElement();
			// falseNorthing element
			xmlWriter.writeStartElement( CRSNS, "falseNorthing" );
			xmlWriter.writeCharacters( Double.toString( projection.getFalseNorthing() ) );
			xmlWriter.writeEndElement();
			if ( "transverseMercator".equalsIgnoreCase( implName ) ) {
				xmlWriter.writeStartElement( CRSNS, "northernHemisphere" );
				xmlWriter.writeCharacters( Boolean.toString( ( (TransverseMercator) projection ).getHemisphere() ) );
				xmlWriter.writeEndElement();
			} else if ( "lambertConformalConic".equalsIgnoreCase( implName ) ) {
				double paralellLatitude = ( (LambertConformalConic) projection ).getFirstParallelLatitude();
				if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
					paralellLatitude = Math.toDegrees( paralellLatitude );
					xmlWriter.writeStartElement( CRSNS, "firstParallelLatitude" );
					xmlWriter.writeAttribute( "inDegrees", "true" );
					xmlWriter.writeCharacters( Double.toString( paralellLatitude ) );
					xmlWriter.writeEndElement();
				}
				paralellLatitude = ( (LambertConformalConic) projection ).getSecondParallelLatitude();
				if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
					paralellLatitude = Math.toDegrees( paralellLatitude );
					xmlWriter.writeStartElement( CRSNS, "secondParallelLatitude" );
					xmlWriter.writeAttribute( "inDegrees", "true" );
					xmlWriter.writeCharacters( Double.toString( paralellLatitude ) );
					xmlWriter.writeEndElement();
				}
			} else if ( "stereographicAzimuthal".equalsIgnoreCase( implName ) ) {
				xmlWriter.writeStartElement( CRSNS, "trueScaleLatitude" );
				xmlWriter.writeAttribute( "inDegrees", "true" );
				xmlWriter.writeCharacters( Double.toString( ( (StereographicAzimuthal) projection ).getTrueScaleLatitude() ) );
				xmlWriter.writeEndElement();
			}
			xmlWriter.writeEndElement();
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
//		private void export( Projection projection, Element rootNode ) {
//	        Element rootElem = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "projection" );
//	        String elementName = projection.getImplementationName();
//	        Element projectionElement = XMLTools.appendElement( rootElem, CommonNamespaces.CRSNS, PRE + elementName );
//	        // exportIdentifiable( projection, projectionElement );
//	        Element tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE
//	                                                                                         + "latitudeOfNaturalOrigin",
//	                                              Double.toString( Math.toDegrees( projection.getProjectionLatitude() ) ) );
//	        tmp.setAttribute( "inDegrees", "true" );
//	        tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "longitudeOfNaturalOrigin",
//	                                      Double.toString( Math.toDegrees( projection.getProjectionLongitude() ) ) );
//	        tmp.setAttribute( "inDegrees", "true" );
//
//	        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "scaleFactor",
//	                                Double.toString( projection.getScale() ) );
//	        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "falseEasting",
//	                                Double.toString( projection.getFalseEasting() ) );
//	        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "falseNorthing",
//	                                Double.toString( projection.getFalseNorthing() ) );
//	        if ( "transverseMercator".equalsIgnoreCase( elementName ) ) {
//	            XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "northernHemisphere",
//	                                    Boolean.toString( ( (TransverseMercator) projection ).getHemisphere() ) );
//	        } else if ( "lambertConformalConic".equalsIgnoreCase( elementName ) ) {
//	            double paralellLatitude = ( (LambertConformalConic) projection ).getFirstParallelLatitude();
//	            if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
//	                paralellLatitude = Math.toDegrees( paralellLatitude );
//	                tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "firstParallelLatitude",
//	                                              Double.toString( paralellLatitude ) );
//	                tmp.setAttribute( "inDegrees", "true" );
//	            }
//	            paralellLatitude = ( (LambertConformalConic) projection ).getSecondParallelLatitude();
//	            if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
//	                paralellLatitude = Math.toDegrees( paralellLatitude );
//	                tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS,
//	                                              PRE + "secondParallelLatitude", Double.toString( paralellLatitude ) );
//	                tmp.setAttribute( "inDegrees", "true" );
//	            }
//	        } else if ( "stereographicAzimuthal".equalsIgnoreCase( elementName ) ) {
//	            tmp = XMLTools.appendElement(
//	                                          projectionElement,
//	                                          CommonNamespaces.CRSNS,
//	                                          PRE + "trueScaleLatitude",
//	                                          Double.toString( ( (StereographicAzimuthal) projection ).getTrueScaleLatitude() ) );
//	            tmp.setAttribute( "inDegrees", "true" );
//	        }
//	    }
	}

	/**
     * Export the geocentric/geographic CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param geographicCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geographic CRS to.
     */
	private void export(GeographicCRS crs, XMLStreamWriter xmlWriter) {
		try {
			xmlWriter.writeStartElement( CRSNS, "geographicCRS" );

			exportAbstractCRS( (CoordinateSystem) crs, xmlWriter );
			xmlWriter.writeStartElement( CRSNS, "usedDatum" );
			xmlWriter.writeCharacters( crs.getDatum().getIdentifier() );
			xmlWriter.writeEndElement();
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
//	    private void export( GeographicCRS geographicCRS, Element rootNode, List<String> exportedIds ) {
//	        if ( !exportedIds.contains( geographicCRS.getIdentifier() ) ) {
//	            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geographicCRS" );
//	            exportAbstractCRS( geographicCRS, crsElement );
//
//	            // export the datum.
//	            GeodeticDatum datum = geographicCRS.getGeodeticDatum();
//	            if ( datum != null ) {
//	                export( datum, rootNode, exportedIds );
//	                // Add a reference from the datum element to the geographic element.
//	                XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedDatum", datum.getIdentifier() );
//	            } else {
//	                LOG.error( "The given datum is not a geodetic one, this mey not be!" );
//	            }
//	            // Add the ids to the exportedID list.
//	            for ( String eID : geographicCRS.getIdentifiers() ) {
//	                exportedIds.add( eID );
//	            }
//	            // finally add the crs node to the rootnode.
//	            rootNode.appendChild( crsElement );
//	        }
//	    }
	}

	
	/**
     * Export the geocentric CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param geocentricCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geocentric CRS to.
     */
	private void export(GeocentricCRS geocentricCRS, XMLStreamWriter xmlWriter) {
    	try {
    		xmlWriter.writeStartElement( CRSNS, "geocentricCRS" );
    		exportAbstractCRS( (CoordinateSystem) geocentricCRS, xmlWriter );
    		GeodeticDatum datum = geocentricCRS.getGeodeticDatum();
    		export( datum, xmlWriter );
    		xmlWriter.writeEndElement();
    	} catch (XMLStreamException e) {
    		e.printStackTrace();
    	}
    	
//    	private void export( GeocentricCRS geocentricCRS, Element rootNode, List<String> exportedIds ) {
//            if ( !exportedIds.contains( geocentricCRS.getIdentifier() ) ) {
//                Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geocentricCRS" );
//                exportAbstractCRS( geocentricCRS, crsElement );
//                // export the datum.
//                GeodeticDatum datum = geocentricCRS.getGeodeticDatum();
//                if ( datum != null ) {
//                    export( datum, rootNode, exportedIds );
//                    // Add a reference from the datum element to the geocentric element.
//                    XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedDatum", datum.getIdentifier() );
//                } // Add the ids to the exportedID list.
//                for ( String eID : geocentricCRS.getIdentifiers() ) {
//                    exportedIds.add( eID );
//                }
//                // finally add the crs node to the rootnode.
//                rootNode.appendChild( crsElement );
//            }
//        }	
    }

	/**
     * Export toplevel crs features.
     * 
     * @param crs
     *            to be exported
     * @param xmlWriter
     *            to export to
     */
	private void exportAbstractCRS(CoordinateSystem crs, XMLStreamWriter xmlWriter) {
		exportIdentifiable( (CRSIdentifiable) crs, xmlWriter );

		Axis[] axes = crs.getAxis();
		StringBuilder axisOrder = new StringBuilder ( 4 ); // maxOccurs of Axis = 3 in the schema

		for ( int i = 0; i < axes.length; ++i ) {
			Axis a = axes[i];
			export( a, "Axis", xmlWriter );
			axisOrder.append( a.getName() );
			if ( ( i + 1 ) < axes.length )
				axisOrder.append( ", " );
		}

		// write the axisOrder
		try {
			xmlWriter.writeStartElement( CRSNS, "axisOrder" );
			xmlWriter.writeCharacters( axisOrder.toString() );
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {

			e.printStackTrace();
		}
		
		// export transformations and recurse on their type
		exportTransformations( crs.getTransformations(), xmlWriter );
			
//	private void exportAbstractCRS( CoordinateSystem crs, Element crsElement ) {
//        exportIdentifiable( crs, crsElement );
//        Axis[] axis = crs.getAxis();
//        StringBuilder axisOrder = new StringBuilder( 200 );
//        for ( int i = 0; i < axis.length; ++i ) {
//            Axis a = axis[i];
//            export( a, crsElement );
//            axisOrder.append( a.getName() );
//            if ( ( i + 1 ) < axis.length ) {
//                axisOrder.append( ", " );
//            }
//        }
//        XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "axisOrder", axisOrder.toString() );
//
//        export( crs.getTransformations(), crsElement );
//
    }
	
	
	/**
     * Export a list of transformations from the crs element to xml with respect to the crs-definitions schema layout.
     * 
     * @param transformations
     *            to be exported.
     * @param xmlWriter
     *            to export to.
     */
	private void exportTransformations(	List<PolynomialTransformation> transformations,	XMLStreamWriter xmlWriter) {
	    for ( PolynomialTransformation transformation : transformations ) {
	    	try {
	    		xmlWriter.writeStartElement( CRSNS, "polynomialTransformation" );

	    		if ( !"leastsquare".equals( transformation.getImplementationName().toLowerCase() ) ) {
	    			xmlWriter.writeAttribute( "class", transformation.getClass().getCanonicalName() );
	    		}
	    		xmlWriter.writeStartElement( CRSNS, "" + transformation.getImplementationName() );
	    		// polynomialOrder
	    		xmlWriter.writeStartElement( CRSNS, "polynomialOrder");
	    		xmlWriter.writeCharacters( Integer.toString( transformation.getOrder() ) );
	    		xmlWriter.writeEndElement();
	    		// xParameters
	    		xmlWriter.writeStartElement( CRSNS, "xParameters");
	    		xmlWriter.writeCharacters( transformation.getFirstParams().toString() );
	    		xmlWriter.writeEndElement();
	    		// yParameters
	    		xmlWriter.writeStartElement( CRSNS, "yParameters");
	    		xmlWriter.writeCharacters( transformation.getSecondParams().toString() );
	    		xmlWriter.writeEndElement();
	    		// targetCRS
	    		xmlWriter.writeStartElement( CRSNS, "targetCRS");
	    		xmlWriter.writeCharacters( transformation.getTargetCRS().getIdentifier() );
	    		xmlWriter.writeEndElement();

	    		xmlWriter.writeEndElement();
	    		xmlWriter.writeEndElement();
	    	} catch (XMLStreamException e) {
	    		e.printStackTrace();
	    	} 
	    }

//	    private void export( List<PolynomialTransformation> transformations, Element currentNode ) {
//	        for ( PolynomialTransformation transformation : transformations ) {
//	            Element transformationElement = XMLTools.appendElement( currentNode, CRSNS, PRE
//	                                                                                        + "polynomialTransformation" );
//	            if ( !"leastsquare".equals( transformation.getImplementationName().toLowerCase() ) ) {
//	                transformationElement.setAttribute( "class", transformation.getClass().getCanonicalName() );
//	            }
//	            Element transformElement = XMLTools.appendElement( transformationElement, CRSNS,
//	                                                               PRE + transformation.getImplementationName() );
//	            XMLTools.appendElement( transformElement, CRSNS, PRE + "polynomialOrder",
//	                                    Integer.toString( transformation.getOrder() ) );
//	            XMLTools.appendElement( transformElement, CRSNS, PRE + "xParameters",
//	                                    transformation.getFirstParams().toString() );
//	            XMLTools.appendElement( transformElement, CRSNS, PRE + "yParameters",
//	                                    transformation.getSecondParams().toString() );
//	            XMLTools.appendElement( transformElement, CRSNS, PRE + "targetCRS",
//	                                    transformation.getTargetCRS().getIdentifier() );
//	        }
//	    }
	}

	/**
     * Export an axis to xml in the crs-definitions schema layout.
     * 
     * @param axis
     *            to be exported.
     * @param elName
     * 			  the name of the element, either 'Axis' or 'heightAxis'
     * @param xmlWriter
     *            to export to.
     */
	private void export(Axis a, String elName, XMLStreamWriter xmlWriter) {
		try {
			xmlWriter.writeStartElement( CRSNS, elName );
			// axis name
			xmlWriter.writeStartElement( CRSNS, "name" );
			xmlWriter.writeCharacters( a.getName() );
			xmlWriter.writeEndElement();
			// axis units
			export( a.getUnits(), xmlWriter );
			// axis orientation
			xmlWriter.writeStartElement( CRSNS, "axisOrientation" );
			xmlWriter.writeCharacters( a.getOrientationAsString() );
			xmlWriter.writeEndElement();
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
//		OLD CODE
//    private void export( Axis axis, Element currentNode ) {
//        Document doc = currentNode.getOwnerDocument();
//        Element axisElement = doc.createElementNS( CRSNS, PRE + "Axis" );
//        // The name.
//        XMLTools.appendElement( axisElement, CommonNamespaces.CRSNS, PRE + "name", axis.getName() );
//
//        // the units.
//        Unit units = axis.getUnits();
//        export( units, axisElement );
//
//        XMLTools.appendElement( axisElement, CommonNamespaces.CRSNS, PRE + "axisOrientation",
//                                axis.getOrientationAsString() );
//        currentNode.appendChild( axisElement );
    }

	/**
     * Export a unit to xml in the crs-definitions schema layout.
     * 
     * @param units
     *            to be exported.
     * @param xmlWriter
     *            to export to.
     */
	private void export(Unit units, XMLStreamWriter xmlWriter) {
		if ( units != null ) {
			try {
				xmlWriter.writeStartElement( CRSNS, "units" );
				xmlWriter.writeCharacters( units.getName().toLowerCase() );
				xmlWriter.writeEndElement();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
	
//    private void export( Unit units, Element currentNode ) {
//        if ( units != null && currentNode != null ) {
//            XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "units", units.getName() );
//        }
//    }	
	}

	/**
     * Export the datum to it's appropriate deegree-crs-definitions form.
     * 
     * @param datum
     *            to be exported
     * @param xmlWriter
     *            to export the datum to.
     */
	private void export(GeodeticDatum datum, XMLStreamWriter xmlWriter) {
		try {
			xmlWriter.writeStartElement( CRSNS, "geodeticDatum" );
			exportIdentifiable( datum, xmlWriter );
			// usedEllipsoid element
			xmlWriter.writeStartElement( CRSNS, "usedEllipsoid" );
			xmlWriter.writeCharacters( datum.getEllipsoid().getIdentifier() );
			xmlWriter.writeEndElement();
			// usedPrimeMeridian element
			PrimeMeridian pm = datum.getPrimeMeridian();
			if ( pm != null ) {
				xmlWriter.writeStartElement( CRSNS, "usedPrimeMeridian" );
				xmlWriter.writeCharacters( pm.getIdentifier() );
				xmlWriter.writeEndElement();
			}
			// usedWGS84ConversionInfo element
			Helmert convInfo = datum.getWGS84Conversion();
			if ( convInfo != null ) {
				xmlWriter.writeStartElement( CRSNS, "usedWGS84ConversionInfo" );
				xmlWriter.writeCharacters( convInfo.getIdentifier() );
				xmlWriter.writeEndElement();
			}
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
//	    private void export( GeodeticDatum datum, Element rootNode, List<String> exportedIds ) {
//	        if ( !exportedIds.contains( datum.getIdentifier() ) ) {
//	            Element datumElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geodeticDatum" );
//	            exportIdentifiable( datum, datumElement );
//	            /**
//	             * EXPORT the ELLIPSOID
//	             */
//	            Ellipsoid ellipsoid = datum.getEllipsoid();
//	            if ( ellipsoid != null ) {
//	                export( ellipsoid, rootNode, exportedIds );
//	                // Add a reference from the ellipsoid element to the datum element.
//	                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedEllipsoid",
//	                                        ellipsoid.getIdentifier() );
//	            }
//
//	            /**
//	             * EXPORT the PRIME_MERIDIAN
//	             */
//	            PrimeMeridian pMeridian = datum.getPrimeMeridian();
//	            if ( pMeridian != null ) {
//	                export( pMeridian, rootNode, exportedIds );
//	                // Add a reference from the prime meridian element to the datum element.
//	                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedPrimeMeridian",
//	                                        pMeridian.getIdentifier() );
//	            }
//
//	            /**
//	             * EXPORT the WGS-84-Conversion INFO
//	             */
//	            Helmert confInvo = datum.getWGS84Conversion();
//	            if ( confInvo != null ) {
//	                export( confInvo, rootNode, exportedIds );
//	                // Add a reference from the prime meridian element to the datum element.
//	                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedWGS84ConversionInfo",
//	                                        confInvo.getIdentifier() );
//	            }
//
//	            // Add the ids to the exportedID list.
//	            for ( String eID : datum.getIdentifiers() ) {
//	                exportedIds.add( eID );
//	            }
//	            // finally add the datum node to the rootnode.
//	            rootNode.appendChild( datumElement );
//	        }
//	    }
	}

	/**
     * Export the ellipsoid to it's appropriate deegree-crs-definitions form.
     * 
     * @param ellipsoid
     *            to be exported
     * @param xmlWriter
     *            to export the ellipsoid to.
     */
	protected void export(Ellipsoid ellipsoid, XMLStreamWriter xmlWriter) {
    	 if ( ellipsoid != null ) {
    		 try {
    			 xmlWriter.writeStartElement( CRSNS, "ellipsoid");

    			 // write the elements that are specific to Identifiable
    			 exportIdentifiable( ellipsoid, xmlWriter);

    			 double sMajorAxis = ellipsoid.getSemiMajorAxis();
    			 xmlWriter.writeStartElement( CRSNS, "semiMajorAxis" ); 
    			 xmlWriter.writeCharacters( Double.toString(sMajorAxis) );
    			 xmlWriter.writeEndElement();
    			 
//    			 double ecc = ellipsoid.getEccentricity();
//    			 xmlWriter.writeStartElement( CRSNS, "eccentricity" );
//    			 xmlWriter.writeCharacters( Double.toString(ecc) );
//    			 xmlWriter.writeEndElement();
    			 
    			 double inverseF = ellipsoid.getInverseFlattening();    			 
    			 xmlWriter.writeStartElement( CRSNS, "inverseFlattening" );
    			 xmlWriter.writeCharacters( Double.toString(inverseF) );
    			 xmlWriter.writeEndElement();
    			 
//    			 double sMinorAxis = ellipsoid.getSemiMinorAxis();
//    			 xmlWriter.writeStartElement( CRSNS, "semiMinorAxis" );
//    			 xmlWriter.writeCharacters( Double.toString(sMinorAxis) );
//    			 xmlWriter.writeEndElement();
    			 
    			 Unit u = ellipsoid.getUnits();
    			 export( u, xmlWriter );
    			 
    			 xmlWriter.writeEndElement();
    		 } catch (XMLStreamException e) {
    			 e.printStackTrace();
    		 }
    	 }
                       
//    	 private void export( Ellipsoid ellipsoid, Element rootNode, final List<String> exportedIds ) {
//    	        if ( !exportedIds.contains( ellipsoid.getIdentifier() ) ) {
//    	            Element ellipsoidElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "ellipsoid" );
//    	            exportIdentifiable( ellipsoid, ellipsoidElement );
//    	            XMLTools.appendElement( ellipsoidElement, CommonNamespaces.CRSNS, PRE + "semiMajorAxis",
//    	                                    Double.toString( ellipsoid.getSemiMajorAxis() ) );
//    	            XMLTools.appendElement( ellipsoidElement, CommonNamespaces.CRSNS, PRE + "inverseFlatting",
//    	                                    Double.toString( ellipsoid.getInverseFlattening() ) );
//    	            export( ellipsoid.getUnits(), ellipsoidElement );
//
//    	            // Add the ids to the exportedID list.
//    	            for ( String eID : ellipsoid.getIdentifiers() ) {
//    	                exportedIds.add( eID );
//    	            }
//    	            // finally add the ellipsoid node to the rootnode.
//    	            rootNode.appendChild( ellipsoidElement );
//    	        }
//    	    }
	}


	/**
     * Creates the basic nodes of the identifiable object.
     * 
     * @param crsIdentifiable
     *            object to be exported.
     * @param xmlWriter
     *            to export to
     */
	private void exportIdentifiable(CRSIdentifiable identifiable, XMLStreamWriter xmlWriter) {
		try {
			// ids
			String[] identifiers = identifiable.getIdentifiers();
			for ( String id : identifiers ) {
				if ( id != null ) {
					xmlWriter.writeStartElement( CRSNS, "id" );
					xmlWriter.writeCharacters( id );
					xmlWriter.writeEndElement();
				}
			}
			// names
			String[] names = identifiable.getNames();
			if ( names != null && names.length > 0 ) {
				for ( String name : names ) {
					if ( name != null ) {
						xmlWriter.writeStartElement( CRSNS, "name");
						xmlWriter.writeCharacters( name );
						xmlWriter.writeEndElement();
					}
				}
			}
			// versions
			String[] versions = identifiable.getVersions();
			if ( versions != null && versions.length > 0 ) {
				for ( String version : versions ) {
					if ( version != null ) {
						xmlWriter.writeStartElement( CRSNS, "version");
						xmlWriter.writeCharacters( version );
						xmlWriter.writeEndElement();
					}
				}
			}
			// descriptions
			String[] descriptions = identifiable.getDescriptions();
			if ( descriptions != null && descriptions.length > 0 ) {
				for ( String description : descriptions ) {
					if ( description != null ) {
						xmlWriter.writeStartElement( CRSNS, "description");
						xmlWriter.writeCharacters( description );
						xmlWriter.writeEndElement();
					}
				}
			}
			// areasOfUse
			String[] areas = identifiable.getAreasOfUse();
			if ( areas != null && areas.length > 0 ) {
				for ( String area : areas ) {
					if ( area != null ) {
						xmlWriter.writeStartElement( CRSNS, "areaOfUse" );
						xmlWriter.writeCharacters( area );
						xmlWriter.writeEndElement();
					}
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
//	    private void exportIdentifiable( CRSIdentifiable id, Element currentNode ) {
//	        for ( String i : id.getIdentifiers() ) {
//	            if ( i != null ) {
//	                XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "id", i );
//	            }
//
//	        }
//	        if ( id.getNames() != null && id.getNames().length > 0 ) {
//	            for ( String i : id.getNames() ) {
//	                if ( i != null ) {
//	                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "name", i );
//	                }
//	            }
//	        }
//	        if ( id.getVersions() != null && id.getVersions().length > 0 ) {
//	            for ( String i : id.getVersions() ) {
//	                if ( i != null ) {
//	                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "version", i );
//	                }
//	            }
//	        }
//	        if ( id.getDescriptions() != null && id.getDescriptions().length > 0 ) {
//	            for ( String i : id.getDescriptions() ) {
//	                if ( i != null ) {
//	                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "description", i );
//	                }
//	            }
//	        }
//	        if ( id.getAreasOfUse() != null && id.getAreasOfUse().length > 0 ) {
//	            for ( String i : id.getAreasOfUse() ) {
//	                if ( i != null ) {
//	                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "areaOfUse", i );
//	                }
//	            }
//	        }
//	    }
	}

//	
//	Only for testing
//	
//	public static void main(String[] args) {
//		CRSProvider provider = CRSConfiguration.getCRSConfiguration().getProvider();
//        DeegreeCRSProvider dProvider = (DeegreeCRSProvider) provider;
//        List<CoordinateSystem> crss = dProvider.getAvailableCRSs();
//        
//        CRSExporter exporter = new CRSExporter();
//        FileWriter fwriter;
//        try {
//        	fwriter = new FileWriter( "/home/ionita/workspace/d3_commons/src/org/deegree/model/crs/configuration/deegree/testExporter.xml" );
//        	exporter.export( fwriter, crss);
//        	fwriter.flush();
//        	fwriter.close();
//        } catch (IOException e) {
//        	e.printStackTrace();
//        } 
//	}
		
}
