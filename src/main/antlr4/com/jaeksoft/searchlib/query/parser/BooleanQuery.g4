/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
grammar BooleanQuery;

// PARSER

expression : term+ ;

term : (AND|OR|NOT|STRING|QSTRING|FIELD)+ ;

// LEXER

AND     : ('AND'|'ET'|'UND') ;
OR      : ('OR'|'OU'|'ODER') ;
NOT     : ('NOT'|'NON'|'NICHTS') ;
STRING  : StringElement+ EOF? ;
QSTRING : '"' (QStringElement|WhiteSpaces)* ('"'|EOF) ;
FIELD   : KeywordElement+ ':' ;
WS      : WhiteSpaces -> channel(HIDDEN) ;
 
fragment KeywordElement : [a-zA-Z_0-9] ;
fragment StringElement  : '\u0021' | '\u0023' .. '\u0039' | '\u003B' .. '\uFFFF' |  CharEscapeSeq ;
fragment QStringElement : '\u0021' | '\u0023' .. '\uFFFF' |  CharEscapeSeq ;
fragment WhiteSpaces    : ( '\u0020' | '\u0009' | '\u000D' | '\u000A' ) ; 
fragment CharEscapeSeq  : '\\' ('b' | 't' | 'n' | 'f' | 'r' | ':' | '"' | '\'' | '\\') ;