## How to configure an incremental database crawl process

Having a "full crawl" process is useful in many cases. For instance :
* A problem occured and a full re-crawling is needed
* It is imperative to ensure that all data is properly indexed

A common practice is to run this process weekly.

However, **it can be time consuming**. A lighter alternative is an incremental crawl process, which **only crawls recently modified data**.

### Common answer

A common practice is to write a dedicated crawl process that uses an SQL query patterned after the full one, but with a slight addition. **A `WHERE` clause is used to restrict results**.

This `WHERE` clause must be applied on a field in which the date of last modification (or the date of creation) is stored for each row. For instance, assume an `articles` tab with a `datetime` field called `date_modification`. We could use this SQL query:

```sql
SELECT title, author, content, date_modification FROM articles WHERE date_modification > subdate(CURRENT_DATE, 1)
```

This example uses the MySQL function [`subdate`](http://dev.mysql.com/doc/refman/5.1/en/date-and-time-functions.html#function_subdate). This function can be used to subtract a number of days from a particular date. The MySQL constant [`CURRENT_DATE`](http://dev.mysql.com/doc/refman/5.5/en/date-and-time-functions.html#function_current-date) is, unsurprisingly,  equal to the current date. Thus, `subdate(CURRENT_DATE, 1)` is SQLese for **"one day ago"**.

Thus, this query will retrieve every row modified during the last day.

This example crawl process could be run daily.
### Using a single crawl process for both full and incremental crawling

Cloning a "full" process to derive an "incremental" process can lead to difficulties - as each modification in a process should be immediately echoed in the other. Therefore, the practice explained above may not be suitable.

Thankfully, OpenSearchServer offers its Scheduler and its _variables_. We can use this for an unified crawl process where SQL Query uses one such variable, for instance:

```sql
SELECT title, author, content, date_modification FROM articles %whereClause%
```

Two Scheduler jobs using the `Database crawler - run` task then get created:

* In the first job the variable `%whereClause%` is replaced by "nothing" (`whereClause=`)
* In the second job the variable is replaced by `WHERE date_modification > subdate(CURRENT_DATE, 1)` (`whereClause=WHERE date_modification > subdate(CURRENT_DATE, 1)`)

Read the [How to use variables with the Database Crawler](http://www.opensearchserver.com/documentation/faq/crawling/how_to_use_variables_with_database_crawler.md) page for more.
