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

import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.AnnotatedTableService;

import java.util.Set;

class BaseTableService<T> {

    AnnotatedTableService<T> tableService;

    final Set<String> columnsSet;
    final String[] columnsArray;

    BaseTableService(final TableServiceInterface tableServiceInterface, final Class<T> recordClass)
        throws NoSuchMethodException {
        tableService = new AnnotatedTableService<>(tableServiceInterface, recordClass);
        tableService.createUpdateTable();
        tableService.createUpdateFields();
        columnsSet = this.tableService.getColumns().keySet();
        columnsArray = columnsSet.toArray(new String[0]);
    }
}
