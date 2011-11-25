//$HeadURL$
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
package org.deegree.tools.alkis;

import static org.slf4j.LoggerFactory.getLogger;

import javax.xml.namespace.QName;

import org.deegree.commons.annotations.Tool;
import org.deegree.gml.GMLInputFactory;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Tool(value = "adds inversDientZurDarstellungVon properties for GeoInfoDok 6.0.1 files")
public class BackReferenceFixer {

    private static final Logger LOG = getLogger( BackReferenceFixer.class );

    private static String ns601 = "http://www.adv-online.de/namespaces/adv/gid/6.0";

    private static QName[] interestingProps601 = new QName[] { new QName( ns601, "AP_Darstellung" ),
                                                              new QName( ns601, "AP_LTO" ),
                                                              new QName( ns601, "AP_PTO" ),
                                                              new QName( ns601, "AP_FTO" ),
                                                              new QName( ns601, "AP_LTO" ), new QName( ns601, "AP_PPO" ) };

    /**
     * Update index. Must be called after each transaction.
     */
    // public static void update() {
    // if ( featureStore == null ) {
    // return;
    // }
    //
    // featuresWithPO.clear();
    //
    // try {
    // for ( QName q : is511 ? interestingProps511 : interestingProps601 ) {
    // FeatureResultSet col = featureStore.query( new Query( q, null, null, -1, -1, -1 ) );
    // QName name = new QName( is511 ? ns511 : ns601, "dientZurDarstellungVon" );
    // for ( Feature f : col ) {
    // for ( Property p : f.getProperties( name ) ) {
    // if ( p != null && p.getType() instanceof FeaturePropertyType ) {
    // FeatureReference ref = (FeatureReference) p.getValue();
    // if ( ref.isResolved() ) {
    // featuresWithPO.add( ref.getReferencedObject() );
    // }
    // }
    // }
    // }
    // }
    // } catch ( FeatureStoreException e ) {
    // LOG.warn( "Could not update the HasPO index: {}", e.getLocalizedMessage() );
    // LOG.trace( "Stack trace:", e );
    // } catch ( FilterEvaluationException e ) {
    // LOG.warn( "Could not update the HasPO index: {}", e.getLocalizedMessage() );
    // LOG.trace( "Stack trace:", e );
    // }
    // }

    public static void main( String[] args ) {
        GMLInputFactory fac = new GMLInputFactory();
        // fac.createGMLStreamReader(GMLVersion.GML_32, null );
    }

}
