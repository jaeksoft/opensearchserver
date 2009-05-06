/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentCatalog;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.util.PDFTextStripper;

public class PdfParser extends Parser {

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {
		PDDocument pdf = null;
		try {
			pdf = PDDocument.load(inputStream);
			PDDocumentInformation info = pdf.getDocumentInformation();
			if (info != null) {
				basketDocument.addIfNoEmpty("title", info.getTitle());
				basketDocument.addIfNoEmpty("subject", info.getSubject());
				basketDocument.addIfNoEmpty("author", info.getAuthor());
				basketDocument.addIfNoEmpty("producer", info.getProducer());
				basketDocument.addIfNoEmpty("keywords", info.getKeywords());
				if (info.getCreationDate() != null)
					basketDocument.addIfNoEmpty("creation_date", info
							.getCreationDate().getTime());
				if (info.getModificationDate() != null)
					basketDocument.addIfNoEmpty("modification_date", info
							.getModificationDate().getTime());
			}
			PDDocumentCatalog catalog = pdf.getDocumentCatalog();
			if (catalog != null) {
				basketDocument.addIfNoEmpty("language", catalog.getLanguage());
			}
			int pages = pdf.getNumberOfPages();
			basketDocument.addIfNoEmpty("number_of_pages", pages);
			for (int page = 0; page < pages; page++) {
				PDFTextStripper stripper = new PDFTextStripper();
				stripper.setStartPage(page);
				stripper.setEndPage(page);
				String text = stripper.getText(pdf);
				String[] frags = text.split("\\n");
				for (String frag : frags)
					basketDocument.addIfNoEmpty("content", frag.replaceAll(
							"\\s+", " ").trim());
			}
		} finally {
			if (pdf != null)
				pdf.close();
		}
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Unsupported");
	}
}
