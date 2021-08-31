package de.latlon.fme.processprovider;

import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;

interface FMEInvocationStrategy {

    OutputParameters getOutputParameters();

}
