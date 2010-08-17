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
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

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
public class PointTableFrame extends JFrame implements GUIConstants {

    public final static String BUTTON_DELETE_SELECTED = "Delete selected";

    public final static String BUTTON_DELETE_ALL = "Delete all";

    public final static String COMPUTE_BUTTON_NAME = "Compute Transformation";

    private JButton deleteSingleButton = new JButton( BUTTON_DELETE_SELECTED );

    private JButton deleteAllButton = new JButton( BUTTON_DELETE_ALL );

    private JButton computeTransform = new JButton( COMPUTE_BUTTON_NAME );

    private String[] columnNames = { "X-Ref", "Y-Ref", "X-Building", "Y-Building", "X-Residual", "Y-Residual" };

    private DefaultTableModel model = new DefaultTableModel();

    private JTable table = new JTable( model );

    private JPanel buttonPanel = new JPanel();

    private JPanel tablePanel = new JPanel();

    private CheckBoxList checkbox;

    private JPanel leftPanel = new JPanel( new BorderLayout() );

    private JPanel rightPanel = new JPanel( new BorderLayout() );

    private JPanel rightUpperPanel = new JPanel( new FlowLayout() );

    private JPanel rightLowerPanel = new JPanel( new BorderLayout() );

    private JPanel rightNorthPanel = new JPanel( new BorderLayout() );

    private JPanel rightCenterPanel = new JPanel();

    // private JPanel rightCenterWest = new JPanel();
    //
    // private JPanel rightCenterCenter = new JPanel();

    private JPanel rightSouthPanel = new JPanel( new FlowLayout() );

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
        leftPanel.add( buttonPanel, BorderLayout.NORTH );

        tablePanel.setLayout( new BorderLayout() );
        tablePanel.add( table.getTableHeader(), BorderLayout.NORTH );
        tablePanel.add( table, BorderLayout.CENTER );
        tableScrollPane.setViewportView( tablePanel );
        leftPanel.add( tableScrollPane, BorderLayout.CENTER );

        Label label = new Label( "Transformationmethods" );
        label.setFont( new Font( "Helvetica", Font.BOLD, 16 ) );
        rightUpperPanel.add( label );

        // Label rightLowerDummy = new Label( " " );
        // rightLowerDummy.setFont( new Font( "Helvetica", Font.BOLD, 40 ) );
        // rightNorthPanel.add( rightLowerDummy );

        // Label rightLowerWestDummy = new Label( "    " );
        // rightLowerWestDummy.setFont( new Font( "Helvetica", Font.BOLD, 40 ) );
        // rightCenterWest.add( rightLowerWestDummy );

        String[] sArray = new String[] { MENUITEM_TRANS_POLYNOM_FIRST, MENUITEM_TRANS_HELMERT };
        checkbox = new CheckBoxList( sArray );

        // rightCenterPanel.add( rightCenterWest, BorderLayout.WEST );
        // rightCenterPanel.add( rightCenterCenter, BorderLayout.CENTER );

        rightCenterPanel.setBorder( BorderFactory.createLineBorder( Color.black ) );

        rightCenterPanel.add( checkbox, BorderLayout.CENTER );
        rightSouthPanel.add( computeTransform );

        rightLowerPanel.add( rightNorthPanel, BorderLayout.NORTH );
        rightLowerPanel.add( rightCenterPanel, BorderLayout.CENTER );
        rightLowerPanel.add( rightSouthPanel, BorderLayout.SOUTH );

        rightPanel.add( rightUpperPanel, BorderLayout.NORTH );
        rightPanel.add( rightLowerPanel, BorderLayout.CENTER );
        rightPanel.setBorder( BorderFactory.createLineBorder( Color.black ) );

        this.getContentPane().add( leftPanel, BorderLayout.CENTER );
        this.getContentPane().add( rightPanel, BorderLayout.EAST );
        setSize( 800, 300 );
        setVisible( true );
        toFront();
        setAlwaysOnTop( true );
        pack();
    }

    public void addHorizontalRefListener( ActionListener c ) {
        deleteSingleButton.addActionListener( c );
        deleteAllButton.addActionListener( c );
        computeTransform.addActionListener( c );

    }

    public void addTableModelListener( TableModelListener c ) {
        model.addTableModelListener( c );

    }

    public void setCoords( AbstractGRPoint point ) {
        if ( model.getRowCount() == 0 ) {
            addRow();
        }
        Object[] rowData = new Object[] { point.x, point.y };
        int rowCount = model.getRowCount();
        switch ( point.getPointType() ) {

        case GeoreferencedPoint:
            model.setValueAt( rowData[0], rowCount - 1, 0 );
            model.setValueAt( rowData[1], rowCount - 1, 1 );
            break;
        case FootprintPoint:
            model.setValueAt( rowData[0], rowCount - 1, 2 );
            model.setValueAt( rowData[1], rowCount - 1, 3 );
            break;

        }

    }

    /**
     * Adds a row to the view with empty values.
     */
    public void addRow() {

        Object[] emptyRow = new Object[] {};
        model.addRow( emptyRow );

    }

    /**
     * Removes the specified row from the view.
     * 
     * @param rowNumber
     */
    public void removeRow( int rowNumber ) {
        model.removeRow( rowNumber );
    }

    /**
     * Removes all rows of the table.
     */
    public void removeAllRows() {
        int length = model.getRowCount();
        for ( int row = 0; row < length; row++ ) {
            this.removeRow( 0 );
        }
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public JTable getTable() {
        return table;
    }

    /**
     * Adds the actionListener to the visible components to interact with the user.
     * 
     * @param e
     */
    public void addListeners( ActionListener e ) {
        checkbox.addCheckboxListener( e );

    }

    /**
     * Sets everything that is needed to handle userinteraction with the checkboxes in the transformationMenu.
     * 
     * @param selectedCheckbox
     *            the checkbox that has been selected by the user.
     */
    public void activateTransformationCheckbox( JCheckBox selectedCheckbox ) {
        this.checkbox.selectThisCheckbox( selectedCheckbox );

    }

    public CheckBoxList getCheckbox() {
        return checkbox;
    }

}