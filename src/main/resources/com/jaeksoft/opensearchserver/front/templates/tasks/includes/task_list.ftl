<#if tasks.records?has_content>
        <table class="table table-hover">
            <thead class="thead-dark">
            <tr>
                <th>Creation time</th>
                <th>Type</th>
                <#if crawlResolver??>
                <th>Crawl</th>
                </#if>
                <#if indexResolver??>
                <th>Index</th>
                </#if>
                <th>Status</th>
                <th>Status time</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#list tasks.records as task>
            <tr>
                <td>${task.creationTime?number_to_datetime}</td>
                <td>${task.type?capitalize}</td>
                <#if crawlResolver??>
                <td>${crawlResolver[task.taskId]!}</td>
                </#if>
                <#if indexResolver??>
                <td>${indexResolver[task.taskId]!}</td>
                </#if>
                <td>${task.status!'Unknown'?capitalize}</td>
                <td><#if task.statusTime??>${task.statusTime?number_to_datetime}</#if></td>
                <td align="right">
                    <a href="/tasks/${task.taskId?html}"
                       class=" btn btn-sm btn-info">Info</a>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>
<#else>
        <p class="text-center text-muted"><em>No task</em></p>
</#if>