/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.mailbox;

public enum MailboxFieldEnum {

	folder,

	message_number,

	message_id,

	subject,

	from_address,

	from_personal,

	content_type,

	content_id,

	received_date,

	send_date,

	html_content,

	plain_content,

	recipient_to_address,

	recipient_to_personal,

	recipient_cc_address,

	recipient_cc_personal,

	recipient_bcc_address,

	recipient_bcc_personal,

	reply_to_address,

	reply_to_personal,

	flags,

	email_attachment_name,

	email_attachment_type,

	email_attachment_content;

	public static final String[] labelArray;

	static {
		labelArray = new String[values().length];
		int i = 0;
		for (MailboxFieldEnum e : values())
			labelArray[i++] = e.name();
	}
}
