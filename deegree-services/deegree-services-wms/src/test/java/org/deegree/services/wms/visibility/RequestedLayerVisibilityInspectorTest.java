package org.deegree.services.wms.visibility;

import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.services.jaxb.wms.VisibilityInspectorType;
import org.deegree.workspace.Workspace;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class RequestedLayerVisibilityInspectorTest {

    @Test
    public void testIsVisible_InspectorForOneCategoryLayerIdentifier()
                            throws Exception {
        List<VisibilityInspectorType> visibilityInspectorTypes = createConfigurationWithOneCategoryLayerIdentifier( "ReqestedLayerToCheck" );
        Workspace workspace = mockWorkspace();
        RequestedLayerVisibilityInspector inspector = new RequestedLayerVisibilityInspector( visibilityInspectorTypes,
                                                                                             workspace );

        assertTrue( inspector.isVisible( "ReqestedLayerToCheck", createLayerMetadata( "IAmVisible" ) ) );
        assertFalse( inspector.isVisible( "ReqestedLayerToCheck", createLayerMetadata( "NotVisible" ) ) );
        assertTrue( inspector.isVisible( "ReqestedLayer", createLayerMetadata( "IAmVisible" ) ) );
        assertTrue( inspector.isVisible( "ReqestedLayer", createLayerMetadata( "NotVisible" ) ) );
    }

    @Test
    public void testIsVisible_InspectorForAllRequestedLayer()
                            throws Exception {
        List<VisibilityInspectorType> visibilityInspectorTypes = createConfigurationWithInspectorForAll();
        Workspace workspace = mockWorkspace();
        RequestedLayerVisibilityInspector inspector = new RequestedLayerVisibilityInspector( visibilityInspectorTypes,
                                                                                             workspace );

        assertTrue( inspector.isVisible( "ReqestedLayerToCheck", createLayerMetadata( "IAmVisible" ) ) );
        assertFalse( inspector.isVisible( "ReqestedLayerToCheck", createLayerMetadata( "NotVisible" ) ) );
        assertTrue( inspector.isVisible( "ReqestedLayer", createLayerMetadata( "IAmVisible" ) ) );
        assertFalse( inspector.isVisible( "ReqestedLayer", createLayerMetadata( "NotVisible" ) ) );
    }

    @Test
    public void testIsVisible_TwoInspectorsOneForAllRequestedLayerOneForTwoCategoryLayerIdentifier()
                            throws Exception {
        List<VisibilityInspectorType> visibilityInspectorTypes = createConfigurationWithMultipleInspectors( "ReqestedLayerToCheck" );
        Workspace workspace = mockWorkspace();
        RequestedLayerVisibilityInspector inspector = new RequestedLayerVisibilityInspector( visibilityInspectorTypes,
                                                                                             workspace );

        assertTrue( inspector.isVisible( "ReqestedLayerToCheck", createLayerMetadata( "IAmVisible" ) ) );
        assertFalse( inspector.isVisible( "ReqestedLayerToCheck", createLayerMetadata( "NotVisible" ) ) );
        assertTrue( inspector.isVisible( "ReqestedLayer", createLayerMetadata( "IAmVisible" ) ) );
        assertFalse( inspector.isVisible( "ReqestedLayer", createLayerMetadata( "NotVisible" ) ) );
    }

    private LayerMetadata createLayerMetadata( String layerName ) {
        return new LayerMetadata( layerName, null, null );
    }

    private List<VisibilityInspectorType> createConfigurationWithOneCategoryLayerIdentifier( String... categoryLayerIdentifier ) {
        return createInspectors( categoryLayerIdentifier );
    }

    private List<VisibilityInspectorType> createConfigurationWithInspectorForAll() {
        return createInspectors();
    }

    private List<VisibilityInspectorType> createConfigurationWithMultipleInspectors( String... categoryLayerIdentifier ) {
        List<VisibilityInspectorType> inspectorForAll = createInspectors();
        List<VisibilityInspectorType> inspector = createInspectors( categoryLayerIdentifier );
        inspectorForAll.addAll( inspector );
        return inspectorForAll;
    }

    private List<VisibilityInspectorType> createInspectors( String... categoryLayerIdentifier ) {
        List<VisibilityInspectorType> inspectorTypes = new ArrayList<VisibilityInspectorType>();
        VisibilityInspectorType inspectorType = new VisibilityInspectorType();
        inspectorType.setJavaClass( LayerVisibilityInspectorTestImpl.class.getCanonicalName() );
        if ( categoryLayerIdentifier != null && categoryLayerIdentifier.length > 0 )
            inspectorType.getCategoryLayerIdentifier().addAll( Arrays.asList( categoryLayerIdentifier ) );
        inspectorTypes.add( inspectorType );
        return inspectorTypes;
    }

    private Workspace mockWorkspace() {
        Workspace workspace = mock( Workspace.class );
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        when( workspace.getModuleClassLoader() ).thenReturn( classLoader );
        return workspace;
    }

}