package org.deegree.feature.persistence.mapping;

public class ColumnName {

    public ColumnName (String column) {
        System.out.println ("Simple");
    }

    public ColumnName (String table, String column) {
        System.out.println ("Qualified");   
    }
    
}
