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
package org.deegree.tools.crs.georeferencing.communication.dialog.menuitem;

import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;

import java.awt.Component;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.tools.crs.georeferencing.communication.dialog.AbstractGRDialog;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMSParameterChooser extends AbstractGRDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private List<JCheckBox> checkBoxListLayer;

    private ButtonGroup groupFormat;

    private ButtonGroup groupSRS;

    private List<JCheckBox> checkBoxListFormat;

    private List<JCheckBox> checkBoxSRSList;

    private WMSClient111 wmsClient;

    private URL url;

    public WMSParameterChooser( Component parent, String urlString ) throws MalformedURLException, NullPointerException {
        super( parent, new Dimension( 300, 600 ) );

        url = new URL( urlString );

        wmsClient = new WMSClient111( url );

        groupFormat = new ButtonGroup();
        groupSRS = new ButtonGroup();

        List<String> allLayers = wmsClient.getNamedLayers();
        List<String> allFormats = wmsClient.getFormats( GetMap );
        this.setCheckBoxListLayer( allLayers );
        this.setCheckBoxListFormat( allFormats );

    }

    public List<String> getCheckBoxListLayer() {
        List<String> selectedLayer = new ArrayList<String>();
        for ( JCheckBox check : checkBoxListLayer ) {
            if ( check.isSelected() ) {
                selectedLayer.add( check.getText() );
            }
        }

        return selectedLayer;
    }

    public StringBuilder getCheckBoxListAsString() {
        StringBuilder s = new StringBuilder();
        for ( JCheckBox check : checkBoxListLayer ) {
            if ( s.length() == 0 ) {
                if ( check.isSelected() ) {
                    s.append( check.getText() );
                }
            } else {
                if ( check.isSelected() ) {
                    s.append( ',' ).append( check.getText() );
                }
            }
        }

        return s;
    }

    public void setCheckBoxListLayer( List<String> checkBoxList ) {
        JLabel label = new JLabel( "Layers" );
        this.getPanel().add( label );
        checkBoxListLayer = new ArrayList<JCheckBox>();
        for ( String s : checkBoxList ) {
            checkBoxListLayer.add( new JCheckBox( s ) );
        }
        if ( checkBoxListLayer != null ) {
            for ( JCheckBox check : checkBoxListLayer ) {
                this.getPanel().add( check );
            }
        }
        setSRSList( checkBoxList );
    }

    public StringBuilder getCheckBoxFormatAsString() {
        StringBuilder s = new StringBuilder();
        for ( JCheckBox check : checkBoxListFormat ) {

            if ( check.isSelected() ) {
                s.append( check.getText() );
                break;
            }
        }
        return s;
    }

    public void setCheckBoxListFormat( List<String> checkBoxList ) {
        JLabel label = new JLabel( "Formats" );
        this.getPanel().add( label );
        checkBoxListFormat = new ArrayList<JCheckBox>();
        for ( String s : checkBoxList ) {
            checkBoxListFormat.add( new JCheckBox( s ) );
        }
        if ( checkBoxListFormat != null ) {
            for ( JCheckBox check : checkBoxListFormat ) {
                groupFormat.add( check );
                this.getPanel().add( check );
            }
        }

    }

    private void setSRSList( List<String> layerList ) {
        JLabel label = new JLabel( "Coordinatesystems" );
        this.getPanel().add( label );
        checkBoxSRSList = new ArrayList<JCheckBox>();
        List<String> srsList = null;
        Set<String> srsSet = new HashSet<String>();

        for ( String layerName : layerList ) {
            srsList = wmsClient.getCoordinateSystems( layerName );
            for ( String s : srsList ) {
                srsSet.add( s );
            }

        }
        for ( String s : srsSet ) {
            checkBoxSRSList.add( new JCheckBox( s ) );
        }
        if ( checkBoxSRSList != null ) {
            for ( JCheckBox check : checkBoxSRSList ) {
                groupSRS.add( check );
                this.getPanel().add( check );
            }
        }

    }

    public CRS getCheckBoxSRS() {
        CRS crs = null;
        for ( JCheckBox check : checkBoxSRSList ) {

            if ( check.isSelected() ) {
                crs = new CRS( check.getText() );
                break;
            }
        }
        return crs;
    }

    public Envelope getEnvelope( CRS srs, List<String> layerList ) {

        return wmsClient.getBoundingBox( srs.getName(), layerList );
    }

    public URL getMapURL() {
        return url;
    }

}
