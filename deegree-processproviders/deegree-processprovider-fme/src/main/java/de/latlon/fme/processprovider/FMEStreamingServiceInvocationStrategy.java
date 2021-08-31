package de.latlon.fme.processprovider;

import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;

/**
 * {@link FMEInvocationStrategy} based on FME Streaming Service.
 * 
 * @author <a href="schneider@occamlabs.de">Markus Schneider</a>
 */
class FMEStreamingServiceInvocationStrategy implements FMEInvocationStrategy {

    private final OutputParameters outputParameters;

    private final String outputFormat;

    private final String outputName;

    FMEStreamingServiceInvocationStrategy(OutputParameters outputParameters, String outputFormat, String outputName) {
        this.outputParameters = outputParameters;
        this.outputFormat = outputFormat;
        this.outputName = outputName;
    }

    public OutputParameters getOutputParameters() {
        return outputParameters;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getOutputName() {
        return outputName;
    }
}
