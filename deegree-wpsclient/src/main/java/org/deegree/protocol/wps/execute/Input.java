package org.deegree.protocol.wps.execute;

import java.util.ArrayList;
import java.util.List;

public class Input {

    List<DataInputExecute> dataInputList = new ArrayList();

    public List<DataInputExecute> getDataInput() {
        return dataInputList;
    }

    public void addDataInput( DataInputExecute dataInput ) {
        this.dataInputList.add( dataInput );
    }

}
