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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.deegree.tools.crs.georeferencing.model.RowColumn;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;

/**
 * 
 * Table to visualize the referenced points that are identified.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PointTableFrame extends JFrame {

    public final static String BUTTON_DELETE_SELECTED = "Delete selected";

    public final static String BUTTON_DELETE_ALL = "Delete all";

    private JButton deleteSingleButton = new JButton( BUTTON_DELETE_SELECTED );

    private JButton deleteAllButton = new JButton( BUTTON_DELETE_ALL );

    private String[] columnNames = { "X-Ref", "Y-Ref", "X-Building", "Y-Building", "X-Residual", "Y-Residual" };

    private DefaultTableModel model = new DefaultTableModel();

    private JTable table = new JTable( model );

    private JPanel buttonPanel = new JPanel();

    private JPanel tablePanel = new JPanel();

    private JScrollPane tableScrollPane = new JScrollPane();

    public PointTableFrame() {
        for ( String s : columnNames ) {
            model.addColumn( s );
        }
        table.setName( "PointTable" );
        this.setLayout( new BorderLayout( 5, 5 ) );
        buttonPanel.setLayout( new FlowLayout() );
        buttonPanel.setBorder( BorderFactory.createLineBorder( Color.black ) );
        buttonPanel.add( deleteSingleButton, BorderLayout.LINE_START );
        buttonPanel.add( deleteAllButton, BorderLayout.LINE_END );
        this.getContentPane().add( buttonPanel, BorderLayout.NORTH );

        tablePanel.setLayout( new BorderLayout() );
        tablePanel.add( table.getTableHeader(), BorderLayout.NORTH );
        tablePanel.add( table, BorderLayout.CENTER );
        this.getContentPane().add( tableScrollPane );
        tableScrollPane.setViewportView( tablePanel );
        setSize( 600, 300 );
        setVisible( true );
        toFront();
        setAlwaysOnTop( true );
    }

    public void addActionButtonListener( ActionListener c ) {
        deleteSingleButton.addActionListener( c );
        deleteAllButton.addActionListener( c );

    }

    public void addTableModelListener( TableModelListener c ) {
        model.addTableModelListener( c );

    }

    public RowColumn setCoords( AbstractGRPoint point ) {
        if ( model.getRowCount() == 0 ) {
            addRow();
        }
        Object[] rowData = new Object[] { point.x, point.y };
        int rowCount = model.getRowCount();
        switch ( point.getPointType() ) {

        case GeoreferencedPoint:
            int row = rowCount - 1;
            int columnX = 0;
            int columnY = 1;
            model.setValueAt( rowData[0], row, columnX );
            model.setValueAt( rowData[1], row, columnY );

            return new RowColumn( row, columnX, columnY );
        case FootprintPoint:

            int rowF = rowCount - 1;
            int columnXF = 2;
            int columnYF = 3;
            model.setValueAt( rowData[0], rowCount - 1, 2 );
            model.setValueAt( rowData[1], rowCount - 1, 3 );
            return new RowColumn( rowF, columnXF, columnYF );

        }

        return null;

    }

    /**
     * Adds a row to the view with empty values.
     */
    public void addRow() {

        Object[] emptyRow = new Object[] {};
        model.addRow( emptyRow );

    }

    /**
     * Removes the specified rows from the view. It removed one element from the dataVector of the underlying tableModel
     * and after that it fires a dataChanged event to notify all the listeners only once. That is better practice
     * instead of calling the removeRow() method which notify all listeners every time one row is deleted.
     * 
     * @param rowsNumber
     *            , not <Code>null</Code>.
     */
    public void removeRow( int[] rowsNumber ) {

        int counter = 0;
        for ( int i : rowsNumber ) {
            model.getDataVector().remove( i - counter );
            counter++;
        }
        model.fireTableDataChanged();
    }

    /**
     * Removes all rows of the table.
     */
    public void removeAllRows() {
        model.getDataVector().removeAllElements();
        model.fireTableDataChanged();
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public JTable getTable() {
        return table;
    }

}