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

package com.jaeksoft.searchlib.request;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexGroup;

public abstract class AbstractGroupRequest<T extends AbstractGroupRequestThread> {

	protected IndexGroup indexGroup;

	protected AbstractGroupRequest(IndexGroup indexGroup) {
		this.indexGroup = indexGroup;
	}

	protected abstract T getNewThread(IndexAbstract index)
			throws ParseException, SyntaxError, IOException;

	final private void run(List<T> threads) throws IOException,
			URISyntaxException, ParseException, SyntaxError,
			ClassNotFoundException {
		for (T thread : threads)
			thread.start();
		Iterator<T> iterator = threads.iterator();
		while (iterator.hasNext()) {
			T thread = iterator.next();
			thread.waitForCompletion();
			thread.exception();
			complete(thread);
			if (thread.done())
				iterator.remove();
		}
		complete();
	}

	/**
	 * Called when all thread execution completed
	 */
	protected abstract void complete() throws IOException, URISyntaxException,
			ParseException, SyntaxError;

	/**
	 * Call each time a thread execution complete
	 * 
	 * @param thread
	 */
	protected abstract void complete(T thread);

	final private List<T> initThreads() throws ParseException, SyntaxError,
			IOException {
		List<T> threads = new ArrayList<T>(indexGroup.size());

		for (IndexAbstract index : indexGroup.getIndices())
			threads.add(getNewThread(index));
		return threads;
	}

	final protected void loop() throws IOException, URISyntaxException,
			ParseException, SyntaxError, ClassNotFoundException {
		List<T> threads = initThreads();
		while (threads.size() > 0)
			run(threads);
	}

	final protected void run() throws ParseException, SyntaxError, IOException,
			URISyntaxException, ClassNotFoundException {
		List<T> threads = initThreads();
		run(threads);
	}
}
