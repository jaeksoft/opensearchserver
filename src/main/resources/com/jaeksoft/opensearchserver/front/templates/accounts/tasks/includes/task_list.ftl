<#if tasks?has_content>
        <table class="table table-hover">
            <thead class="thead-dark">
            <tr>
                <th>Session start time</th>
                <#if show_crawl>
                <th>Type</th>
                <th>Crawl</th>
                </#if>
                <th>Index</th>
                <th>Status</th>
                <th>Status time</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <#list tasks as task>
            <tr>
                <td>${task.record.sessionTimeId?number_to_datetime}</td>
                <#if show_crawl>
                <td>${task.record.type?capitalize}</td>
                <td><#if task.crawlName??>
                    <a href="${task.crawlPath!'#'}">${task.crawlName}</a></#if>
                </td>
                </#if>
                <td><#if task.indexName??><a href="${task.indexPath!'#'}">${task.indexName}</a></#if></td>
                <td>${task.record.status!'Unknown'?capitalize}</td>
                <td><#if task.record.statusTime??>${task.record.statusTime?number_to_datetime}</#if></td>
                <td align="right">
                    <a href="/accounts/${account.name?url}/tasks/${task.record.taskId?html}"
                       class=" btn btn-sm btn-info">Info</a>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>
    <#if paging?has_content>
            <br/>
            <form>
                <#if rows?has_content><input type="hidden" name="rows" value="${rows?c}"></#if>
                <#include '../../../includes/paging.ftl'>
            </form>
    </#if>
<#else>
        <p class="text-center text-muted"><em>No task</em></p>
</#if>