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

package com.jaeksoft.searchlib.learning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class MalletLearner implements LearnerInterface {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Client client;

	private InstanceList instances;

	private Classifier classifier;

	private File instancesFile;

	@Override
	public void init(File instancesFile) {
		rwl.w.lock();
		try {
			classifier = null;
			this.instancesFile = instancesFile;
			if (instancesFile.exists()) {
				instances = InstanceList.load(instancesFile);
			} else
				instances = new InstanceList(buildPipe());
		} finally {
			rwl.w.unlock();
		}
	}

	private Pipe buildPipe() {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Regular expression for what constitutes a token.
		// This pattern includes Unicode letters, Unicode numbers,
		// and the underscore character. Alternatives:
		// "\\S+" (anything not whitespace)
		// "\\w+" ( A-Z, a-z, 0-9, _ )
		// "[\\p{L}\\p{N}_]+|[\\p{P}]+" (a group of only letters and numbers OR
		// a group of only punctuation marks)
		Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");

		// Tokenize raw strings
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));

		// Normalize all tokens to all lowercase
		pipeList.add(new TokenSequenceLowercase());

		// Remove stopwords from a standard English stoplist.
		// options: [case sensitive] [mark deletions]
		// pipeList.add(new TokenSequenceRemoveStopwords(false, false));

		// Rather than storing tokens as strings, convert
		// them to integers by looking them up in an alphabet.
		pipeList.add(new TokenSequence2FeatureSequence());

		// Do the same thing for the "target" field:
		// convert a class label string to a Label object,
		// which has an index in a Label alphabet.
		pipeList.add(new Target2Label());

		// Now convert the sequence of features to a sparse vector,
		// mapping feature IDs to counts.
		pipeList.add(new FeatureSequence2FeatureVector());

		// Print out the features and the label
		// pipeList.add(new PrintInputAndTarget());

		return new SerialPipes(pipeList);
	}

	private void learn(IndexDocument document) {
		String data = document.getFieldValueString("data", 0);
		String target = document.getFieldValueString("target", 0);
		String name = document.getFieldValueString("name", 0);
		String source = document.getFieldValueString("source", 0);
		if (data == null || target == null || name == null)
			return;
		if (target.length() == 0 || name.length() == 0 || data.length() == 0)
			return;
		instances.addThruPipe(new Instance(data, target, name, source));
	}

	@Override
	public void learn(SearchRequest request, FieldMap sourceFieldMap,
			TaskLog taskLog) throws SearchLibException, IOException {
		rwl.w.lock();
		try {
			int start = 0;
			final int rows = 50;
			request.setRows(rows);
			request.setQueryString("*:*");
			for (;;) {
				request.setStart(start);
				AbstractResultSearch result = (AbstractResultSearch) client
						.request(request);
				if (result.getDocumentCount() == 0)
					break;
				for (int i = 0; i < result.getDocumentCount(); i++) {
					IndexDocument target = new IndexDocument();
					sourceFieldMap.mapIndexDocument(result.getDocument(i),
							target);
					learn(target);
				}
				request.reset();
				start += rows;
			}
			instances.save(instancesFile);
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void reset() {
		if (instancesFile.exists())
			instancesFile.delete();
		init(instancesFile);
	}

	private final String[] MALLET_SOURCE_FIELDS = { "data", "target", "name",
			"source" };

	@Override
	public String[] getSourceFieldList() {
		return MALLET_SOURCE_FIELDS;
	}

	private final String[] MALLET_TARGET_FIELDS = { "label", "score" };

	@Override
	public String[] getTargetFieldList() {
		return MALLET_TARGET_FIELDS;
	}

	private Classifier checkClassifier() {
		rwl.r.lock();
		try {
			if (classifier != null)
				return classifier;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (classifier != null)
				return classifier;
			NaiveBayesTrainer trainer = new NaiveBayesTrainer();
			classifier = trainer.train(instances);
			return classifier;
		} finally {
			rwl.w.unlock();
		}

	}

	@Override
	public void classify(IndexDocument source, FieldMap sourceFieldMap,
			FieldMap targetFieldMap, int maxRank, double minScore)
			throws IOException {
		classifier = checkClassifier();
		IndexDocument target = new IndexDocument();
		sourceFieldMap.mapIndexDocument(source, target);
		String data = target.getFieldValueString("data", 0);
		if (data == null)
			return;
		IndexDocument source2 = new IndexDocument();
		Classification cf = classifier.classify(data);
		Labeling labeling = cf.getLabeling();
		if (maxRank == 0 || maxRank > labeling.getLabelAlphabet().size())
			maxRank = labeling.getLabelAlphabet().size();
		for (int i = 0; i < maxRank; i++) {
			double score = labeling.getValueAtRank(i);
			if (score < minScore)
				break;
			source2.add("label", Double.toString(score), 1.0F);
			source2.add("score", labeling.getLabelAtRank(i).toString(), 1.0F);
		}
		targetFieldMap.mapIndexDocument(source2, source);
	}

	private final static Comparator<Double> REVERSE_DOUBLE_COMPARATOR = new Comparator<Double>() {
		@Override
		public int compare(Double o1, Double o2) {
			return o2.compareTo(o1);
		}
	};

	@Override
	public Map<Double, String> classify(String data, int maxRank,
			double minScore) throws IOException {
		classifier = checkClassifier();
		Classification cf = classifier.classify(data);
		Labeling labeling = cf.getLabeling();
		if (maxRank == 0 || maxRank > labeling.getLabelAlphabet().size())
			maxRank = labeling.getLabelAlphabet().size();
		Map<Double, String> map = new TreeMap<Double, String>(
				REVERSE_DOUBLE_COMPARATOR);
		for (int i = 0; i < maxRank; i++) {
			double score = labeling.getValueAtRank(i);
			if (score < minScore)
				break;
			map.put(score, labeling.getLabelAtRank(i).toString());
		}
		return map;
	}
}
