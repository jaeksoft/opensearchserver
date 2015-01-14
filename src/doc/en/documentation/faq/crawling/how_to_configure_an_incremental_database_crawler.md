## How to configure an incremental Database crawl process

Having a "full crawl" process is useful in many cases: when a problem occured and a full re-crawling is needed, or if we want to ensure every data is properly indexed. It is a process that can be run for example weekly.

However, **it can be time consuming**. That is why we often configure some incremental crawl process, which **only crawls data modified recently**.

### Common answer

What users often do is simply writing a dedicated crawl process which uses almost the same SQL query than the "full" one, but with a slight addition: **a `WHERE` clause is used to restrict results**.

This `WHERE` clause must be applied on a field in which date of last modification (or date of creation) is stored for each row. Let's imagine for example a table `articles` having a field called `date_modification`, of type `datetime`. We could use this SQL query:

```sql
SELECT title, author, content, date_modification FROM articles WHERE date_modification > subdate(CURRENT_DATE, 1)
```

Here we are using the MySQL function [`subdate`](http://dev.mysql.com/doc/refman/5.1/en/date-and-time-functions.html#function_subdate). This function can be used to substract a number of days to a particular date. The MySQL constant [`CURRENT_DATE`](http://dev.mysql.com/doc/refman/5.5/en/date-and-time-functions.html#function_current-date) is, surprisingly,  equal to the current date. Thus, `subdate(CURRENT_DATE, 1)` simply means **"one day ago"**.

The above query will retrieve every rows modified during the last day.

This crawl process could be run daily for example.

### Using only one crawl process for full and incremental crawling

Cloning a "full" process for configuring an "incremental" process could lead to hard to maintain settings (each modification in a process should be immediately reported to the other).

Hopefully, this situation can be avoided, using the Scheduler and the _variables_. We could think of an unique crawl process where SQL Query would use one variable, for instance:

```sql
SELECT title, author, content, date_modification FROM articles %whereClause%
```

Two jobs of schedulers using the `Database crawler - run` task would be created:

* In one of the job the variable `%whereClause%` would be replaced by "nothing" (`whereClause=`)
* In the second job the variable would be replace by `WHERE date_modification > subdate(CURRENT_DATE, 1)` (`whereClause=WHERE date_modification > subdate(CURRENT_DATE, 1)`)

Read the [How to use variables with the Database Crawler](http://www.opensearchserver.com/documentation/faq/crawling/how_to_use_variables_with_database_crawler.md) page to learn it all.  
