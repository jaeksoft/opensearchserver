/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.services;

import org.junit.Assert;
import org.junit.Test;

public class ScoreProviderTest {

	@Test
	public void testRecentScore() {
		final float[] expected = { 15f, 12.3f, 10.1f, 8.3f, 6.8F, 5.8f, 5.2f, 5.0f, 5.0f, 5.0f };

		final SearchService.RecentScore recentScore = new SearchService.RecentScore(null);
		final long currentTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			final long time = currentTime - 1000 * 60 * 60 * 24 * i;
			Assert.assertEquals(expected[i], recentScore.customScore(0, 5.0f, time), 0.1);
		}
	}
}
