import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.LiteralOutput;

public class Process42 implements Processlet {

    @Override
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {
        LiteralOutput output = (LiteralOutput) out.getParameter( "Answer" );
        output.setValue( "42" );
    }

    @Override
    public void init() {
        // nothing to initialize
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }
}

