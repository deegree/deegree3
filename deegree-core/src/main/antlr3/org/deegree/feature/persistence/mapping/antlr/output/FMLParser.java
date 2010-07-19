// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g 2010-07-19 14:03:59

  package org.deegree.feature.persistence.mapping.antlr;
  import org.deegree.feature.persistence.mapping.MappingExpression;
  import org.deegree.feature.persistence.mapping.JoinChain;
  import org.deegree.feature.persistence.mapping.DBField;
  import org.deegree.feature.persistence.mapping.Function;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.antlr.runtime.debug.*;
import java.io.IOException;

import org.antlr.runtime.tree.*;

public class FMLParser extends DebugParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "Identifier", "WS", "'()'", "'('", "')'", "','", "'->'", "'.'"
    };
    public static final int WS=5;
    public static final int T__11=11;
    public static final int T__10=10;
    public static final int EOF=-1;
    public static final int T__9=9;
    public static final int T__8=8;
    public static final int Identifier=4;
    public static final int T__7=7;
    public static final int T__6=6;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "argList", "joinChain", "synpred1_FML", "function", 
        "dbField", "mappingExpr"
    };
     
        public int ruleLevel = 0;
        public int getRuleLevel() { return ruleLevel; }
        public void incRuleLevel() { ruleLevel++; }
        public void decRuleLevel() { ruleLevel--; }
        public FMLParser(TokenStream input) {
            this(input, DebugEventSocketProxy.DEFAULT_DEBUGGER_PORT, new RecognizerSharedState());
        }
        public FMLParser(TokenStream input, int port, RecognizerSharedState state) {
            super(input, state);
            DebugEventSocketProxy proxy =
                new DebugEventSocketProxy(this,port,adaptor);
            setDebugListener(proxy);
            setTokenStream(new DebugTokenStream(input,proxy));
            try {
                proxy.handshake();
            }
            catch (IOException ioe) {
                reportError(ioe);
            }
            TreeAdaptor adap = new CommonTreeAdaptor();
            setTreeAdaptor(adap);
            proxy.setTreeAdaptor(adap);
        }
    public FMLParser(TokenStream input, DebugEventListener dbg) {
        super(input, dbg);

         
        TreeAdaptor adap = new CommonTreeAdaptor();
        setTreeAdaptor(adap);

    }
    protected boolean evalPredicate(boolean result, String predicate) {
        dbg.semanticPredicate(result, predicate);
        return result;
    }

    protected DebugTreeAdaptor adaptor;
    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = new DebugTreeAdaptor(dbg,adaptor);

    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }


    public String[] getTokenNames() { return FMLParser.tokenNames; }
    public String getGrammarFileName() { return "/home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g"; }


    public static class mappingExpr_return extends ParserRuleReturnScope {
        public MappingExpression value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "mappingExpr"
    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:24:1: mappingExpr returns [MappingExpression value] : ( dbField | joinChain | function );
    public final FMLParser.mappingExpr_return mappingExpr() throws RecognitionException {
        FMLParser.mappingExpr_return retval = new FMLParser.mappingExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        FMLParser.dbField_return dbField1 = null;

        FMLParser.joinChain_return joinChain2 = null;

        FMLParser.function_return function3 = null;



        try { dbg.enterRule(getGrammarFileName(), "mappingExpr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(24, 1);

        try {
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:25:5: ( dbField | joinChain | function )
            int alt1=3;
            try { dbg.enterDecision(1);

            int LA1_0 = input.LA(1);

            if ( (LA1_0==Identifier) ) {
                switch ( input.LA(2) ) {
                case 11:
                    {
                    int LA1_2 = input.LA(3);

                    if ( (LA1_2==Identifier) ) {
                        switch ( input.LA(4) ) {
                        case 11:
                            {
                            int LA1_7 = input.LA(5);

                            if ( (LA1_7==Identifier) ) {
                                int LA1_8 = input.LA(6);

                                if ( (LA1_8==10) ) {
                                    alt1=2;
                                }
                                else if ( ((LA1_8>=8 && LA1_8<=9)) ) {
                                    alt1=1;
                                }
                                else {
                                    if (state.backtracking>0) {state.failed=true; return retval;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 1, 8, input);

                                    dbg.recognitionException(nvae);
                                    throw nvae;
                                }
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 1, 7, input);

                                dbg.recognitionException(nvae);
                                throw nvae;
                            }
                            }
                            break;
                        case 8:
                        case 9:
                            {
                            alt1=1;
                            }
                            break;
                        case 10:
                            {
                            alt1=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 1, 6, input);

                            dbg.recognitionException(nvae);
                            throw nvae;
                        }

                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 1, 2, input);

                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    }
                    break;
                case 6:
                case 7:
                    {
                    alt1=3;
                    }
                    break;
                case 10:
                    {
                    alt1=2;
                    }
                    break;
                case 8:
                case 9:
                    {
                    alt1=1;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }

            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(1);}

            switch (alt1) {
                case 1 :
                    dbg.enterAlt(1);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:25:10: dbField
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(25,10);
                    pushFollow(FOLLOW_dbField_in_mappingExpr63);
                    dbField1=dbField();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dbField1.getTree());
                    dbg.location(25,18);
                    if ( state.backtracking==0 ) {
                      retval.value =(dbField1!=null?dbField1.value:null);
                    }

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:26:10: joinChain
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(26,10);
                    pushFollow(FOLLOW_joinChain_in_mappingExpr76);
                    joinChain2=joinChain();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, joinChain2.getTree());
                    dbg.location(26,20);
                    if ( state.backtracking==0 ) {
                      retval.value =(joinChain2!=null?joinChain2.value:null);
                    }

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:27:10: function
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(27,10);
                    pushFollow(FOLLOW_function_in_mappingExpr89);
                    function3=function();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, function3.getTree());
                    dbg.location(27,19);
                    if ( state.backtracking==0 ) {
                      retval.value =(function3!=null?function3.value:null);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

                throw re;

        }
        finally {
        }
        dbg.location(28, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "mappingExpr");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return retval;
    }
    // $ANTLR end "mappingExpr"

    public static class function_return extends ParserRuleReturnScope {
        public Function value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function"
    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:33:1: function returns [Function value] options {backtrack=true; } : ( Identifier '()' | Identifier '(' argList ')' );
    public final FMLParser.function_return function() throws RecognitionException {
        FMLParser.function_return retval = new FMLParser.function_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token Identifier4=null;
        Token string_literal5=null;
        Token Identifier6=null;
        Token char_literal7=null;
        Token char_literal9=null;
        FMLParser.argList_return argList8 = null;


        CommonTree Identifier4_tree=null;
        CommonTree string_literal5_tree=null;
        CommonTree Identifier6_tree=null;
        CommonTree char_literal7_tree=null;
        CommonTree char_literal9_tree=null;

        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(33, 1);

        try {
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:35:5: ( Identifier '()' | Identifier '(' argList ')' )
            int alt2=2;
            try { dbg.enterDecision(2);

            int LA2_0 = input.LA(1);

            if ( (LA2_0==Identifier) ) {
                int LA2_1 = input.LA(2);

                if ( (LA2_1==6) ) {
                    alt2=1;
                }
                else if ( (LA2_1==7) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(2);}

            switch (alt2) {
                case 1 :
                    dbg.enterAlt(1);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:35:10: Identifier '()'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(35,10);
                    Identifier4=(Token)match(input,Identifier,FOLLOW_Identifier_in_function132); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Identifier4_tree = (CommonTree)adaptor.create(Identifier4);
                    adaptor.addChild(root_0, Identifier4_tree);
                    }
                    dbg.location(35,21);
                    string_literal5=(Token)match(input,6,FOLLOW_6_in_function134); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal5_tree = (CommonTree)adaptor.create(string_literal5);
                    adaptor.addChild(root_0, string_literal5_tree);
                    }

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:36:10: Identifier '(' argList ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(36,10);
                    Identifier6=(Token)match(input,Identifier,FOLLOW_Identifier_in_function145); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    Identifier6_tree = (CommonTree)adaptor.create(Identifier6);
                    adaptor.addChild(root_0, Identifier6_tree);
                    }
                    dbg.location(36,21);
                    char_literal7=(Token)match(input,7,FOLLOW_7_in_function147); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal7_tree = (CommonTree)adaptor.create(char_literal7);
                    adaptor.addChild(root_0, char_literal7_tree);
                    }
                    dbg.location(36,25);
                    pushFollow(FOLLOW_argList_in_function149);
                    argList8=argList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argList8.getTree());
                    dbg.location(36,33);
                    char_literal9=(Token)match(input,8,FOLLOW_8_in_function151); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal9_tree = (CommonTree)adaptor.create(char_literal9);
                    adaptor.addChild(root_0, char_literal9_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

                throw re;

        }
        finally {
        }
        dbg.location(37, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "function");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return retval;
    }
    // $ANTLR end "function"

    public static class argList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argList"
    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:42:1: argList : mappingExpr ( ',' argList )* ;
    public final FMLParser.argList_return argList() throws RecognitionException {
        FMLParser.argList_return retval = new FMLParser.argList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal11=null;
        FMLParser.mappingExpr_return mappingExpr10 = null;

        FMLParser.argList_return argList12 = null;


        CommonTree char_literal11_tree=null;

        try { dbg.enterRule(getGrammarFileName(), "argList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(42, 1);

        try {
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:43:5: ( mappingExpr ( ',' argList )* )
            dbg.enterAlt(1);

            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:43:10: mappingExpr ( ',' argList )*
            {
            root_0 = (CommonTree)adaptor.nil();

            dbg.location(43,10);
            pushFollow(FOLLOW_mappingExpr_in_argList177);
            mappingExpr10=mappingExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, mappingExpr10.getTree());
            dbg.location(43,22);
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:43:22: ( ',' argList )*
            try { dbg.enterSubRule(3);

            loop3:
            do {
                int alt3=2;
                try { dbg.enterDecision(3);

                int LA3_0 = input.LA(1);

                if ( (LA3_0==9) ) {
                    alt3=1;
                }


                } finally {dbg.exitDecision(3);}

                switch (alt3) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:43:23: ',' argList
            	    {
            	    dbg.location(43,23);
            	    char_literal11=(Token)match(input,9,FOLLOW_9_in_argList180); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal11_tree = (CommonTree)adaptor.create(char_literal11);
            	    adaptor.addChild(root_0, char_literal11_tree);
            	    }
            	    dbg.location(43,27);
            	    pushFollow(FOLLOW_argList_in_argList182);
            	    argList12=argList();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, argList12.getTree());

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);
            } finally {dbg.exitSubRule(3);}


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

                throw re;

        }
        finally {
        }
        dbg.location(44, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "argList");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return retval;
    }
    // $ANTLR end "argList"

    public static class joinChain_return extends ParserRuleReturnScope {
        public JoinChain value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "joinChain"
    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:49:1: joinChain returns [JoinChain value] : (dbf1= dbField '->' dbf2= dbField | dbf1= dbField '->' jc1= joinChain );
    public final FMLParser.joinChain_return joinChain() throws RecognitionException {
        FMLParser.joinChain_return retval = new FMLParser.joinChain_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal13=null;
        Token string_literal14=null;
        FMLParser.dbField_return dbf1 = null;

        FMLParser.dbField_return dbf2 = null;

        FMLParser.joinChain_return jc1 = null;


        CommonTree string_literal13_tree=null;
        CommonTree string_literal14_tree=null;

        try { dbg.enterRule(getGrammarFileName(), "joinChain");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(49, 1);

        try {
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:50:5: (dbf1= dbField '->' dbf2= dbField | dbf1= dbField '->' jc1= joinChain )
            int alt4=2;
            try { dbg.enterDecision(4);

            try {
                isCyclicDecision = true;
                alt4 = dfa4.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(4);}

            switch (alt4) {
                case 1 :
                    dbg.enterAlt(1);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:50:10: dbf1= dbField '->' dbf2= dbField
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(50,14);
                    pushFollow(FOLLOW_dbField_in_joinChain216);
                    dbf1=dbField();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dbf1.getTree());
                    dbg.location(50,23);
                    string_literal13=(Token)match(input,10,FOLLOW_10_in_joinChain218); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal13_tree = (CommonTree)adaptor.create(string_literal13);
                    adaptor.addChild(root_0, string_literal13_tree);
                    }
                    dbg.location(50,32);
                    pushFollow(FOLLOW_dbField_in_joinChain222);
                    dbf2=dbField();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dbf2.getTree());
                    dbg.location(50,41);
                    if ( state.backtracking==0 ) {
                      retval.value =new JoinChain((dbf1!=null?dbf1.value:null),(dbf2!=null?dbf2.value:null));
                    }

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:51:10: dbf1= dbField '->' jc1= joinChain
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(51,14);
                    pushFollow(FOLLOW_dbField_in_joinChain237);
                    dbf1=dbField();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dbf1.getTree());
                    dbg.location(51,23);
                    string_literal14=(Token)match(input,10,FOLLOW_10_in_joinChain239); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal14_tree = (CommonTree)adaptor.create(string_literal14);
                    adaptor.addChild(root_0, string_literal14_tree);
                    }
                    dbg.location(51,31);
                    pushFollow(FOLLOW_joinChain_in_joinChain243);
                    jc1=joinChain();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, jc1.getTree());
                    dbg.location(51,42);
                    if ( state.backtracking==0 ) {
                      retval.value =new JoinChain((dbf1!=null?dbf1.value:null), (jc1!=null?jc1.value:null));
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

                throw re;

        }
        finally {
        }
        dbg.location(52, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "joinChain");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return retval;
    }
    // $ANTLR end "joinChain"

    public static class dbField_return extends ParserRuleReturnScope {
        public DBField value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dbField"
    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:57:1: dbField returns [DBField value] : (i1= Identifier | i1= Identifier '.' i2= Identifier | i1= Identifier '.' i2= Identifier '.' i3= Identifier );
    public final FMLParser.dbField_return dbField() throws RecognitionException {
        FMLParser.dbField_return retval = new FMLParser.dbField_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token i1=null;
        Token i2=null;
        Token i3=null;
        Token char_literal15=null;
        Token char_literal16=null;
        Token char_literal17=null;

        CommonTree i1_tree=null;
        CommonTree i2_tree=null;
        CommonTree i3_tree=null;
        CommonTree char_literal15_tree=null;
        CommonTree char_literal16_tree=null;
        CommonTree char_literal17_tree=null;

        try { dbg.enterRule(getGrammarFileName(), "dbField");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(57, 1);

        try {
            // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:58:5: (i1= Identifier | i1= Identifier '.' i2= Identifier | i1= Identifier '.' i2= Identifier '.' i3= Identifier )
            int alt5=3;
            try { dbg.enterDecision(5);

            int LA5_0 = input.LA(1);

            if ( (LA5_0==Identifier) ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1==11) ) {
                    int LA5_2 = input.LA(3);

                    if ( (LA5_2==Identifier) ) {
                        int LA5_4 = input.LA(4);

                        if ( (LA5_4==11) ) {
                            alt5=3;
                        }
                        else if ( ((LA5_4>=8 && LA5_4<=10)) ) {
                            alt5=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 4, input);

                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 5, 2, input);

                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                }
                else if ( ((LA5_1>=8 && LA5_1<=10)) ) {
                    alt5=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(5);}

            switch (alt5) {
                case 1 :
                    dbg.enterAlt(1);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:58:10: i1= Identifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(58,12);
                    i1=(Token)match(input,Identifier,FOLLOW_Identifier_in_dbField281); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    i1_tree = (CommonTree)adaptor.create(i1);
                    adaptor.addChild(root_0, i1_tree);
                    }
                    dbg.location(58,24);
                    if ( state.backtracking==0 ) {
                      retval.value =new DBField ((i1!=null?i1.getText():null));
                    }

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:59:10: i1= Identifier '.' i2= Identifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(59,12);
                    i1=(Token)match(input,Identifier,FOLLOW_Identifier_in_dbField296); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    i1_tree = (CommonTree)adaptor.create(i1);
                    adaptor.addChild(root_0, i1_tree);
                    }
                    dbg.location(59,24);
                    char_literal15=(Token)match(input,11,FOLLOW_11_in_dbField298); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal15_tree = (CommonTree)adaptor.create(char_literal15);
                    adaptor.addChild(root_0, char_literal15_tree);
                    }
                    dbg.location(59,30);
                    i2=(Token)match(input,Identifier,FOLLOW_Identifier_in_dbField302); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    i2_tree = (CommonTree)adaptor.create(i2);
                    adaptor.addChild(root_0, i2_tree);
                    }
                    dbg.location(59,42);
                    if ( state.backtracking==0 ) {
                      retval.value =new DBField ((i1!=null?i1.getText():null),(i2!=null?i2.getText():null));
                    }

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /home/schneider/workspace/deegree-core/src/main/antlr3/org/deegree/feature/persistence/mapping/antlr/FML.g:60:10: i1= Identifier '.' i2= Identifier '.' i3= Identifier
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    dbg.location(60,12);
                    i1=(Token)match(input,Identifier,FOLLOW_Identifier_in_dbField317); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    i1_tree = (CommonTree)adaptor.create(i1);
                    adaptor.addChild(root_0, i1_tree);
                    }
                    dbg.location(60,24);
                    char_literal16=(Token)match(input,11,FOLLOW_11_in_dbField319); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal16_tree = (CommonTree)adaptor.create(char_literal16);
                    adaptor.addChild(root_0, char_literal16_tree);
                    }
                    dbg.location(60,30);
                    i2=(Token)match(input,Identifier,FOLLOW_Identifier_in_dbField323); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    i2_tree = (CommonTree)adaptor.create(i2);
                    adaptor.addChild(root_0, i2_tree);
                    }
                    dbg.location(60,42);
                    char_literal17=(Token)match(input,11,FOLLOW_11_in_dbField325); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal17_tree = (CommonTree)adaptor.create(char_literal17);
                    adaptor.addChild(root_0, char_literal17_tree);
                    }
                    dbg.location(60,48);
                    i3=(Token)match(input,Identifier,FOLLOW_Identifier_in_dbField329); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    i3_tree = (CommonTree)adaptor.create(i3);
                    adaptor.addChild(root_0, i3_tree);
                    }
                    dbg.location(60,60);
                    if ( state.backtracking==0 ) {
                      retval.value =new DBField ((i1!=null?i1.getText():null),(i2!=null?i2.getText():null),(i3!=null?i3.getText():null));
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

                throw re;

        }
        finally {
        }
        dbg.location(61, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "dbField");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return retval;
    }
    // $ANTLR end "dbField"

    // Delegated rules


    protected DFA4 dfa4 = new DFA4(this);
    static final String DFA4_eotS =
        "\16\uffff";
    static final String DFA4_eofS =
        "\16\uffff";
    static final String DFA4_minS =
        "\1\4\1\12\2\4\1\12\1\10\2\4\2\uffff\1\12\1\10\1\4\1\10";
    static final String DFA4_maxS =
        "\1\4\1\13\2\4\2\13\2\4\2\uffff\1\12\1\13\1\4\1\12";
    static final String DFA4_acceptS =
        "\10\uffff\1\2\1\1\4\uffff";
    static final String DFA4_specialS =
        "\16\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\1",
            "\1\3\1\2",
            "\1\4",
            "\1\5",
            "\1\3\1\6",
            "\2\11\1\10\1\7",
            "\1\12",
            "\1\13",
            "",
            "",
            "\1\3",
            "\2\11\1\10\1\14",
            "\1\15",
            "\2\11\1\10"
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "49:1: joinChain returns [JoinChain value] : (dbf1= dbField '->' dbf2= dbField | dbf1= dbField '->' jc1= joinChain );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_dbField_in_mappingExpr63 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_joinChain_in_mappingExpr76 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_mappingExpr89 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_function132 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_6_in_function134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_function145 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_7_in_function147 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_argList_in_function149 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_8_in_function151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mappingExpr_in_argList177 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_9_in_argList180 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_argList_in_argList182 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_dbField_in_joinChain216 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_joinChain218 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_dbField_in_joinChain222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dbField_in_joinChain237 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_joinChain239 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_joinChain_in_joinChain243 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_dbField281 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_dbField296 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_dbField298 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_dbField302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_dbField317 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_dbField319 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_dbField323 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_dbField325 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_dbField329 = new BitSet(new long[]{0x0000000000000002L});

}