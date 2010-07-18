/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.analysis.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassFactory;
import com.jaeksoft.searchlib.config.Config;

public abstract class TokenizerFactory extends ClassFactory {

	final private static String TOKENIZER_PACKAGE = "com.jaeksoft.searchlib.analysis.tokenizer";

	public abstract Tokenizer create(Reader reader);

	public static TokenizerFactory getDefaultTokenizer(Config config)
			throws SearchLibException {
		return (TokenizerFactory) ClassFactory.create(config,
				TOKENIZER_PACKAGE, TokenizerEnum.StandardTokenizer.name());
	}

	public static TokenizerFactory create(Config config, String className)
			throws SearchLibException {
		return (TokenizerFactory) ClassFactory.create(config,
				TOKENIZER_PACKAGE, className);
	}

	public static TokenizerFactory create(Config config, Node node)
			throws SearchLibException {
		return (TokenizerFactory) ClassFactory.create(config,
				TOKENIZER_PACKAGE, node);
	}

	public static TokenizerFactory create(TokenizerFactory tokenizer)
			throws SearchLibException {
		return (TokenizerFactory) ClassFactory.create(tokenizer);
	}

}
