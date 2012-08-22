//$HeadURL$
/*----------------------------------------
 Coyright 2003 IDgis BV

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ----------------------------------------*/

package org.deegree.tools.shape;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.dbaseapi.DBaseIndex;
import org.deegree.io.rtree.HyperBoundingBox;
import org.deegree.io.rtree.HyperPoint;
import org.deegree.io.rtree.RTree;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

public class IndexFrame extends JFrame implements ActionListener, ChangeListener {

    private static final ILogger LOG = LoggerFactory.getLogger( IndexFrame.class );

    protected JCheckBox geometryCheckBox;

    protected JCheckBox[] checkboxes;

    protected ShapeFile shapeFile;

    protected String[] properties;

    protected JButton cancel;

    protected Indexing indexing = null;

    protected String fileName;

    protected boolean hasGeometry;

    protected boolean[] hasIndex;

    protected JCheckBox[] uniqueBoxes;

    class Indexing extends Thread {
        boolean stop = false;

        IndexFrame frame;

        Indexing( IndexFrame frame ) {
            this.frame = frame;
        }

        public void run() {
            Container container = getContentPane();
            container.removeAll();
            container.setLayout( new GridLayout( 3, 1 ) );

            int features = shapeFile.getRecordNum();
            container.add( new JLabel( "Indexing..." ) );
            JProgressBar progressBar = new JProgressBar( 1, features );
            progressBar.setStringPainted( true );
            container.add( progressBar );
            cancel = new JButton( "Cancel" );
            cancel.addActionListener( frame );
            container.add( cancel );
            pack();

            boolean geometry = false;
            DBaseIndex[] index = new DBaseIndex[properties.length];
            RTree rtree = null;

            try {
                String[] dataTypes = shapeFile.getDataTypes();
                int[] lengths = shapeFile.getDataLengths();

                if ( geometryCheckBox.isSelected() && !hasGeometry ) {
                    geometry = true;
                    rtree = new RTree( 2, 11, fileName + ".rti" );
                }

                boolean indexes = false;
                for ( int i = 0; i < index.length; i++ ) {
                    if ( checkboxes[i].isSelected() && !hasIndex[i] ) {
                        index[i] = DBaseIndex.createIndex(
                                                           fileName + "$" + properties[i],
                                                           properties[i],
                                                           lengths[i],
                                                           uniqueBoxes[i].isSelected(),
                                                           ( dataTypes[i].equalsIgnoreCase( "N" )
                                                             || dataTypes[i].equalsIgnoreCase( "I" ) || dataTypes[i].equalsIgnoreCase( "F" ) ) );
                        indexes = true;
                    } else
                        index[i] = null;
                }

                if ( geometry || indexes ) {
                    for ( int i = 1; i < features + 1; i++ ) {
                        Feature feature = shapeFile.getFeatureByRecNo( i );

                        if ( geometry ) {
                            Geometry[] geometries = feature.getGeometryPropertyValues();
                            if ( geometries.length == 0 ) {
                                LOG.logInfo( "no geometries at recno" + i );
                                continue;
                            }
                            Envelope envelope = null;
                            // TODO: deal with more than one geometry; handle geometry=null (allowed
                            // in shapefile)
                            envelope = ( feature.getDefaultGeometryPropertyValue() ).getEnvelope();
                            if ( envelope == null ) { // assume a Point-geometry
                                Point pnt = (Point) geometries[0];
                                envelope = GeometryFactory.createEnvelope( pnt.getX(), pnt.getY(), pnt.getX(),
                                                                           pnt.getY(), null );
                            }
                            HyperBoundingBox box = new HyperBoundingBox(
                                                                         new HyperPoint( envelope.getMin().getAsArray() ),
                                                                         new HyperPoint( envelope.getMax().getAsArray() ) );
                            rtree.insert( new Integer( i ), box );
                        }

                        for ( int j = 0; j < index.length; j++ ) {
                            if ( index[j] != null ) {
                                QualifiedName qn = new QualifiedName( properties[j] );
                                index[j].addKey( (Comparable) feature.getDefaultProperty( qn ), i );
                            }
                        }

                        progressBar.setValue( i );

                        synchronized ( this ) {
                            if ( stop ) {
                                shapeFile.close();
                                if ( geometry ) {
                                    rtree.close();
                                    new File( fileName + ".rti" ).delete();
                                }
                                for ( int j = 0; j < index.length; j++ ) {
                                    if ( index[j] != null ) {
                                        index[j].close();
                                        new File( fileName + "$" + properties[j] + ".ndx" ).delete();
                                    }
                                }
                                System.exit( 3 );
                            }
                        }
                    }
                }

                try {
                    if ( geometry ) {
                        rtree.close();
                    }
                    shapeFile.close();

                    for ( int i = 0; i < index.length; i++ )
                        if ( index[i] != null )
                            index[i].close();
                } catch ( Exception e ) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog( frame, e );
                    System.exit( 1 );
                }

                if ( !geometryCheckBox.isSelected() && hasGeometry ) {
                    new File( fileName + ".rti" ).delete();
                }

                for ( int i = 0; i < index.length; i++ ) {
                    if ( !checkboxes[i].isSelected() && hasIndex[i] ) {
                        new File( fileName + "$" + properties[i] + ".ndx" ).delete();
                    }
                }

                System.exit( 0 );
            } catch ( Exception ex ) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog( frame, ex );
                System.exit( 1 );
            }
        }

        public void stopIndexing() {
            synchronized ( this ) {
                stop = true;
            }
        }
    }

    public IndexFrame( File file ) throws Exception {
        super( "Deegree" );
        fileName = file.getPath();
        fileName = fileName.substring( 0, fileName.length() - 4 );
        shapeFile = new ShapeFile( fileName );
        properties = shapeFile.getProperties();

        Container container = getContentPane();
        container.setLayout( new BorderLayout() );
        int size = properties.length;
        JPanel panel = new JPanel( new GridLayout( 10 + 2, 2 ) );

        panel.add( new JLabel( "Attributes" ) );
        panel.add( new JLabel( "Unique" ) );

        hasGeometry = shapeFile.hasRTreeIndex();
        hasIndex = new boolean[size];
        for ( int i = 0; i < size; i++ )
            hasIndex[i] = shapeFile.hasDBaseIndex( properties[i] );

        checkboxes = new JCheckBox[size];
        uniqueBoxes = new JCheckBox[size];

        geometryCheckBox = new JCheckBox( "Geometry" );
        geometryCheckBox.setSelected( hasGeometry );

        panel.add( geometryCheckBox );
        panel.add( new JCheckBox( "" ) ).setEnabled( false );

        for ( int i = 0; i < checkboxes.length; i++ ) {
            checkboxes[i] = new JCheckBox( properties[i] );
            checkboxes[i].setSelected( hasIndex[i] );
            if ( i < 10 ) {
                panel.add( checkboxes[i] );

                uniqueBoxes[i] = (JCheckBox) panel.add( new JCheckBox( "" ) );
                if ( hasIndex[i] ) {
                    uniqueBoxes[i].setSelected( shapeFile.isUnique( properties[i] ) );
                } else {
                    checkboxes[i].addChangeListener( this );
                }
                uniqueBoxes[i].setEnabled( false );
            }
        }

        JButton start = new JButton( "Start indexing" );
        start.addActionListener( this );
        container.add( "South", start );
        container.add( "Center", panel );

        setBounds( 100, 100, 100, 100 );
        pack();

        setResizable( true );
    }

    /**
     * @param e
     */
    public void actionPerformed( ActionEvent e ) {
        if ( e.getSource() == cancel ) {
            if ( indexing != null ) {
                if ( JOptionPane.showConfirmDialog( this, "Cancel indexing?", "Question", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION )
                    indexing.stopIndexing();
            }
        } else {
            if ( hasGeometry != geometryCheckBox.isSelected() ) {
                indexing = new Indexing( this );
                indexing.start();
                return;
            }

            for ( int i = 0; i < hasIndex.length; i++ ) {
                if ( hasIndex[i] != checkboxes[i].isSelected() ) {
                    indexing = new Indexing( this );
                    indexing.start();
                    return;
                }
            }

            JOptionPane.showMessageDialog( this, "Nothing changed" );
        }
    }

    /**
     * @param e
     */
    public void stateChanged( ChangeEvent e ) {
        JCheckBox checkbox = (JCheckBox) e.getSource();

        for ( int i = 0; i < checkboxes.length; i++ ) {
            if ( checkbox == checkboxes[i] ) {
                uniqueBoxes[i].setEnabled( checkbox.isSelected() );
                if ( !checkbox.isSelected() ) {
                    uniqueBoxes[i].setSelected( false );
                }
                break;
            }
        }
    }
}