import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.LiteralOutput;

public class AdditionProcesslet implements Processlet {

    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {
        int summandA = Integer.parseInt( ( (LiteralInput) in.getParameter( "SummandA" ) ).getValue() );
        int summandB = Integer.parseInt( ( (LiteralInput) in.getParameter( "SummandB" ) ).getValue() );
        int sum = summandA + summandB;

        LiteralOutput output = (LiteralOutput) out.getParameter( "Sum" );
        output.setValue( "" + sum );
    }

    public void destroy() {}

    public void init() {}
}

