/*
 * Copyright lat/lon GmbH 2011
 * All rights reserved.
 */
package de.latlon.fme.processprovider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition.InputParameters;
import org.deegree.services.wps.DefaultExceptionCustomizer;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.WPSProcess;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: goerke $
 * 
 * @version $Revision: 84 $, $Date: 2011-05-30 10:31:58 +0200 (Mo, 30. Mai 2011)
 */
public class FMEProcess implements WPSProcess {

    private final ProcessDefinition definition;

    private final String fmeWorkspace;

    private final String repo;

    private final FMEProcesslet process;

    public FMEProcess(InputParameters inputs, FMEInvocationStrategy invocationStrategy, String fmeWorkspace,
            String title, String repo, String wsDescr, String restUrl, String uri, String tokenUrl, String token,
            Map<String, String> tokenmap, boolean idQualified) throws MalformedURLException, IOException {
        this.fmeWorkspace = fmeWorkspace;
        this.repo = repo;
        definition = createDefinition(inputs, invocationStrategy, fmeWorkspace, title, repo, wsDescr, idQualified);
        process = new FMEProcesslet(restUrl, tokenUrl, tokenmap, repo, fmeWorkspace, invocationStrategy);
    }

    private ProcessDefinition createDefinition(InputParameters inputs, FMEInvocationStrategy invocationStrategy,
            String fmeWorkspace, String fmeTitle, String repo, String wsDescr, boolean idQualified) {
        ProcessDefinition definition = new ProcessDefinition();
        org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
        if (idQualified) {
            id.setCodeSpace(repo);
        }
        id.setValue(fmeWorkspace);
        definition.setIdentifier(id);
        definition.setProcessVersion("0.0.1");
        definition.setStatusSupported(false);
        definition.setStoreSupported(false);
        LanguageStringType title = new LanguageStringType();
        title.setValue(fmeTitle);
        definition.setTitle(title);
        LanguageStringType abstract_ = new LanguageStringType();
        abstract_.setValue(wsDescr);
        definition.setAbstract(abstract_);
        definition.setInputParameters(inputs);
        definition.setOutputParameters(invocationStrategy.getOutputParameters());
        definition.setStoreSupported(true);
        return definition;
    }

    public ProcessDefinition getDescription() {
        return definition;
    }

    public ExceptionCustomizer getExceptionCustomizer() {
        return new DefaultExceptionCustomizer(new CodeType(fmeWorkspace, repo));
    }

    public Processlet getProcesslet() {
        return process;
    }
}
