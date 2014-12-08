## Crawling a database

This quick OpenSearchServer tutorial will teach you how to crawl a MySQL database and configure a nice-looking search page (with facets, auto-completion, snippets, etc.).

Here is an example of the search form and search results with facets we'll get in the end:

![Example of final results](database_1_renderer.png)

## Create database

As an example we will use a small database with 3 tables. This database is used to store some articles, authors and categories.

Here are some screenshot for these tables:

**Table articles:**

![Table articles](database_table_articles.png)

**Table authors:**

![Table authors](database_table_authors.png)

**Table categories:**

![Table categories](database_table_categories.png)

A query allowing for retrieval of every useful information would be for instance:

```sql
SELECT a.id, a.title, a.content, a.date_created, c.name as category, CONCAT(au.lastname, ' ', au.firstname) as author, UNIX_TIMESTAMP() as time 
FROM articles a 
LEFT JOIN categories c ON c.id = a.id_category 
LEFT JOIN authors au ON au.id = a.id_author
```

Here are the results for this query:

![Example of query results](database_table_results.png)

As you can see, this query gives every useful information on one line for each article. The OpenSearchServer database crawler will be able to read these results and build one document in the index for each line, identified by the column `id`.

> Another useful feature of the crawler is being able to "group" several lines sharing the same unique ID. For instance, if the database use some join tables and the final query returns several lines for one article, but with one different category by line, then the crawler would be able to create only one document in the index by giving some multiple values to its `category` field.
>
> We will not cover this case in this tutorial  but it's nice to know that it's possible!

