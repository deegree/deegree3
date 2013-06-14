// $ANTLR 3.5 /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g 2013-03-07 14:11:14

package org.deegree.featureinfo.templating;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("all")
public class Templating2Lexer extends Lexer {
	public static final int EOF=-1;
	public static final int BracketLeft=4;
	public static final int BracketRight=5;
	public static final int Colon=6;
	public static final int Comma=7;
	public static final int Digit=8;
	public static final int Equals=9;
	public static final int EvenStart=10;
	public static final int ExplicitTemplateEnd=11;
	public static final int FeatureCallStart=12;
	public static final int GmlId=13;
	public static final int ID=14;
	public static final int Index=15;
	public static final int Kvp=16;
	public static final int Letter=17;
	public static final int LinkStart=18;
	public static final int MapDefinitionStart=19;
	public static final int MapSpace=20;
	public static final int NameStart=21;
	public static final int Not=22;
	public static final int OddStart=23;
	public static final int Point=24;
	public static final int PropertyCallStart=25;
	public static final int Rest=26;
	public static final int Slash=27;
	public static final int Star=28;
	public static final int TagClose=29;
	public static final int TagOpen=30;
	public static final int TemplateDefinitionStart=31;
	public static final int Url=32;
	public static final int UrlWithPort=33;
	public static final int ValueStart=34;
	public static final int WS=35;

	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public Templating2Lexer() {} 
	public Templating2Lexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public Templating2Lexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "/home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g"; }

	@Override
	public Token nextToken() {
		while (true) {
			if ( input.LA(1)==CharStream.EOF ) {
				Token eof = new CommonToken(input,Token.EOF,
											Token.DEFAULT_CHANNEL,
											input.index(),input.index());
				eof.setLine(getLine());
				eof.setCharPositionInLine(getCharPositionInLine());
				return eof;
			}
			state.token = null;
		state.channel = Token.DEFAULT_CHANNEL;
			state.tokenStartCharIndex = input.index();
			state.tokenStartCharPositionInLine = input.getCharPositionInLine();
			state.tokenStartLine = input.getLine();
		state.text = null;
			try {
				int m = input.mark();
				state.backtracking=1; 
				state.failed=false;
				mTokens();
				state.backtracking=0;
				if ( state.failed ) {
					input.rewind(m);
					input.consume(); 
				}
				else {
					emit();
					return state.token;
				}
			}
			catch (RecognitionException re) {
				// shouldn't happen in backtracking mode, but...
				reportError(re);
				recover(re);
			}
		}
	}

	@Override
	public void memoize(IntStream input,
			int ruleIndex,
			int ruleStartIndex)
	{
	if ( state.backtracking>1 ) super.memoize(input, ruleIndex, ruleStartIndex);
	}

	@Override
	public boolean alreadyParsedRule(IntStream input, int ruleIndex) {
	if ( state.backtracking>1 ) return super.alreadyParsedRule(input, ruleIndex);
	return false;
	}
	// $ANTLR start "TemplateDefinitionStart"
	public final void mTemplateDefinitionStart() throws RecognitionException {
		try {
			int _type = TemplateDefinitionStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:11:24: ( '<?template ' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:11:26: '<?template '
			{
			match("<?template "); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TemplateDefinitionStart"

	// $ANTLR start "MapDefinitionStart"
	public final void mMapDefinitionStart() throws RecognitionException {
		try {
			int _type = MapDefinitionStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:12:19: ( '<?map ' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:12:21: '<?map '
			{
			match("<?map "); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MapDefinitionStart"

	// $ANTLR start "FeatureCallStart"
	public final void mFeatureCallStart() throws RecognitionException {
		try {
			int _type = FeatureCallStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:13:17: ( '<?feature ' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:13:19: '<?feature '
			{
			match("<?feature "); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FeatureCallStart"

	// $ANTLR start "PropertyCallStart"
	public final void mPropertyCallStart() throws RecognitionException {
		try {
			int _type = PropertyCallStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:14:18: ( '<?property ' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:14:20: '<?property '
			{
			match("<?property "); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PropertyCallStart"

	// $ANTLR start "NameStart"
	public final void mNameStart() throws RecognitionException {
		try {
			int _type = NameStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:15:10: ( '<?name' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:15:12: '<?name'
			{
			match("<?name"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NameStart"

	// $ANTLR start "ValueStart"
	public final void mValueStart() throws RecognitionException {
		try {
			int _type = ValueStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:16:11: ( '<?value' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:16:13: '<?value'
			{
			match("<?value"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ValueStart"

	// $ANTLR start "OddStart"
	public final void mOddStart() throws RecognitionException {
		try {
			int _type = OddStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:17:9: ( '<?odd' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:17:11: '<?odd'
			{
			match("<?odd"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OddStart"

	// $ANTLR start "EvenStart"
	public final void mEvenStart() throws RecognitionException {
		try {
			int _type = EvenStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:18:10: ( '<?even' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:18:12: '<?even'
			{
			match("<?even"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EvenStart"

	// $ANTLR start "LinkStart"
	public final void mLinkStart() throws RecognitionException {
		try {
			int _type = LinkStart;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:19:10: ( '<?link' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:19:12: '<?link'
			{
			match("<?link"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LinkStart"

	// $ANTLR start "Index"
	public final void mIndex() throws RecognitionException {
		try {
			int _type = Index;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:20:6: ( '<?index>' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:20:8: '<?index>'
			{
			match("<?index>"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Index"

	// $ANTLR start "GmlId"
	public final void mGmlId() throws RecognitionException {
		try {
			int _type = GmlId;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:21:6: ( '<?gmlid>' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:21:8: '<?gmlid>'
			{
			match("<?gmlid>"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GmlId"

	// $ANTLR start "ExplicitTemplateEnd"
	public final void mExplicitTemplateEnd() throws RecognitionException {
		try {
			int _type = ExplicitTemplateEnd;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:22:20: ( '</?>' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:22:22: '</?>'
			{
			match("</?>"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ExplicitTemplateEnd"

	// $ANTLR start "Letter"
	public final void mLetter() throws RecognitionException {
		try {
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:24:16: ({...}? . )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:24:18: {...}? .
			{
			if ( !(( Character.isLetter( input.LA(1) ) )) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "Letter", " Character.isLetter( input.LA(1) ) ");
			}
			matchAny(); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Letter"

	// $ANTLR start "Digit"
	public final void mDigit() throws RecognitionException {
		try {
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:25:15: ({...}? . )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:25:17: {...}? .
			{
			if ( !(( Character.isDigit( input.LA(1) ) )) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "Digit", " Character.isDigit( input.LA(1) ) ");
			}
			matchAny(); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Digit"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:26:12: ({...}? . )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:26:14: {...}? .
			{
			if ( !(( Character.isWhitespace( input.LA(1) ) )) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "WS", " Character.isWhitespace( input.LA(1) ) ");
			}
			matchAny(); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	// $ANTLR start "Point"
	public final void mPoint() throws RecognitionException {
		try {
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:27:15: ( '.' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:27:17: '.'
			{
			match('.'); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Point"

	// $ANTLR start "Slash"
	public final void mSlash() throws RecognitionException {
		try {
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:28:15: ( '/' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:28:17: '/'
			{
			match('/'); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Slash"

	// $ANTLR start "UrlWithPort"
	public final void mUrlWithPort() throws RecognitionException {
		try {
			int _type = UrlWithPort;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:30:12: ( Url Colon ( Digit )+ (~ ( Colon | TagClose ) )+ )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:30:14: Url Colon ( Digit )+ (~ ( Colon | TagClose ) )+
			{
			mUrl(); if (state.failed) return;

			mColon(); if (state.failed) return;

			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:30:24: ( Digit )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= '\u0000' && LA1_0 <= '9')||(LA1_0 >= ';' && LA1_0 <= '=')||(LA1_0 >= '?' && LA1_0 <= '\uFFFF')) ) {
					int LA1_1 = input.LA(2);
					if ( (( Character.isDigit( input.LA(1) ) )) ) {
						alt1=1;
					}

				}
				else if ( (LA1_0==':'||LA1_0=='>') ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:30:24: Digit
					{
					mDigit(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:30:31: (~ ( Colon | TagClose ) )+
			int cnt2=0;
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( ((LA2_0 >= '\u0000' && LA2_0 <= '9')||(LA2_0 >= ';' && LA2_0 <= '=')||(LA2_0 >= '?' && LA2_0 <= '\uFFFF')) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '9')||(input.LA(1) >= ';' && input.LA(1) <= '=')||(input.LA(1) >= '?' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt2 >= 1 ) break loop2;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(2, input);
					throw eee;
				}
				cnt2++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UrlWithPort"

	// $ANTLR start "Url"
	public final void mUrl() throws RecognitionException {
		try {
			int _type = Url;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:31:4: ( ( ID Colon Slash Slash (~ Colon )+ ) )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:31:6: ( ID Colon Slash Slash (~ Colon )+ )
			{
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:31:6: ( ID Colon Slash Slash (~ Colon )+ )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:31:7: ID Colon Slash Slash (~ Colon )+
			{
			mID(); if (state.failed) return;

			mColon(); if (state.failed) return;

			mSlash(); if (state.failed) return;

			mSlash(); if (state.failed) return;

			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:31:28: (~ Colon )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '\u0000' && LA3_0 <= '9')||(LA3_0 >= ';' && LA3_0 <= '\uFFFF')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '9')||(input.LA(1) >= ';' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt3 >= 1 ) break loop3;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(3, input);
					throw eee;
				}
				cnt3++;
			}

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Url"

	// $ANTLR start "Equals"
	public final void mEquals() throws RecognitionException {
		try {
			int _type = Equals;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:33:7: ( '=' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:33:9: '='
			{
			match('='); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Equals"

	// $ANTLR start "Star"
	public final void mStar() throws RecognitionException {
		try {
			int _type = Star;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:34:5: ( '*' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:34:7: '*'
			{
			match('*'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Star"

	// $ANTLR start "Comma"
	public final void mComma() throws RecognitionException {
		try {
			int _type = Comma;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:35:6: ( ',' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:35:8: ','
			{
			match(','); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Comma"

	// $ANTLR start "Colon"
	public final void mColon() throws RecognitionException {
		try {
			int _type = Colon;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:36:6: ( ':' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:36:8: ':'
			{
			match(':'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Colon"

	// $ANTLR start "TagOpen"
	public final void mTagOpen() throws RecognitionException {
		try {
			int _type = TagOpen;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:37:8: ( '<' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:37:10: '<'
			{
			match('<'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TagOpen"

	// $ANTLR start "TagClose"
	public final void mTagClose() throws RecognitionException {
		try {
			int _type = TagClose;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:38:9: ( '>' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:38:11: '>'
			{
			match('>'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TagClose"

	// $ANTLR start "MapSpace"
	public final void mMapSpace() throws RecognitionException {
		try {
			int _type = MapSpace;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:39:9: ( 'map ' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:39:11: 'map '
			{
			match("map "); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MapSpace"

	// $ANTLR start "Not"
	public final void mNot() throws RecognitionException {
		try {
			int _type = Not;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:40:4: ( 'not' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:40:6: 'not'
			{
			match("not"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Not"

	// $ANTLR start "BracketLeft"
	public final void mBracketLeft() throws RecognitionException {
		try {
			int _type = BracketLeft;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:41:12: ( '(' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:41:14: '('
			{
			match('('); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BracketLeft"

	// $ANTLR start "BracketRight"
	public final void mBracketRight() throws RecognitionException {
		try {
			int _type = BracketRight;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:42:13: ( ')' )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:42:15: ')'
			{
			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BracketRight"

	// $ANTLR start "Kvp"
	public final void mKvp() throws RecognitionException {
		try {
			int _type = Kvp;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:44:4: ( ( '\\n' )* (~ ( '=' | '<' | '>' | '?' | '/' | '\\n' ) )+ '=' (~ ( '\\n' ) )+ ( '\\n' )* )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:44:6: ( '\\n' )* (~ ( '=' | '<' | '>' | '?' | '/' | '\\n' ) )+ '=' (~ ( '\\n' ) )+ ( '\\n' )*
			{
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:44:6: ( '\\n' )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0=='\n') ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:44:6: '\\n'
					{
					match('\n'); if (state.failed) return;
					}
					break;

				default :
					break loop4;
				}
			}

			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:44:12: (~ ( '=' | '<' | '>' | '?' | '/' | '\\n' ) )+
			int cnt5=0;
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( ((LA5_0 >= '\u0000' && LA5_0 <= '\t')||(LA5_0 >= '\u000B' && LA5_0 <= '.')||(LA5_0 >= '0' && LA5_0 <= ';')||(LA5_0 >= '@' && LA5_0 <= '\uFFFF')) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '.')||(input.LA(1) >= '0' && input.LA(1) <= ';')||(input.LA(1) >= '@' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt5 >= 1 ) break loop5;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(5, input);
					throw eee;
				}
				cnt5++;
			}

			match('='); if (state.failed) return;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:44:57: (~ ( '\\n' ) )+
			int cnt6=0;
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( ((LA6_0 >= '\u0000' && LA6_0 <= '\t')||(LA6_0 >= '\u000B' && LA6_0 <= '\uFFFF')) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt6 >= 1 ) break loop6;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(6, input);
					throw eee;
				}
				cnt6++;
			}

			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:44:68: ( '\\n' )*
			loop7:
			while (true) {
				int alt7=2;
				int LA7_0 = input.LA(1);
				if ( (LA7_0=='\n') ) {
					alt7=1;
				}

				switch (alt7) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:44:68: '\\n'
					{
					match('\n'); if (state.failed) return;
					}
					break;

				default :
					break loop7;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Kvp"

	// $ANTLR start "ID"
	public final void mID() throws RecognitionException {
		try {
			int _type = ID;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:45:3: ( ( Letter | Digit | '_' | '-' )+ )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:45:5: ( Letter | Digit | '_' | '-' )+
			{
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:45:5: ( Letter | Digit | '_' | '-' )+
			int cnt8=0;
			loop8:
			while (true) {
				int alt8=5;
				int LA8_0 = input.LA(1);
				if ( (LA8_0=='_') ) {
					int LA8_2 = input.LA(2);
					if ( (( Character.isLetter( input.LA(1) ) )) ) {
						alt8=1;
					}
					else if ( (( Character.isDigit( input.LA(1) ) )) ) {
						alt8=2;
					}
					else if ( (true) ) {
						alt8=3;
					}

				}
				else if ( (LA8_0=='-') ) {
					int LA8_3 = input.LA(2);
					if ( (( Character.isLetter( input.LA(1) ) )) ) {
						alt8=1;
					}
					else if ( (( Character.isDigit( input.LA(1) ) )) ) {
						alt8=2;
					}
					else if ( (true) ) {
						alt8=4;
					}

				}
				else if ( ((LA8_0 >= '\u0000' && LA8_0 <= ',')||(LA8_0 >= '.' && LA8_0 <= '^')||(LA8_0 >= '`' && LA8_0 <= '\uFFFF')) ) {
					int LA8_4 = input.LA(2);
					if ( (( Character.isLetter( input.LA(1) ) )) ) {
						alt8=1;
					}
					else if ( (( Character.isDigit( input.LA(1) ) )) ) {
						alt8=2;
					}

				}

				switch (alt8) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:45:6: Letter
					{
					mLetter(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:45:15: Digit
					{
					mDigit(); if (state.failed) return;

					}
					break;
				case 3 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:45:23: '_'
					{
					match('_'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:45:29: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;

				default :
					if ( cnt8 >= 1 ) break loop8;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(8, input);
					throw eee;
				}
				cnt8++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ID"

	// $ANTLR start "Rest"
	public final void mRest() throws RecognitionException {
		try {
			int _type = Rest;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:47:5: ( (~ ( '<' ) )+ )
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:47:7: (~ ( '<' ) )+
			{
			// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:47:7: (~ ( '<' ) )+
			int cnt9=0;
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( ((LA9_0 >= '\u0000' && LA9_0 <= ';')||(LA9_0 >= '=' && LA9_0 <= '\uFFFF')) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= ';')||(input.LA(1) >= '=' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt9 >= 1 ) break loop9;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(9, input);
					throw eee;
				}
				cnt9++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Rest"

	@Override
	public void mTokens() throws RecognitionException {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:39: ( TemplateDefinitionStart | MapDefinitionStart | FeatureCallStart | PropertyCallStart | NameStart | ValueStart | OddStart | EvenStart | LinkStart | Index | GmlId | ExplicitTemplateEnd | UrlWithPort | Url | Equals | Star | Comma | Colon | TagOpen | TagClose | MapSpace | Not | BracketLeft | BracketRight | Kvp | ID | Rest )
		int alt10=27;
		int LA10_0 = input.LA(1);
		if ( (LA10_0=='<') ) {
			int LA10_1 = input.LA(2);
			if ( (synpred1_Templating2Lexer()) ) {
				alt10=1;
			}
			else if ( (synpred2_Templating2Lexer()) ) {
				alt10=2;
			}
			else if ( (synpred3_Templating2Lexer()) ) {
				alt10=3;
			}
			else if ( (synpred4_Templating2Lexer()) ) {
				alt10=4;
			}
			else if ( (synpred5_Templating2Lexer()) ) {
				alt10=5;
			}
			else if ( (synpred6_Templating2Lexer()) ) {
				alt10=6;
			}
			else if ( (synpred7_Templating2Lexer()) ) {
				alt10=7;
			}
			else if ( (synpred8_Templating2Lexer()) ) {
				alt10=8;
			}
			else if ( (synpred9_Templating2Lexer()) ) {
				alt10=9;
			}
			else if ( (synpred10_Templating2Lexer()) ) {
				alt10=10;
			}
			else if ( (synpred11_Templating2Lexer()) ) {
				alt10=11;
			}
			else if ( (synpred12_Templating2Lexer()) ) {
				alt10=12;
			}
			else if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred19_Templating2Lexer()) ) {
				alt10=19;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 10, 1, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}

		}
		else if ( (LA10_0=='_') ) {
			int LA10_2 = input.LA(2);
			if ( (synpred13_Templating2Lexer()) ) {
				alt10=13;
			}
			else if ( (synpred14_Templating2Lexer()) ) {
				alt10=14;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( (synpred26_Templating2Lexer()) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='-') ) {
			int LA10_3 = input.LA(2);
			if ( (synpred13_Templating2Lexer()) ) {
				alt10=13;
			}
			else if ( (synpred14_Templating2Lexer()) ) {
				alt10=14;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( (synpred26_Templating2Lexer()) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='=') ) {
			int LA10_4 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred15_Templating2Lexer()) ) {
				alt10=15;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='*') ) {
			int LA10_5 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred16_Templating2Lexer()) ) {
				alt10=16;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0==',') ) {
			int LA10_6 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred17_Templating2Lexer()) ) {
				alt10=17;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0==':') ) {
			int LA10_7 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred18_Templating2Lexer()) ) {
				alt10=18;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='>') ) {
			int LA10_8 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred20_Templating2Lexer()) ) {
				alt10=20;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='m') ) {
			int LA10_9 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred21_Templating2Lexer()) ) {
				alt10=21;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='n') ) {
			int LA10_10 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred22_Templating2Lexer()) ) {
				alt10=22;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='(') ) {
			int LA10_11 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred23_Templating2Lexer()) ) {
				alt10=23;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0==')') ) {
			int LA10_12 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred24_Templating2Lexer()) ) {
				alt10=24;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='\n') ) {
			int LA10_13 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( ((LA10_0 >= '\u0000' && LA10_0 <= '\t')||(LA10_0 >= '\u000B' && LA10_0 <= '\'')||LA10_0=='+'||LA10_0=='.'||(LA10_0 >= '0' && LA10_0 <= '9')||LA10_0==';'||(LA10_0 >= '@' && LA10_0 <= '^')||(LA10_0 >= '`' && LA10_0 <= 'l')||(LA10_0 >= 'o' && LA10_0 <= '\uFFFF')) ) {
			int LA10_14 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( (synpred25_Templating2Lexer()) ) {
				alt10=25;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}
		else if ( (LA10_0=='/'||LA10_0=='?') ) {
			int LA10_15 = input.LA(2);
			if ( ((synpred13_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=13;
			}
			else if ( ((synpred14_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=14;
			}
			else if ( ((synpred26_Templating2Lexer()&&(( Character.isLetter( input.LA(1) ) )||( Character.isDigit( input.LA(1) ) )))) ) {
				alt10=26;
			}
			else if ( (true) ) {
				alt10=27;
			}

		}

		else {
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 10, 0, input);
			throw nvae;
		}

		switch (alt10) {
			case 1 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:41: TemplateDefinitionStart
				{
				mTemplateDefinitionStart(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:65: MapDefinitionStart
				{
				mMapDefinitionStart(); if (state.failed) return;

				}
				break;
			case 3 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:84: FeatureCallStart
				{
				mFeatureCallStart(); if (state.failed) return;

				}
				break;
			case 4 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:101: PropertyCallStart
				{
				mPropertyCallStart(); if (state.failed) return;

				}
				break;
			case 5 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:119: NameStart
				{
				mNameStart(); if (state.failed) return;

				}
				break;
			case 6 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:129: ValueStart
				{
				mValueStart(); if (state.failed) return;

				}
				break;
			case 7 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:140: OddStart
				{
				mOddStart(); if (state.failed) return;

				}
				break;
			case 8 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:149: EvenStart
				{
				mEvenStart(); if (state.failed) return;

				}
				break;
			case 9 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:159: LinkStart
				{
				mLinkStart(); if (state.failed) return;

				}
				break;
			case 10 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:169: Index
				{
				mIndex(); if (state.failed) return;

				}
				break;
			case 11 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:175: GmlId
				{
				mGmlId(); if (state.failed) return;

				}
				break;
			case 12 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:181: ExplicitTemplateEnd
				{
				mExplicitTemplateEnd(); if (state.failed) return;

				}
				break;
			case 13 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:201: UrlWithPort
				{
				mUrlWithPort(); if (state.failed) return;

				}
				break;
			case 14 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:213: Url
				{
				mUrl(); if (state.failed) return;

				}
				break;
			case 15 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:217: Equals
				{
				mEquals(); if (state.failed) return;

				}
				break;
			case 16 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:224: Star
				{
				mStar(); if (state.failed) return;

				}
				break;
			case 17 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:229: Comma
				{
				mComma(); if (state.failed) return;

				}
				break;
			case 18 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:235: Colon
				{
				mColon(); if (state.failed) return;

				}
				break;
			case 19 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:241: TagOpen
				{
				mTagOpen(); if (state.failed) return;

				}
				break;
			case 20 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:249: TagClose
				{
				mTagClose(); if (state.failed) return;

				}
				break;
			case 21 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:258: MapSpace
				{
				mMapSpace(); if (state.failed) return;

				}
				break;
			case 22 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:267: Not
				{
				mNot(); if (state.failed) return;

				}
				break;
			case 23 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:271: BracketLeft
				{
				mBracketLeft(); if (state.failed) return;

				}
				break;
			case 24 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:283: BracketRight
				{
				mBracketRight(); if (state.failed) return;

				}
				break;
			case 25 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:296: Kvp
				{
				mKvp(); if (state.failed) return;

				}
				break;
			case 26 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:300: ID
				{
				mID(); if (state.failed) return;

				}
				break;
			case 27 :
				// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:303: Rest
				{
				mRest(); if (state.failed) return;

				}
				break;

		}
	}

	// $ANTLR start synpred1_Templating2Lexer
	public final void synpred1_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:41: ( TemplateDefinitionStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:41: TemplateDefinitionStart
		{
		mTemplateDefinitionStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred1_Templating2Lexer

	// $ANTLR start synpred2_Templating2Lexer
	public final void synpred2_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:65: ( MapDefinitionStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:65: MapDefinitionStart
		{
		mMapDefinitionStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred2_Templating2Lexer

	// $ANTLR start synpred3_Templating2Lexer
	public final void synpred3_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:84: ( FeatureCallStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:84: FeatureCallStart
		{
		mFeatureCallStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred3_Templating2Lexer

	// $ANTLR start synpred4_Templating2Lexer
	public final void synpred4_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:101: ( PropertyCallStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:101: PropertyCallStart
		{
		mPropertyCallStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred4_Templating2Lexer

	// $ANTLR start synpred5_Templating2Lexer
	public final void synpred5_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:119: ( NameStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:119: NameStart
		{
		mNameStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred5_Templating2Lexer

	// $ANTLR start synpred6_Templating2Lexer
	public final void synpred6_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:129: ( ValueStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:129: ValueStart
		{
		mValueStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred6_Templating2Lexer

	// $ANTLR start synpred7_Templating2Lexer
	public final void synpred7_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:140: ( OddStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:140: OddStart
		{
		mOddStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred7_Templating2Lexer

	// $ANTLR start synpred8_Templating2Lexer
	public final void synpred8_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:149: ( EvenStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:149: EvenStart
		{
		mEvenStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred8_Templating2Lexer

	// $ANTLR start synpred9_Templating2Lexer
	public final void synpred9_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:159: ( LinkStart )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:159: LinkStart
		{
		mLinkStart(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred9_Templating2Lexer

	// $ANTLR start synpred10_Templating2Lexer
	public final void synpred10_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:169: ( Index )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:169: Index
		{
		mIndex(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred10_Templating2Lexer

	// $ANTLR start synpred11_Templating2Lexer
	public final void synpred11_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:175: ( GmlId )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:175: GmlId
		{
		mGmlId(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred11_Templating2Lexer

	// $ANTLR start synpred12_Templating2Lexer
	public final void synpred12_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:181: ( ExplicitTemplateEnd )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:181: ExplicitTemplateEnd
		{
		mExplicitTemplateEnd(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred12_Templating2Lexer

	// $ANTLR start synpred13_Templating2Lexer
	public final void synpred13_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:201: ( UrlWithPort )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:201: UrlWithPort
		{
		mUrlWithPort(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred13_Templating2Lexer

	// $ANTLR start synpred14_Templating2Lexer
	public final void synpred14_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:213: ( Url )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:213: Url
		{
		mUrl(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred14_Templating2Lexer

	// $ANTLR start synpred15_Templating2Lexer
	public final void synpred15_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:217: ( Equals )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
		{
		if ( input.LA(1)=='=' ) {
			input.consume();
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			recover(mse);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred15_Templating2Lexer

	// $ANTLR start synpred16_Templating2Lexer
	public final void synpred16_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:224: ( Star )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
		{
		if ( input.LA(1)=='*' ) {
			input.consume();
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			recover(mse);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred16_Templating2Lexer

	// $ANTLR start synpred17_Templating2Lexer
	public final void synpred17_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:229: ( Comma )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
		{
		if ( input.LA(1)==',' ) {
			input.consume();
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			recover(mse);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred17_Templating2Lexer

	// $ANTLR start synpred18_Templating2Lexer
	public final void synpred18_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:235: ( Colon )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
		{
		if ( input.LA(1)==':' ) {
			input.consume();
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			recover(mse);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred18_Templating2Lexer

	// $ANTLR start synpred19_Templating2Lexer
	public final void synpred19_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:241: ( TagOpen )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
		{
		if ( input.LA(1)=='<' ) {
			input.consume();
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			recover(mse);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred19_Templating2Lexer

	// $ANTLR start synpred20_Templating2Lexer
	public final void synpred20_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:249: ( TagClose )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
		{
		if ( input.LA(1)=='>' ) {
			input.consume();
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			recover(mse);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred20_Templating2Lexer

	// $ANTLR start synpred21_Templating2Lexer
	public final void synpred21_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:258: ( MapSpace )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:258: MapSpace
		{
		mMapSpace(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred21_Templating2Lexer

	// $ANTLR start synpred22_Templating2Lexer
	public final void synpred22_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:267: ( Not )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:267: Not
		{
		mNot(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred22_Templating2Lexer

	// $ANTLR start synpred23_Templating2Lexer
	public final void synpred23_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:271: ( BracketLeft )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
		{
		if ( input.LA(1)=='(' ) {
			input.consume();
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			recover(mse);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred23_Templating2Lexer

	// $ANTLR start synpred24_Templating2Lexer
	public final void synpred24_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:283: ( BracketRight )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:
		{
		if ( input.LA(1)==')' ) {
			input.consume();
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			recover(mse);
			throw mse;
		}
		}

	}
	// $ANTLR end synpred24_Templating2Lexer

	// $ANTLR start synpred25_Templating2Lexer
	public final void synpred25_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:296: ( Kvp )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:296: Kvp
		{
		mKvp(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred25_Templating2Lexer

	// $ANTLR start synpred26_Templating2Lexer
	public final void synpred26_Templating2Lexer_fragment() throws  {
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:300: ( ID )
		// /home/stranger/checkouts/deegree3/deegree-core/deegree-core-featureinfo/src/main/antlr3/org/deegree/featureinfo/templating/Templating2Lexer.g:1:300: ID
		{
		mID(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred26_Templating2Lexer

	public final boolean synpred6_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred6_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred23_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred23_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred24_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred24_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred3_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred3_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred9_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred9_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred14_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred14_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred5_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred5_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred4_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred4_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred22_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred22_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred1_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred18_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred18_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred25_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred25_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred10_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred10_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred17_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred17_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred15_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred15_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred12_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred12_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred21_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred21_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred16_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred16_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred26_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred26_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred11_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred11_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred13_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred13_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred8_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred8_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred19_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred19_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred20_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred20_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred7_Templating2Lexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred7_Templating2Lexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}



}
