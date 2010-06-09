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
package org.deegree.tools.crs.georeferencing.communication;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.deegree.tools.crs.georeferencing.model.AbstractPoint;

/**
 * 
 * Table to visualize the referenced points that are identified.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PointTablePanel extends JPanel {

    public final static String BUTTON_DELETE_SELECTED = "Delete selected";

    public final static String BUTTON_DELETE_ALL = "Delete all";

    private JButton deleteSingleButton = new JButton( BUTTON_DELETE_SELECTED );

    private JButton deleteAllButton = new JButton( BUTTON_DELETE_ALL );

    private String[] columnNames = { "referenced coordinates", "building coordinates" };

    private DefaultTableModel model = new DefaultTableModel();

    private JTable table = new JTable( model );

    private JPanel buttonPanel = new JPanel();

    private JPanel tablePanel = new JPanel();

    public PointTablePanel() {
        model.addColumn( columnNames[0] );
        model.addColumn( columnNames[1] );
        table.setName( "PointTable" );
        this.setLayout( new BorderLayout( 5, 5 ) );
        buttonPanel.setLayout( new FlowLayout() );
        buttonPanel.setBorder( BorderFactory.createLineBorder( Color.black ) );
        buttonPanel.add( deleteSingleButton, BorderLayout.LINE_START );
        buttonPanel.add( deleteAllButton, BorderLayout.LINE_END );
        this.add( buttonPanel, BorderLayout.NORTH );

        // table.setPreferredSize( new Dimension( this.getWidth(), this.getHeight() ) );
        tablePanel.setLayout( new BorderLayout() );
        tablePanel.add( table.getTableHeader(), BorderLayout.NORTH );
        tablePanel.add( table, BorderLayout.CENTER );
        this.add( tablePanel, BorderLayout.CENTER );

    }

    public void addHorizontalRefListener( ActionListener c ) {
        deleteSingleButton.addActionListener( c );
        deleteAllButton.addActionListener( c );

    }

    public void addTableModelListener( TableModelListener c ) {
        model.addTableModelListener( c );

    }

    public void setCoords( AbstractPoint point ) {
        if ( model.getRowCount() == 0 ) {
            Object[] emptyRow = new Object[] {};
            model.addRow( emptyRow );
        }
        Object[] rowData = null;
        switch ( point.getPointType() ) {

        case FootprintPoint:
            rowData = new Object[] { point };

            model.setValueAt( rowData[0], 0, 1 );
            break;
        case GeoreferencedPoint:

            rowData = new Object[] { point };

            model.setValueAt( rowData[0], 0, 0 );
            break;
        }

    }

}