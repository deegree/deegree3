grammar Dimensions;

@parser::members {
  public List<Object> values = new ArrayList<Object>();
  public String error;

  {
      setErrorHandler(new BailErrorStrategy());
  }
}

dimensionvalues:
  value (',' value)*
  ;
catch[ParseCancellationException e] {
  if(error == null) {
    error = "Expected another value after " + values + ".";
  }
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
  [ \t\r\n] -> skip
  ;

VALUE:
  ('a'..'z' | 'A'..'Z' | '0'..'9' | '.' | '-' | '+' | ':')+
  ;
