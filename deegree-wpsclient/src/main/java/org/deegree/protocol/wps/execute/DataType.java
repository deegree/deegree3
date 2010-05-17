package org.deegree.protocol.wps.execute;

public class DataType {

    ComplexData complexData;

    LiteralData literalData;

    BoundingBoxData boundingBoxData;

    public ComplexData getComplexData() {
        return complexData;
    }

    public void setComplexData( ComplexData complexData ) {
        this.complexData = complexData;
    }

    public LiteralData getLiteralData() {
        return literalData;
    }

    public void setLiteralData( LiteralData literalData ) {
        this.literalData = literalData;
    }

    public BoundingBoxData getBoundingBoxData() {
        return boundingBoxData;
    }

    public void setBoundingBoxData( BoundingBoxData boundingBoxData ) {
        this.boundingBoxData = boundingBoxData;
    }

}
