/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class EmlParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES = { "message/rfc822" };

	public static final String[] DEFAULT_EXTENSIONS = { "eml" };

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.email_display_from,
			ParserFieldEnum.email_display_to, ParserFieldEnum.email_display_cc,
			ParserFieldEnum.email_display_bcc,
			ParserFieldEnum.email_conversation_topic, ParserFieldEnum.subject,
			ParserFieldEnum.content, ParserFieldEnum.email_sent_date,
			ParserFieldEnum.email_received_date,
			ParserFieldEnum.email_attachment_name,
			ParserFieldEnum.email_attachment_type,
			ParserFieldEnum.email_attachment_content,
			ParserFieldEnum.htmlSource, ParserFieldEnum.lang };

	public EmlParser() {
		super(fl);
	}

	private final static Properties JAVAMAIL_PROPS = new Properties();

	static {
		JAVAMAIL_PROPS.put("mail.host", "localhost");
		JAVAMAIL_PROPS.put("mail.transport.protocol", "smtp");
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException, SearchLibException {
		Session session = Session.getDefaultInstance(JAVAMAIL_PROPS);
		try {

			MimeMessage mimeMessage = new MimeMessage(session,
					streamLimiter.getNewInputStream());
			MimeMessageParser mimeMessageParser = new MimeMessageParser(
					mimeMessage).parse();

			ParserResultItem result = getNewParserResultItem();
			String from = mimeMessageParser.getFrom();
			if (from != null)
				result.addField(ParserFieldEnum.email_display_from,
						from.toString());
			for (Address address : mimeMessageParser.getTo())
				result.addField(ParserFieldEnum.email_display_to,
						address.toString());
			for (Address address : mimeMessageParser.getCc())
				result.addField(ParserFieldEnum.email_display_cc,
						address.toString());
			for (Address address : mimeMessageParser.getBcc())
				result.addField(ParserFieldEnum.email_display_bcc,
						address.toString());
			result.addField(ParserFieldEnum.subject,
					mimeMessageParser.getSubject());
			result.addField(ParserFieldEnum.htmlSource,
					mimeMessageParser.getHtmlContent());
			result.addField(ParserFieldEnum.content,
					mimeMessageParser.getPlainContent());
			result.addField(ParserFieldEnum.email_sent_date,
					mimeMessage.getSentDate());
			result.addField(ParserFieldEnum.email_received_date,
					mimeMessage.getReceivedDate());

			for (DataSource dataSource : mimeMessageParser.getAttachmentList()) {
				result.addField(ParserFieldEnum.email_attachment_name,
						dataSource.getName());
				result.addField(ParserFieldEnum.email_attachment_type,
						dataSource.getContentType());
				if (parserSelector == null)
					continue;
				Parser attachParser = parserSelector.parseStream(
						getSourceDocument(), dataSource.getName(),
						dataSource.getContentType(), null,
						dataSource.getInputStream(), null, null, null);
				if (attachParser == null)
					continue;
				List<ParserResultItem> parserResults = attachParser
						.getParserResults();
				if (parserResults != null)
					for (ParserResultItem parserResult : parserResults)
						result.addField(
								ParserFieldEnum.email_attachment_content,
								parserResult);
			}
			if (StringUtils.isEmpty(mimeMessageParser.getHtmlContent()))
				result.langDetection(10000, ParserFieldEnum.content);
			else
				result.langDetection(10000, ParserFieldEnum.htmlSource);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
