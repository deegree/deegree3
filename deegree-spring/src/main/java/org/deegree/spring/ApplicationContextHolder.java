package org.deegree.spring;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

import org.springframework.context.ApplicationContext;

public class ApplicationContextHolder implements Resource {

    private final ResourceMetadata<ApplicationContextHolder> metadata;

    private final ApplicationContext applicationContext;

    public ApplicationContextHolder( final ResourceMetadata<ApplicationContextHolder> metadata,
                                     final ApplicationContext applicationContext ) {
        this.metadata = metadata;
        this.applicationContext = applicationContext;
    }

    @Override
    public ResourceMetadata<ApplicationContextHolder> getMetadata() {
        return metadata;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
