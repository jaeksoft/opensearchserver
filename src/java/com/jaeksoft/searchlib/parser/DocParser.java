/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.extractor.WordExtractor;

public class DocParser extends Parser {

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		WordExtractor word = new WordExtractor(inputStream);

		SummaryInformation info = word.getSummaryInformation();
		if (info != null) {
			basketDocument.addIfNoEmpty("title", info.getTitle());
			basketDocument.addIfNoEmpty("author", info.getAuthor());
			basketDocument.addIfNoEmpty("subject", info.getSubject());
		}

		String[] paragraphes = word.getParagraphText();
		for (String paragraph : paragraphes) {
			String[] frags = paragraph.split("\\n");
			for (String frag : frags)
				basketDocument.addIfNoEmpty("content", frag.replaceAll("\\s+",
						" "));
		}
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}
}
