/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.TaskExecutionRecord;
import com.jaeksoft.opensearchserver.model.TaskInfos;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;

import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskExecutionService extends BaseTableService<TaskExecutionRecord> {

    private final Map<String, TaskProcessor<?>> tasksProcessors;

    public TaskExecutionService(final TableServiceInterface tableServiceInterface,
        final Map<String, TaskProcessor<?>> tasksProcessors) throws NoSuchMethodException {
        super(tableServiceInterface, TaskExecutionRecord.class);
        this.tasksProcessors = tasksProcessors;
    }

    TaskProcessor<?> getTasksProcessor(final String taskType) {
        return tasksProcessors.getOrDefault(taskType, TaskProcessor.DEFAULT);
    }

    private int getNextTaskExecutions(final UUID accountId, final int sizeLimit, final long timeLimit, int start,
        int rows, final Collection<TaskExecutionRecord> collector) {
        final TableRequestResultRecords<TaskExecutionRecord> results;
        try {
            results = tableService.queryRows(TableRequest.from(0, sizeLimit)
                .column(columnsArray)
                .query(new TableQuery.StringTerm("accountId", accountId.toString()))
                .build());
        } catch (IOException | ReflectiveOperationException e) {
            throw new InternalServerErrorException(e);
        }
        if (results == null || results.records == null || results.records.isEmpty())
            return 0;
        final List<TaskExecutionRecord> sortableRecords = new ArrayList<>(results.records);
        sortableRecords.sort(Comparator.comparingLong(r -> r.nextExecutionTime));
        final Iterator<TaskExecutionRecord> iterator = sortableRecords.iterator();
        while (iterator.hasNext() && start-- > 0)
            iterator.next();
        while (iterator.hasNext() && rows-- > 0) {
            final TaskExecutionRecord taskExecutionRecord = iterator.next();
            if (taskExecutionRecord.nextExecutionTime > timeLimit)
                break;
            collector.add(taskExecutionRecord);
        }
        return results.count.intValue();
    }

    List<TaskExecutionRecord> getImmediateTaskExecutions(final AccountRecord account) {
        final List<TaskExecutionRecord> taskExecutionRecords = new ArrayList<>();
        final int rows = account.getCrawlNumberLimit();
        getNextTaskExecutions(account.getId(), rows, System.currentTimeMillis(), 0, rows, taskExecutionRecords);
        return taskExecutionRecords.size() > rows ? taskExecutionRecords.subList(0, rows) : taskExecutionRecords;
    }

    public int collectFutureExecutions(final AccountRecord account, final int start, final int rows,
        final Collection<TaskExecutionRecord> collector) {
        return getNextTaskExecutions(account.getId(), account.getCrawlNumberLimit(), Long.MAX_VALUE, start, rows,
            collector);
    }

    void upsertTaskExecution(final TaskRecord taskRecord, final TaskInfos taskInfos) {
        final TaskExecutionRecord taskExecutionRecord =
            TaskExecutionRecord.of(taskRecord.getAccountId(), taskRecord.getTaskId())
                .nextExecutiontime(System.currentTimeMillis())
                .build();
        tableService.upsertRow(taskExecutionRecord.id, taskExecutionRecord);
    }

    boolean removeTaskExecution(final UUID accountId, final String taskId) {
        return tableService.deleteRow(TaskExecutionRecord.of(accountId, taskId).build().id);
    }

    public boolean checkTaskStatus(final TaskRecord taskRecord) {
        final TaskProcessor taskProcessor = getTasksProcessor(taskRecord.type);
        switch (taskRecord.getStatus()) {
        case PAUSED:
            taskProcessor.abort(taskRecord.taskId);
            return removeTaskExecution(taskRecord.getAccountId(), taskRecord.taskId);
        case ACTIVE:
            upsertTaskExecution(taskRecord, taskProcessor.getTaskInfos(taskRecord));
            return true;
        }
        return false;
    }
}
