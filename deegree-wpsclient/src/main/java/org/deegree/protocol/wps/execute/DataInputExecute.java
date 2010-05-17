package org.deegree.protocol.wps.execute;

public class DataInputExecute {

    String identifier;

    String title;

    String abstraCt;

    InputFormChoiceExecute inputFormChoice;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getAbstraCt() {
        return abstraCt;
    }

    public void setAbstraCt( String abstraCt ) {
        this.abstraCt = abstraCt;
    }

    public InputFormChoiceExecute getInputFormChoice() {
        return inputFormChoice;
    }

    public void setInputFormChoice( InputFormChoiceExecute inputFormChoice ) {
        this.inputFormChoice = inputFormChoice;
    }

}
