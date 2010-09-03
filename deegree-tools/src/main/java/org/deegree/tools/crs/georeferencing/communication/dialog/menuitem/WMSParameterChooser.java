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
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.tools.crs.georeferencing.communication.dialog.AbstractGRDialog;

/**
 * Dialog that handles the communication of a WMS-request.
 * <p>
 * TODO this should be more abstracted by the MVC-Pattern
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

    // private JComboBox comboSRS;

    private List<JCheckBox> checkBoxListFormat;

    private List<JCheckBox> checkBoxSRSList;

    private List<String> srsList;

    private List<List<String>> listOfSrsList;

    private WMSClient111 wmsClient;

    private URL url;

    private JLabel labelC;

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
        this.srsList = new ArrayList<String>();
        this.listOfSrsList = new ArrayList<List<String>>();

    }

    public List<String> getCheckBoxListLayerText() {
        List<String> selectedLayer = new ArrayList<String>();
        for ( JCheckBox check : checkBoxListLayer ) {
            if ( check.isSelected() ) {
                selectedLayer.add( check.getText() );
            }
        }
        if ( selectedLayer.size() == 0 ) {
            removeAllElementsFromGroup( groupSRS );
            labelC.setVisible( false );
        }

        return selectedLayer;
    }

    public List<String> getCheckBoxListLayerName() {
        List<String> selectedLayer = new ArrayList<String>();
        for ( JCheckBox check : checkBoxListLayer ) {
            if ( check.isSelected() ) {
                selectedLayer.add( check.getName() );
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
            JCheckBox box = new JCheckBox( s );
            box.setName( s );
            checkBoxListLayer.add( box );
        }
        if ( checkBoxListLayer != null ) {
            for ( JCheckBox check : checkBoxListLayer ) {
                this.getPanel().add( check );
            }
        }

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
        labelC = new JLabel( "Coordinatesystems" );
        this.getPanel().add( labelC );
        labelC.setVisible( false );
    }

    /**
     * 
     * @param layerName
     *            of which the coordinateSystems should be taken to fill the list of srs.
     */
    public void fillSRSList( String layerName ) {
        labelC.setVisible( true );

        List<String> srsListTemp = wmsClient.getCoordinateSystems( layerName );
        listOfSrsList.add( srsListTemp );
        this.checkBoxSRSList = new ArrayList<JCheckBox>();

        if ( listOfSrsList.size() > 1 ) {
            for ( List<String> l : listOfSrsList ) {
                srsList = mergeLists( srsList, l );
            }
        } else {
            srsList.addAll( srsListTemp );
        }

        for ( String s : srsList ) {
            checkBoxSRSList.add( new JCheckBox( s ) );
        }
        removeAllElementsFromGroup( groupSRS );
        if ( checkBoxSRSList != null ) {
            for ( JCheckBox check : checkBoxSRSList ) {

                groupSRS.add( check );
                this.getPanel().add( check );

            }
        }
        // comboSRS.removeAllItems();
        // for ( String s : srsList ) {
        // comboSRS.addItem( s );
        // }

    }

    private void removeAllElementsFromGroup( ButtonGroup group ) {
        while ( group.getElements().hasMoreElements() ) {
            AbstractButton b = group.getElements().nextElement();
            this.getPanel().remove( b );
            group.remove( b );
        }
        this.getPanel().revalidate();
    }

    /**
     * Merges two Collections by checking the contained elements to be in both of the collections.
     * 
     * @param <E>
     * @param c1
     *            , not <Code>null</Code>.
     * @param c2
     *            , not <Code>null</Code>.
     * @return a typeGeneric list.
     */
    private <E> List<E> mergeLists( Collection<? extends E> c1, Collection<? extends E> c2 ) {
        List<E> list = new ArrayList<E>();
        for ( E e : c2 ) {
            if ( c1.contains( e ) ) {
                list.add( e );
            }
        }
        return list;
    }

    public CRS getCheckBoxSRS() {
        CRS crs = null;
        if ( checkBoxSRSList != null ) {
            for ( JCheckBox check : checkBoxSRSList ) {

                if ( check.isSelected() ) {
                    crs = new CRS( check.getText() );
                    break;
                }
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

    public ButtonGroup getGroupSRS() {
        return groupSRS;
    }

    // public JComboBox getComboSRS() {
    // return comboSRS;
    // }
    //
    // /**
    // *
    // * @return the comboBox as a CRS, can be <Code>null</Code>.
    // */
    // public CRS getComboCRS() {
    // CRS crs = null;
    //
    // // crs = new CRS( comboSRS.getSelectedItem().toString() );
    //
    // return crs;
    // }
    //
    // public void setComboSRS( JComboBox comboSRS ) {
    // this.comboSRS = comboSRS;
    // }
    //
    // public void addComboActionListener( ActionListener l ) {
    // comboSRS.addActionListener( l );
    // }

    public void addCheckBoxListener( ActionListener l ) {
        for ( JCheckBox c : checkBoxListLayer ) {
            c.addActionListener( l );
        }
    }

    public WMSClient111 getWmsClient() {
        return wmsClient;
    }

}
