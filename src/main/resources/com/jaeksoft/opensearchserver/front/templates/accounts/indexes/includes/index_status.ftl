<div class="card">
    <div class="card-body">
        <h5 class="card-title">Indexing status</h5>
        <ul class="list-group list-group-flush">
        <#list indexStatusCount as status, count>
            <li class="list-group-item list-group-item-${status.css}">${status} : ${count!0}</li>
        </#list>
        </ul>
    </div>
</div>