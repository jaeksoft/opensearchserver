/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

term : (AND|OR|NOT|QSTRING|STRING)+ ;

// LEXER

AND : ('AND'|'ET'|'UND') ;
OR : ('OR'|'OU'|'ODER') ;
NOT : ('NOT'|'NON'|'NICHTS') ;
STRING : StringElement+ EOF? ;
QSTRING : '"' (StringElement|WhiteSpaces)* ('"'|EOF) ;
WS : WhiteSpaces -> channel(HIDDEN) ;
 
fragment WhiteSpaces      : ( '\u0020' | '\u0009' | '\u000D' | '\u000A' ) ; 
fragment StringElement    : '\u0021'|'\u0023' .. '\u007F' |  CharEscapeSeq ;
fragment CharEscapeSeq    : '\\' ('b' | 't' | 'n' | 'f' | 'r' | '"' | '\'' | '\\');