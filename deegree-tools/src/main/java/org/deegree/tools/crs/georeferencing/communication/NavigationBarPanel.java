package org.deegree.tools.crs.georeferencing.communication;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class NavigationBarPanel extends JPanel {

    JCheckBox button = new JCheckBox( "Horizontal Referencing" );

    public NavigationBarPanel() {
        this.setName( "NavigationBarPanel" );
        this.setLayout( new FlowLayout( 10 ) );
        this.add( button );
        this.repaint();
    }

    public void addHorizontalRefListener( ActionListener c ) {
        button.addActionListener( c );

    }

}
