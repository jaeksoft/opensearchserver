/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class MapiMsgParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.email_display_from,
			ParserFieldEnum.email_display_to, ParserFieldEnum.email_display_cc,
			ParserFieldEnum.email_display_bcc,
			ParserFieldEnum.email_conversation_topic, ParserFieldEnum.subject,
			ParserFieldEnum.content, ParserFieldEnum.creation_date,
			ParserFieldEnum.email_recipient_address,
			ParserFieldEnum.email_recipient_name, ParserFieldEnum.htmlSource,
			ParserFieldEnum.lang };

	public MapiMsgParser() {
		super(fl);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException, SearchLibException {
		MAPIMessage msg = new MAPIMessage(streamLimiter.getNewInputStream());
		msg.setReturnNullOnMissingChunk(true);
		ParserResultItem result = getNewParserResultItem();
		try {
			result.addField(ParserFieldEnum.email_display_from,
					msg.getDisplayFrom());
			result.addField(ParserFieldEnum.email_display_to,
					msg.getDisplayTo());
			result.addField(ParserFieldEnum.email_display_cc,
					msg.getDisplayCC());
			result.addField(ParserFieldEnum.email_display_bcc,
					msg.getDisplayBCC());
			result.addField(ParserFieldEnum.subject, msg.getSubject());
			result.addField(ParserFieldEnum.htmlSource, msg.getHtmlBody());
			result.addField(ParserFieldEnum.content, msg.getTextBody());
			result.addField(ParserFieldEnum.creation_date, msg.getMessageDate());
			result.addField(ParserFieldEnum.email_conversation_topic,
					msg.getConversationTopic());
			RecipientChunks[] recipientChuncksList = msg
					.getRecipientDetailsChunks();
			if (recipientChuncksList != null) {
				for (RecipientChunks recipientChunks : recipientChuncksList) {
					result.addField(ParserFieldEnum.email_recipient_name,
							recipientChunks.getRecipientName());
					result.addField(ParserFieldEnum.email_recipient_address,
							recipientChunks.getRecipientEmailAddress());
				}
			}
			if (StringUtils.isEmpty(msg.getHtmlBody()))
				result.langDetection(10000, ParserFieldEnum.content);
			else
				result.langDetection(10000, ParserFieldEnum.htmlSource);
		} catch (ChunkNotFoundException e) {
			Logging.warn(e);
		}
	}
}
