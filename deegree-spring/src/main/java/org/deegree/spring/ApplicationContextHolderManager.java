package org.deegree.spring;

import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;

public class ApplicationContextHolderManager extends DefaultResourceManager<ApplicationContextHolder> {

    public ApplicationContextHolderManager() {
        super( new DefaultResourceManagerMetadata<ApplicationContextHolder>( ApplicationContextHolderProvider.class, 
                                "spring application context holders", "spring") );
    }
}
