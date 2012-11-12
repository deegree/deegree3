grammar Dimensions;

@parser::header {
package org.deegree.layer.dims;
}

@lexer::header {
package org.deegree.layer.dims;
}

@parser::members {
  public List<Object> values = new ArrayList<Object>();
  public String error;

  public void displayRecognitionError(String[] tokenNames,
                                    RecognitionException e) {
    String hdr = getErrorHeader(e);
    String msg = getErrorMessage(e, tokenNames);
    error = hdr + ":" + msg;
  }

  protected void mismatch(IntStream input, int ttype, BitSet follow) throws MismatchedTokenException {
    throw new MismatchedTokenException(ttype, input);
  }

  public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
    throw e;
  }
}

@rulecatch {
  catch (RecognitionException e) {
    throw e;
  }
}

dimensionvalues:
  value (',' value)*
  ;
catch[RecognitionException e] {
  if(error == null) {
    error = "Expected another value after " + values + ".";
  }
  throw e;
}

value:
  interval { values.add($interval.iv); }
  | v = VALUE { values.add($v.text); }
  ;

interval returns [DimensionInterval iv]:
  v1 = VALUE '/' v2 = VALUE '/' res = VALUE { $iv = new DimensionInterval($v1.text, $v2.text, $res.text); }
  | v1 = VALUE '/' v2 = VALUE '/' { $iv = new DimensionInterval($v1.text, $v2.text, 0); }
  | v1 = VALUE '/' v2 = VALUE { $iv = new DimensionInterval($v1.text, $v2.text, 0); }
  ;
catch[RecognitionException e] {
  error = "Missing max value for interval.";
  throw e;
}

WS:
  (' ' | '\t' | '\r' | '\n') { $channel = HIDDEN; }
  ;

VALUE:
  ('a'..'z' | 'A'..'Z' | '0'..'9' | '.' | '-' | '+' | ':')+
  ;
