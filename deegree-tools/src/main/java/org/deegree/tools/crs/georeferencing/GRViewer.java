package org.deegree.tools.crs.georeferencing;

import java.io.IOException;

public class GRViewer {

    /**
     * @param args
     * @throws IOException
     */
    public static void main( String[] args )
                            throws IOException {
        // GRViewerGUI gui = new GRViewerGUI();
        GUItest g = new GUItest();

        Scene2D scene2d = new Scene2DImplWMStest();

        new Controller( g, scene2d );

        g.setVisible( true );

    }

}
