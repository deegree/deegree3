package org.deegree;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.deegree.feature.persistence.mapping.antlr.FMLLexer;
import org.deegree.feature.persistence.mapping.antlr.FMLParser;

public class ANTLRTest {

    public static void main( String[] args )
                            throws Exception {

        ANTLRStringStream in = new ANTLRStringStream("arschgesichtpopel");
        FMLLexer lexer = new FMLLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FMLParser parser = new FMLParser(tokens);
        System.out.println ("BLA: " + parser.eval().value);

    }
}
