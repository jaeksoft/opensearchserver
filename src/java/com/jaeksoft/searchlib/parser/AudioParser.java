/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;

public class AudioParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.artist,
			ParserFieldEnum.album, ParserFieldEnum.title,
			ParserFieldEnum.track, ParserFieldEnum.year, ParserFieldEnum.genre,
			ParserFieldEnum.comment, ParserFieldEnum.album_artist,
			ParserFieldEnum.composer, ParserFieldEnum.grouping };

	public AudioParser() {
		super(fl);
		AudioFileIO.logger.setLevel(Level.OFF);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
	}

	private void closeQuiet(final OutputStream os) {
		if (os == null)
			return;
		try {
			os.close();
		} catch (IOException e) {
		}
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {
		File file = File.createTempFile("oss_temp", "audio_parser");
		OutputStream os = new FileOutputStream(file);
		try {
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1)
				os.write(buffer, 0, bytesRead);
			os.close();
		} finally {
			closeQuiet(os);
		}
		doParseContent(file);
		file.delete();
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Not supported");
	}

	private void addFields(Tag tag, FieldKey fieldKey,
			ParserFieldEnum parserField) {
		List<TagField> list = tag.getFields(fieldKey);
		if (list != null && list.size() > 0) {
			for (TagField field : list)
				addField(parserField, field);
			return;
		}
		String f = tag.getFirst(fieldKey);
		if (f == null)
			return;
		f = f.trim();
		if (f.length() == 0)
			return;
		addField(parserField, f);
	}

	@Override
	public void doParseContent(File file) throws IOException {
		AudioFile f;
		try {
			f = AudioFileIO.read(file);
		} catch (CannotReadException e) {
			throw new IOException(e);
		} catch (TagException e) {
			throw new IOException(e);
		} catch (ReadOnlyFileException e) {
			throw new IOException(e);
		} catch (InvalidAudioFrameException e) {
			throw new IOException(e);
		}
		Tag tag = f.getTag();
		addFields(tag, FieldKey.TITLE, ParserFieldEnum.title);
		addFields(tag, FieldKey.ARTIST, ParserFieldEnum.artist);
		addFields(tag, FieldKey.ALBUM, ParserFieldEnum.album);
		addFields(tag, FieldKey.YEAR, ParserFieldEnum.year);
		addFields(tag, FieldKey.TRACK, ParserFieldEnum.track);
		addFields(tag, FieldKey.ALBUM_ARTIST, ParserFieldEnum.album_artist);
		addFields(tag, FieldKey.COMMENT, ParserFieldEnum.comment);
		addFields(tag, FieldKey.COMPOSER, ParserFieldEnum.composer);
		addFields(tag, FieldKey.GROUPING, ParserFieldEnum.grouping);
	}
}
