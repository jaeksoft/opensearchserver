## Searching using the OpenSearchServer module for Drupal 7

### Disabling core Search module

The OpenSearchServer module for Drupal 7 does not replace the Core Search module. First thing to do is then to disable the Core Search Module

Go to page _/admin/modules_ and uncheck "Search" then click on "Save configuration".

![Disable core Search module](drupal_disablecoresearch.png)

### Adding some test content

Since a fresh install is used here, we will create some test "Articles" and "Basic pages":

![Add an article](drupal_createarticle.png)

All content:

![Content](drupal_content.png)

### Creating a search page using Views

Check that you enabled **Search views** in _/admin/modules_:

![Checking Search views](drupal_checkviews.png)

And enable module **Views UI**:

![Enabling Views UI](drupal_viewsui.png)


#### Adding a View

Go to page _/admin/structure/views/add_ and fill in the form:

* View name: **Search**.
* Show: choose **OpenSearchServer Index** in the list (name of the index created before), sort by **Unsorted**.
* Create page : **checked**
  * Page title: **Search**
  * Path : **search** 
  * Display format: **Unformatted list** of **Rendered entity**
  * Items to display: **10**
  * Use a pager: **checked**

![Adding a view](drupal_createview.png)

Click on "Continue & edit".

In the new form:

* Filter Criteria: click "Add" and check "Search: Fulltext search". Click "Apply (all displays)"
  * Expose this filter to visitors, to allow them to change it: **checked**
  * Label: **Search**
  * Value / Remember the last selection: **checked**
    * User roles: **anonymous user**, **authenticated user**
  * Use as: **Search keys**

![Configuring filter](drupal_configurefilter.png) 

* Sort Criteria: click "Add" and check "Search: Relevance".
* Page settings / Access: click "None" and choose "Permission" / "View published content".
* Advanced / Exposed form:
  * Exposed form in block: click "No" and choose "Yes"
  * Exposed form style: click "settings":
    * "Submit button text": write "Search"
    * Expose sort order: **unchecked**

Full configuration:

![Full view](drupal_fullview.png) 

#### Configuring search block

Go to page _/admin/structure/block_ and move the new block "Exposed from: search-page" to one region, for instance "Sidebar first".

![Configuring block](drupal_block.png) 

Click "Save blocks".

#### Testing the page

Go to page _/search_. In the search block write some keywords, for instance "i'm a test page" and press enter or click "Search".

![Search results](drupal_searchresults.png)

#### Editing the displayed fields

Displayed fields for each result can be changed. Go to the edit page for the View, _/admin/structure/views/view/search_.

In Format / Show click on "Rendered entity" and choose "Fields" instead. Click "Apply (all displays)" two times to close the window.

In Fields, click on "Add". 

![Adding a field](drupal_viewaddfield.png)

Then choose fields to display and configure them (add / remove labels, choose format, etc.).

For example (see below for how to add facets):

![Configuring a field](drupal_configurefield.png)

All the chosen fields:

![Fields](drupal_viewfields.png)

Click on "Save" and run a new search with this View:

![Search results with fields](drupal_searchresultsfields.png)

##### Highlighting found terms

To highlight found terms go to the Search API administration page, choose your index and click on tab "Filters" (_/admin/config/search/search_api/index/opensearchserver_index/workflow_).

Check Processors / **Highlighting**.

Click on "Save configuration", go to the search page and run a new search:

![Search results with highlighting](drupal_searchresultsfields_snippet.png)

##### Adding an excerpt

When processor "Higlighting" is enabled: go to the edit page for the View and add field "Search: Excerpt".

![Fields](drupal_viewfields_excerpt.png)

Save the view and run a new search: 

![Search results with highlighting](drupal_searchresultsfields_snippet_excerpt.png)
  
### Configuring facets

If you followed step "Enabling facets" from the [Configuring](configuring.md) page go to page _/admin/structure/block_ and move blocks "Facet API: Search service: OpenSearchServer Index : Author" and "Facet API: Search service: OpenSearchServer Index : Content type" to one region, for instance "Sidebar first". 

![Adding facets](drupal_blockfacets.png)

Click "Save blocks".

Reload the _/search_ page and run a query:

![Search results with facets](drupal_searchresultsfacets.png)