You could for example download the MySQL scripts here [https://gist.github.com/AlexandreToyer/f00c3eec976e654e211b](https://gist.github.com/AlexandreToyer/f00c3eec976e654e211b) and create a local database on your computer. It could also be a PostgreSQL database or some other kinds of database.

When your database is ready go to OpenSearchServer's interface.

## Create and configure index's schema and analyzers

### Create index

Create an empty index named for instance `articles`.

### Create analyzers

Two analyzers will be needed in order to make some useful transformation on data.

#### Analyzer `_KeepFirstLetterOnly`

This analyzer will be used to index the first letter of the author's name in a particular field. We will then use this field as a facet.

Go to tab `Schema` / `Analyzer` and create this analyzer:

![First analyzer](database_analyzer1.png)

You can test it in the test area in the bottom. It will only keep the first letter of the given text.

#### Analyzer `_KeepYearMonth`

This analyzer will be used to keep only the year and the month from a full date like `2014-10-14 10:25:34`. This data will then be used as a facet to filter on month.

Go to tab `Schema` / `Analyzer` and create this analyzer:

![Second analyzer](database_analyzer_2.png)

The regular expression to use is `([0-9]{4}\-[0-9]{2}).*`.

You can test it in the test area in the bottom.

### Create schema

Go to tab `Schema` and create fields as shown in this screenshot:

![Schema of the index](database_schema.png)

Some fields are created in several versions, each one using a particular analyzer. For example the `title` and `titleStandard` fields will receive the same value (title of the article) but will index it in a different way: the field `title` will use a `TextAnalyzer` and the field `titleStandard` will use a `StandardAnalyzer`. We will then use these fields with some different weight when creating the query.

Have a look at the [How to use analyzers](../faq/indexing/how_to_use_analyzers.md) page to understand it all. 

#### Default and unique field

With lists located at the top of the page (still in tab `Schema`) configure the index with:

* Default field: `content`
* Unique field: `id`

The unique field will be used to uniquely identify the documents. When crawling the database, if an article already existing in the index is found in the database then it will be updated by the content from the database.

## Configure crawler

Here comes the part where we need to actually work with the database.

Go to tab `Crawler` / `Database` and click button `New crawl...`.

Configure the first tab as shown here:
 
![Configuring the crawler](database_crawler_1.png)

Of course you will need to use some customized information:

* Driver class: choose the one matching your type of database
* JDBC URL: this connection string can vary depending on your type of database. For MySQL for example it will be: `jdbc:mysql://<host>:<port_if_any>/<database_name>`
  * Of course the database host must be accessible from the servers used by OpenSearchServer. 
* User: user with read right on your database
* Password: password for this user

The `SQL Select` query is the one explained earlier. The `UNIX_TIMESTAMP()` function will be used to saved the time of indexing. An SQL `CONCAT` is used to concatenate lastname ans firstname of authors.

Click button `Check`. A popup showing name of columns should be displayed:

![Checking query](database_crawler_check.png)

Go to tab `FieldMap` and add these mappings:
 
![Configuring the crawler](database_crawler_2.png)

What this means is quite simple: value of each found column from the SQL query will be indexed into a particular field of the schema. The `Copy of` feature used earlier when creating fields will be used to copy the same value to different fields (no need to add several mappings to those fields here).

Create (or Save) the Crawl and then click on the button with the green icon to start it. It should quickly say "Complete".

> If message "Error" is shown, hover the message with your mouse and more information will be displayed in a tooltip. It may be an SQL error, or you may have forgotten some mappings in the `FieldMap` tab. The "Unique field" of the schema in particular **must have a mapped valued**.

![Starting the crawler](database_crawler_3.png)

## Configure autocompletion

Go to tab `Schema` / `Auto-completion` and create an autocompletion item using the field "autocomplete": 

![Configuring the autocompletion](database_autocompletion.png)

Click button "Create" and in the list below click on the "Build" button to fill in the auto-completion sub-index with values.

## Create a query

Some documents are now indexed we did half the job! We still need to create a query to be able to search for them.

Go to tab `Query` and create a new query (type `Search (field)`, named "search" for instance). 

Configure it as shown below:

![Configuring the query](database_query_1.png)

![Configuring the query](database_query_2.png)

![Configuring the query](database_query_3.png)

![Configuring the query](database_query_4.png)

![Configuring the query](database_query_5.png)

Everything is quite standard with this configuration: 

* we use a AND with a phrase slop of 1
* we search into different fields, each one with a different weight
* we want to return some fields, and create some facets
* we configure some snippets to highlight the searched keywords in the results

Click button "Search" to test it:

![Configuring the query](database_query_6.png)
 
## Create a renderer

The final step is creating a renderer that will use the previously created query template.

Go to tab `Renderer` and configure it as shown below:

**Global configuration:**

![Configuring the renderer](database_renderer_1.png)

Use the query template (request) (`search` here) and the autocompletion created earlier. 

We are using here some Javascript to enhance the display and rename facets. `jQuery` is dynamically loaded from the Google CDN. Full code is:

```javascript
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js" ></script>
<script type="text/javascript">

jQuery(function($) {
    
   // Wrap results in a div
   $('.ossfieldrdr1').each(function() {
      $(this).nextUntil('br').andSelf().wrapAll('<div class="oss-one-result"></div>');
   });
 
  // Rename facets
     var elem = $('h3:contains("date_monthyear")');
     elem.text(elem.text().replace("date_monthyear", "Filter by month"));
     var elem = $('h3:contains("authorFirstLetter")');
     elem.text(elem.text().replace("authorFirstLetter", "Filter by author"));
     var elem = $('h3:contains("categoryKeyword")');
     elem.text(elem.text().replace("categoryKeyword", "Filter by category"));

});

</script>
```


**Fields**

![Configuring the renderer](database_renderer_2.png)

The first two lines are `SNIPPET`, the other ones are `FIELD`. We want here to display the `dateCrawl` and `id` fields for testing purpose.

In order to be able to choose fields from the list `Field / Snippet` you will have to first create the renderer (click "Create") with a query template chosen in list `Request name` (in the first sub-tab), and then edit it again (click "Edit" in the list of renderers and come back to the `Fields` tabs).

**CSS Style**

Configure some customized CSS to enhance the display:

```css
body { font-family: Arial, sans-serif; background:#efedea; color:rgb(124, 112, 94); }
#oss-wrap { width:1000px; margin:20px auto 0 auto; font-size:14px;  line-height: 24px;}

.oss-one-result { background:white; padding:20px; margin:0 0 10px 0; border:1px solid rgb(228, 225, 220);}

/* Search box */
.ossinputrdr { height:30px; padding:3px; font-size:1em;}
/* Search button */
.ossbuttonrdr { height:40px; }
/* Num found */
.ossnumfound { margin:10px 0 0 0; }

/* Title of article */
.ossfieldrdr1 { text-align:center; color:#bd5532; font-family: Tahoma; font-size:2.1em; margin:10px 0 20px 0;}
/* Content */
.ossfieldrdr2 {}

/* Author, category, date */
.ossfieldrdr3:before { content: 'Written by: ';}
.ossfieldrdr4:before { content: ', in: '}
.ossfieldrdr5:before {content: ',  ';}
.ossfieldrdr3, .ossfieldrdr4, .ossfieldrdr5 { color:#ada393; font-style:italic; display:inline-block;}

/* For debugging purpose: Crawl date and ID article */
.ossfieldrdr6:before {content:'Last crawled time: ';}
.ossfieldrdr7:before {content:'ID: ';}
.ossfieldrdr6 { border-top:1px solid #CDCCC9; margin-top:20px;}
.ossfieldrdr6, .ossfieldrdr7 { font-size:0.9em; color:#CDCCC9;}

/* Facets */
.oss-facet h3 { color:#6f9d9f; font-family:Tahoma;}
.oss-facet a { color:#bd5532; }
.oss-facet ul { padding-left:10px;}

/* Autocomplete */
#ossautocompletelist{ background:white; padding:0px; border:1px solid rgb(228, 225, 220);}
.ossautocomplete_link , .ossautocomplete_link_over { cursor:pointer; display:block; padding:5px; }
.ossautocomplete_link_over { background:#efedea;}
```

That's it! Click "Save & close" and then click "View" in the list of renderers.

Autocompletion should work. 

![Using autocompletion](database_autocomplete_results.png)

Try searching for "Lorem". Facets are dynamically loaded and can be used to easily filter content on a particular field:

![Using facets](database_facets.png)


## Going further

### Scheduling the crawl

The crawl process can be launched automatically on a regular basis. To do so create a job of scheduler in tab "Scheduler" and choose task `Database crawler - run`. 

### Scheduling the autocompletion re-building

You could also want to automatically re-build the autocompletion sub-index after each crawl. To do so simply add a `Build autocompletion` task to your previously created job of scheduler.

### Using variables when crawling

Several variables can be used in the SQL Query. Values for these variable can be given at crawl time. See [How to use variables with the database crawler](../faq/crawling/how_to_use_variables_with_database_crawler.md) to discover this advanced feature.