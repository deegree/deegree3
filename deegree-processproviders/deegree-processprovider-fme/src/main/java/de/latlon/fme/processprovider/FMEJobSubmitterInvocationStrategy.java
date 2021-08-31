package de.latlon.fme.processprovider;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexOutputDefinition;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;

/**
 * Default {@link FMEInvocationStrategy}.
 * 
 * @author <a href="schneider@occamlabs.de">Markus Schneider</a>
 */
class FMEJobSubmitterInvocationStrategy implements FMEInvocationStrategy {

    @Override
    public OutputParameters getOutputParameters() {
        ComplexOutputDefinition response = new ComplexOutputDefinition();
        org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
        id.setValue("FMEResponse");
        LanguageStringType title = new LanguageStringType();
        title.setValue("Response from FME (Job Submitter Service)");
        response.setTitle(title);
        ComplexFormatType fmtType = new ComplexFormatType();
        fmtType.setMimeType("application/xml");
        response.setDefaultFormat(fmtType);
        response.setIdentifier(id);
        OutputParameters parameters = new OutputParameters();
        parameters.getProcessOutput().add(
                new JAXBElement<ComplexOutputDefinition>(new QName(""), ComplexOutputDefinition.class, response));
        return parameters;
    }
}
