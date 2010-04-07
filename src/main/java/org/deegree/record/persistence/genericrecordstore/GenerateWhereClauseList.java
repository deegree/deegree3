//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.record.persistence.genericrecordstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.commons.utils.Pair;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;

/**
 * Here is the handling for the kinds of expression coming from the {@link PostGISWhereBuilder}. Expressions like <li>
 * <Code>A AND B OR C</Code> =&gt; <Code>A AND B, C</Code></li> <li><Code>A AND B AND (C OR D)</Code> =&gt;
 * <Code>A AND B AND C, A AND B AND D</Code></li> So there is a mathematical representation of the expression coming
 * from the {@link PostGISWhereBuilder}. This class at the beginning splits every element and generates a {@link Pair}
 * of the String-representation and its value as an object. After this is done, there is a splitting of the
 * whereClause-representation into the relevant expressions. With this class it is possible to parse complicated
 * expressions with nested AND/OR operations.
 * 
 * TODO NOT is not implemented yet.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenerateWhereClauseList {

    private StringBuilder whereBuilder;

    private Collection<Object> whereParams;

    /**
     * Generates a new instance of the {@link GenerateWhereClauseList}.
     * 
     * @param whereBuilder
     *            should not be <Code>null</Code>
     * @param whereParams
     *            could be <Code>null</Code>
     */
    public GenerateWhereClauseList( StringBuilder whereBuilder, Collection<Object> whereParams ) {
        this.whereBuilder = whereBuilder;
        this.whereParams = whereParams;

    }

    /**
     * 
     * @return a list of {@link Pair}s, not <Code>null</Code>
     */
    public List<Pair<StringBuilder, Collection<Object>>> generateList() {
        List<Pair<StringBuilder, Collection<Object>>> whereClauseList = new ArrayList<Pair<StringBuilder, Collection<Object>>>();

        StringBuilder whereBuilderForGenerating = this.whereBuilder;
        StringBuilder whereBuilderHelper = this.whereBuilder;

        Collection<Object> whereParamsForGenerating = this.whereParams;
        Collection<Object> whereParamsForGeneratingTemp = new ArrayList<Object>();
        whereParamsForGeneratingTemp.addAll( this.whereParams );

        List<Pair<StringBuilder, Object>> clauseParamPairList = new ArrayList<Pair<StringBuilder, Object>>();
        List<Pair<StringBuilder, Object>> clauseParamPairListTemp = new ArrayList<Pair<StringBuilder, Object>>();
        List<Pair<StringBuilder, Object>> clauseParamPairListTemp2 = new ArrayList<Pair<StringBuilder, Object>>();
        Pair<StringBuilder, Object> clauseParamPair;
        Pattern ANDOR = Pattern.compile( "AND|OR" );
        Pattern bracketForwards = Pattern.compile( "\\(" );
        Pattern bracketBackwards = Pattern.compile( "\\)" );
        Iterator iter = whereParamsForGenerating.iterator();

        String[] match = ANDOR.split( whereBuilderHelper );
        for ( String matchee : match ) {
            clauseParamPair = new Pair<StringBuilder, Object>();

            String[] t = matchee.split( "\\?" );

            matchee = matchee.trim();
            matchee = matchee.replaceAll( "^\\(*", "" );
            matchee = matchee.replaceAll( "\\)*\\z", "" );

            /*
             * if there are brackets inside of the expression that are closed at the end, that causes errors
             */
            int countBracket = 0;

            Matcher mForwards = bracketForwards.matcher( matchee );
            Matcher mBackwards = bracketBackwards.matcher( matchee );
            while ( mForwards.find() ) {
                countBracket++;
            }
            while ( mBackwards.find() ) {
                countBracket--;
            }
            if ( countBracket < 0 ) {
                for ( int i = 0; i > countBracket; i-- ) {
                    matchee = "(" + matchee;
                }
            } else if ( countBracket > 0 ) {
                for ( int i = 0; i < countBracket; i++ ) {
                    matchee = matchee + ")";
                }
            }

            clauseParamPair.first = new StringBuilder( "(" + matchee + ")" );
            if ( t.length > 1 ) {

                clauseParamPair.second = iter.next();
                iter.remove();

            }
            clauseParamPairList.add( clauseParamPair );
        }

        Pair<StringBuilder, Collection<Object>> clauseBuilderPair;
        // cut whereBuilder at position OR

        Pattern pOR = Pattern.compile( "OR" );
        String[] sizeOfORConditions = pOR.split( whereBuilderForGenerating );

        boolean isORContaining = sizeOfORConditions.length == 1 ? false : true;
        if ( isORContaining ) {
            for ( int pointerOfOrConditions = 1; pointerOfOrConditions < sizeOfORConditions.length; pointerOfOrConditions++ ) {
                // gets the first occurence of OR
                int cut = whereBuilderForGenerating.indexOf( "OR" );
                if ( cut <= 0 ) {
                    cut = whereBuilderForGenerating.length();
                }
                String before = whereBuilderForGenerating.substring( 0, cut );
                String after = whereBuilderForGenerating.substring( cut, whereBuilderForGenerating.length() );
                after = after.replace( "OR", "" ).trim();
                before = before.replace( "OR", "" ).trim();
                String reverse = new StringBuffer( before ).reverse().toString().trim();
                String parsedExpression = "";
                String parsedExpressionAfter = "";
                String parsedExpressionBefore = "";
                int countBracket = 0;

                /*
                 * parses the first element before the OR operator this could be an expression, as well. Like (A AND B)
                 */
                for ( int pos = 0; pos <= reverse.length() - 1; pos++ ) {
                    if ( reverse.charAt( pos ) == '(' ) {
                        countBracket--;
                        parsedExpression += reverse.charAt( pos );
                        if ( countBracket != 0 ) {
                            continue;
                        }
                    }
                    if ( reverse.charAt( pos ) == ')' ) {
                        countBracket++;
                        parsedExpression += reverse.charAt( pos );
                        continue;
                    }
                    if ( countBracket != 0 ) {
                        if ( reverse.charAt( pos ) != '(' || reverse.charAt( pos ) != ')' ) {
                            parsedExpression += reverse.charAt( pos );
                            continue;
                        }
                    } else {
                        String reverseReverse = new StringBuffer( parsedExpression ).reverse().toString();
                        clauseBuilderPair = new Pair<StringBuilder, Collection<Object>>();
                        clauseBuilderPair.first = new StringBuilder( reverseReverse.trim() );
                        Collection<Object> col = new ArrayList<Object>();
                        clauseParamPairListTemp.addAll( clauseParamPairList );

                        for ( Pair<StringBuilder, Object> a : clauseParamPairList ) {
                            int i = reverseReverse.indexOf( a.first.toString().trim() );

                            if ( i > -1 ) {
                                if ( a.second != null ) {
                                    col.add( a.second );
                                }
                                reverseReverse = reverseReverse.replace( a.first.toString().trim(), "" );
                                clauseParamPairListTemp.remove( new Pair( a.first, a.second ) );

                            }

                        }
                        clauseParamPairListTemp2.addAll( clauseParamPairListTemp );
                        clauseBuilderPair.second = col;

                        whereClauseList.add( clauseBuilderPair );
                        String g = new StringBuffer( parsedExpression ).reverse().toString();
                        before = before.replace( g, "" ).trim();
                        break;
                    }
                }

                /*
                 * parses the first element after the OR operator this could be an expression, as well. Like (A AND B)
                 */
                for ( int pos = 0; pos <= after.length() - 1; pos++ ) {

                    if ( after.charAt( pos ) == '(' ) {
                        countBracket++;
                        parsedExpressionAfter += after.charAt( pos );
                        continue;
                    }
                    if ( after.charAt( pos ) == ')' ) {
                        countBracket--;
                        parsedExpressionAfter += after.charAt( pos );
                        if ( countBracket != 0 ) {
                            continue;
                        }
                    }
                    if ( countBracket != 0 ) {
                        if ( after.charAt( pos ) != '(' || after.charAt( pos ) != ')' ) {
                            parsedExpressionAfter += after.charAt( pos );
                            continue;
                        }
                    } else {
                        String parsedExpressionAfterTemp = parsedExpressionAfter.trim();
                        clauseBuilderPair = new Pair<StringBuilder, Collection<Object>>();
                        clauseBuilderPair.first = new StringBuilder( parsedExpressionAfterTemp );
                        Collection<Object> col = new ArrayList<Object>();

                        for ( Pair<StringBuilder, Object> a : clauseParamPairListTemp ) {
                            int i = parsedExpressionAfterTemp.indexOf( a.first.toString().trim() );
                            if ( i > -1 ) {
                                if ( a.second != null ) {
                                    col.add( a.second );
                                }
                                parsedExpressionAfterTemp = parsedExpressionAfterTemp.replace(
                                                                                               a.first.toString().trim(),
                                                                                               "" );
                                clauseParamPairListTemp2.remove( new Pair( a.first, a.second ) );
                            }

                        }

                        clauseBuilderPair.second = col;

                        whereClauseList.add( clauseBuilderPair );
                        String g = new StringBuffer( parsedExpressionAfter ).toString();
                        after = after.replace( g, "" ).trim();
                        break;
                    }
                }

                /*
                 * parses the rest of the expression and appends it to the whereClauseList-elements
                 */
                for ( int pos = 0; pos <= before.length() - 1; pos++ ) {

                    if ( before.charAt( pos ) == '(' ) {
                        countBracket++;
                        parsedExpressionBefore += before.charAt( pos );
                        continue;
                    }
                    if ( before.charAt( pos ) == ')' ) {
                        countBracket--;
                        parsedExpressionBefore += before.charAt( pos );
                        if ( countBracket != 0 ) {
                            continue;
                        }
                    }
                    if ( countBracket != 0 ) {
                        if ( before.charAt( pos ) != '(' || before.charAt( pos ) != ')' ) {
                            parsedExpressionBefore += before.charAt( pos );
                            continue;
                        }
                    } else {
                        String expressionBeforeTemp = parsedExpressionBefore.trim();
                        clauseBuilderPair = new Pair<StringBuilder, Collection<Object>>();
                        clauseBuilderPair.first = new StringBuilder( expressionBeforeTemp );
                        Collection<Object> col = new ArrayList<Object>();

                        for ( Pair<StringBuilder, Object> a : clauseParamPairListTemp2 ) {
                            int i = expressionBeforeTemp.indexOf( a.first.toString().trim() );
                            if ( i > -1 ) {
                                if ( a.second != null ) {
                                    col.add( a.second );
                                }
                                expressionBeforeTemp = expressionBeforeTemp.replace( a.first.toString().trim(), "" );

                            }

                        }

                        clauseBuilderPair.second = col;
                        String g = new StringBuffer( parsedExpressionBefore ).toString();
                        after = after.replace( g, "" ).trim();

                        for ( Pair<StringBuilder, Collection<Object>> listlet : whereClauseList ) {
                            listlet.first.append( " AND " + clauseBuilderPair.first );
                            listlet.second.addAll( clauseBuilderPair.second );
                        }
                        before = before.replace( parsedExpressionBefore, "" );
                        before = before.replaceAll( "AND", "" ).trim();
                        before = before.replaceAll( "^[ ]*", "" ).trim();
                        before = before.replaceAll( "[ ]*\\z", "" ).trim();

                        pos = -1;
                        parsedExpressionBefore = "";
                    }
                }
            }
        } else {

            whereClauseList.add( new Pair<StringBuilder, Collection<Object>>( whereBuilderHelper,
                                                                              whereParamsForGeneratingTemp ) );
        }
        return whereClauseList;
    }

}
