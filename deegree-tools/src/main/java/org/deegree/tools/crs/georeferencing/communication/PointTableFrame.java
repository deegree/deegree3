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
import java.util.Vector;

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

    public static final String SAVE_POINTTABLE = "Save";

    public static final String LOAD_POINTTABLE = "Load";

    public final static String BUTTON_DELETE_SELECTED_NAME = "Delete selected name";

    public final static String BUTTON_DELETE_ALL_NAME = "Delete all name";

    public static final String SAVE_POINTTABLE_NAME = "Save name";

    public static final String LOAD_POINTTABLE_NAME = "Load name";

    private JButton deleteSingleButton = new JButton( BUTTON_DELETE_SELECTED );

    private JButton deleteAllButton = new JButton( BUTTON_DELETE_ALL );

    private JButton saveButton = new JButton( SAVE_POINTTABLE );

    private JButton loadButton = new JButton( LOAD_POINTTABLE );

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

        deleteAllButton.setName( BUTTON_DELETE_ALL_NAME );
        deleteSingleButton.setName( BUTTON_DELETE_SELECTED_NAME );
        saveButton.setName( SAVE_POINTTABLE_NAME );
        loadButton.setName( LOAD_POINTTABLE_NAME );

        buttonPanel.add( deleteSingleButton, BorderLayout.LINE_START );
        buttonPanel.add( deleteAllButton, BorderLayout.LINE_START );
        buttonPanel.add( saveButton, BorderLayout.LINE_START );
        buttonPanel.add( loadButton, BorderLayout.LINE_START );
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
        saveButton.addActionListener( c );
        loadButton.addActionListener( c );

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
        int row = rowCount - 1;
        int colX = 0;
        int colY = 0;
        switch ( point.getPointType() ) {

        case GeoreferencedPoint:

            colX = 0;
            colY = 1;
            model.setValueAt( rowData[0], row, colX );
            model.setValueAt( rowData[1], row, colY );

            break;
        case FootprintPoint:

            colX = 2;
            colY = 3;
            model.setValueAt( rowData[0], row, colX );
            model.setValueAt( rowData[1], row, colY );
            break;
        case ResidualPoint:
            colX = 4;
            colY = 5;
            model.setValueAt( "", row, colX );
            model.setValueAt( "", row, colY );
        }

        return new RowColumn( row, colX, colY );

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

    public String[] getColumnNames() {
        return columnNames;
    }

    public Vector<String> getColumnNamesAsVector() {
        Vector<String> v = new Vector<String>();
        for ( String s : columnNames ) {
            v.add( s );
        }
        return v;
    }

}