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
import java.util.ArrayList;
import java.util.regex.Pattern;

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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.index.IndexDocument;

public class MalletLearner implements LearnerInterface {

	private InstanceList instances;

	private NaiveBayesTrainer trainer;

	private File instancesFile;

	@Override
	public void init(Client client, Learner learner) {
		// TODO Auto-generated method stub
		System.out.println("Learner - init");
		instancesFile = new File(client.getLearnerDirectory(),
				learner.getName() + ".data");
		if (instancesFile.exists()) {
			instances = InstanceList.load(instancesFile);
			instances.setPipe(buildPipe());
		} else
			instances = new InstanceList(buildPipe());
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

	@Override
	public void flush() {
		instances.save(instancesFile);
		// trainer = new NaiveBayesTrainer.Factory().newClassifierTrainer();
		// trainer.train(instances);
	}

	@Override
	public void learn(Client client, IndexDocument document) {
		String art_texte = document.getFieldValueString("art_texte", 0);
		String rub_id = document.getFieldValueString("rub_id", 0);
		String art_id = document.getFieldValueString("art_id", 0);
		System.out.println("Learner - learn + " + art_id + " - " + rub_id);
		if (rub_id == null || art_id == null || art_texte == null)
			return;
		if (rub_id.length() == 0 || art_id.length() == 0
				|| art_texte.length() == 0)
			return;
		instances.addThruPipe(new Instance(art_texte, rub_id, art_id, null));
	}
}
