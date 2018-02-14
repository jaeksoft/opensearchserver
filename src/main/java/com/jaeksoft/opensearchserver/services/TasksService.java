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

import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.qwazr.store.StoreServiceInterface;

import java.util.UUID;

public class TasksService extends StoreService<TaskRecord> {

	private final static String DIRECTORY = "tasks";

	public TasksService(final StoreServiceInterface storeService, final String storeSchema) {
		super(storeService, storeSchema, DIRECTORY, TaskRecord.class);
	}

	@Override
	protected UUID getUuid(final TaskRecord record) {
		return record.getUuid();
	}
}
