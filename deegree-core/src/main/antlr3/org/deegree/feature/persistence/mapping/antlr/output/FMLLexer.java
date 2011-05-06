// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g 2010-07-19 14:04:00

  package org.deegree.feature.persistence.mapping.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class FMLLexer extends Lexer {
    public static final int WS=5;
    public static final int T__11=11;
    public static final int T__10=10;
    public static final int EOF=-1;
    public static final int T__9=9;
    public static final int T__8=8;
    public static final int T__7=7;
    public static final int Identifier=4;
    public static final int T__6=6;

    // delegates
    // delegators

    public FMLLexer() {;} 
    public FMLLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public FMLLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g"; }

    // $ANTLR start "T__6"
    public final void mT__6() throws RecognitionException {
        try {
            int _type = T__6;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:7:6: ( '()' )
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:7:8: '()'
            {
            match("()"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__6"

    // $ANTLR start "T__7"
    public final void mT__7() throws RecognitionException {
        try {
            int _type = T__7;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:8:6: ( '(' )
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:8:8: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__7"

    // $ANTLR start "T__8"
    public final void mT__8() throws RecognitionException {
        try {
            int _type = T__8;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:9:6: ( ')' )
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:9:8: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__8"

    // $ANTLR start "T__9"
    public final void mT__9() throws RecognitionException {
        try {
            int _type = T__9;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:10:6: ( ',' )
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:10:8: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__9"

    // $ANTLR start "T__10"
    public final void mT__10() throws RecognitionException {
        try {
            int _type = T__10;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:11:7: ( '->' )
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:11:9: '->'
            {
            match("->"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__10"

    // $ANTLR start "T__11"
    public final void mT__11() throws RecognitionException {
        try {
            int _type = T__11;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:12:7: ( '.' )
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:12:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__11"

    // $ANTLR start "Identifier"
    public final void mIdentifier() throws RecognitionException {
        try {
            int _type = Identifier;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:71:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )+ )
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:71:10: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )+
            {
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:71:10: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Identifier"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:76:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:76:9: ( ' ' | '\\t' | '\\r' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:8: ( T__6 | T__7 | T__8 | T__9 | T__10 | T__11 | Identifier | WS )
        int alt2=8;
        alt2 = dfa2.predict(input);
        switch (alt2) {
            case 1 :
                // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:10: T__6
                {
                mT__6(); 

                }
                break;
            case 2 :
                // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:15: T__7
                {
                mT__7(); 

                }
                break;
            case 3 :
                // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:20: T__8
                {
                mT__8(); 

                }
                break;
            case 4 :
                // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:25: T__9
                {
                mT__9(); 

                }
                break;
            case 5 :
                // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:30: T__10
                {
                mT__10(); 

                }
                break;
            case 6 :
                // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:36: T__11
                {
                mT__11(); 

                }
                break;
            case 7 :
                // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:42: Identifier
                {
                mIdentifier(); 

                }
                break;
            case 8 :
                // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:1:53: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA2 dfa2 = new DFA2(this);
    static final String DFA2_eotS =
        "\1\uffff\1\11\10\uffff";
    static final String DFA2_eofS =
        "\12\uffff";
    static final String DFA2_minS =
        "\1\11\1\51\10\uffff";
    static final String DFA2_maxS =
        "\1\172\1\51\10\uffff";
    static final String DFA2_acceptS =
        "\2\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\1\1\2";
    static final String DFA2_specialS =
        "\12\uffff}>";
    static final String[] DFA2_transitionS = {
            "\2\7\2\uffff\1\7\22\uffff\1\7\7\uffff\1\1\1\2\2\uffff\1\3\1"+
            "\4\1\5\1\uffff\12\6\7\uffff\32\6\4\uffff\1\6\1\uffff\32\6",
            "\1\10",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__6 | T__7 | T__8 | T__9 | T__10 | T__11 | Identifier | WS );";
        }
    }
 

}