In everyday use, the term `facet` can mean two things:
* a list of values for a specific field
* a filter that can be added to a query

## Configuring facets
Facets can be configured in two ways:

1. Through OpenSearchServer's interface, to build and configure a query. The "Facets" tab holds the tools you will need.
2. Using the OssSearch class from our library. For instance:

```php
require_once(dirname(__FILE__).'/oss_api.class.php');
require_once(dirname(__FILE__).'/oss_results.class.php');

...

$xmlResult = $oss_search->query($keywords)
                        ->lang('en')
                        ->template('search')
                        ->facet('category', 2, true) //here we ask a facet on field 'category'
                        ->rows(10)
                        ->execute(60);
```

This example query will return the documents matching the given `$keywords` and list the values for their `category` field. Here is an extract of the returned XML:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
...
  <faceting>
    <field name="category">
      <facet name="politics">14575</facet>
      <facet name="usa">2467</facet>
...
```

> Parameter `2` in `->facet('category', 2, true)` tells OpenSearchServer to not return values that are unique (i.e., have a count of 1). For instance if only one document has "europe" as its `category` then this value would not have been returned in this facet.
> You can use any number you want for this parameter. Most of the time this will be `1`, to avoid returning values with zero match in the current query.

## Displaying facets

The functions `getFacets` and `getFacet` will be used here to loop through available facets and build an associative array containing every field and values.

Since OssResults returns some SimpleXMLElement objects we need to access data in a specific way :

```php
$xmlResult = $oss_search->query($keywords)
                        ...
                        ->execute(60);

$oss_result = new OssResults($xmlResult);      
foreach($oss_result->getFacets() as $facetName) {
	$facetArray = (array)$facetName;
	foreach($oss_result->getFacet($facetName) as $facetDetails) {				
		$facetDetailsArray = (array)$facetDetails;
		$facets[$facetArray[0]][$facetDetailsArray['@attributes']['name']] = (string)$facetDetails;
	}
}
```

This code produces an array structured like this:

```
Array
(
    [<field name 1>] => Array
    (
       [<value 1>] => <value_1_total>
       [<value 2>] => <value_2_total>
       ... 
    )
    [<field name 2>] => Array
    (
      [<value 1>] => <value_1_total>
      [<value 2>] => <value_2_total>
      ... 
    )
)
```

Here is an example of data returned in such an array:
```
Array
(
    [category] => Array
    (
       [politics] => 14575
       [usa] => 2467
    )
)
```

You can then easily loop through this array to build a list of links to filter results:

```php

foreach($facets as $facetName => $facetValues) {
  print '<h3>'.$facetName.'</h3>';
  print '<ul>';
  foreach($facetValues as $value => $number) {  
    print '<li><a href="/search?q='.$keywords.'&f['.$facetName.']='.$value.'">'.$value.' <span>('.$number.')</span></a></li>';
  }
  print '</ul>';
}
```

When applied to the data in the previous example, this example code displays:

***

**category**
* [politics (14575)](search?f[category]=politics)
* [usa (2467)](search?f[category]=usa)

***

In this code example, the links were built using the parameter `f`, which indicates an array. This allow using multiple facets - or multiple values for a single facet. See below for more.

## Applying filters

Applying filters is straightforward: we need to get facetq valueq from the URL and add a `filter()` call to the `oss_search` object, just like we did in the [Using pagination to navigate through results](Using-pagination-to-navigate-through-results) page.

Here is an example:

```php
require_once(dirname(__FILE__).'/oss_api.class.php');
require_once(dirname(__FILE__).'/oss_results.class.php');

...

$facets = (isset($_GET['f'])) ? $_GET['f'] : array();          //retrieves facets from the URL
                                                               //   Since facets are written with an array notation in
                                                               //   the URL, PHP automatically returns an array of values

$oss_search->query($keywords)                                     //starts by configuring a basic search
           ->lang('en')
           ->template('search')
           ->facet('category', 1, true) 
           ->rows(10);

foreach($facets as $facetField => $facetValue) {               //loops through facets found in the URL
  $oss_search->filter($facetField . ':"' . $facetValue .'"');  //merely applies a filter on a specific field with a specific value.
                                                               //   this would for instance result in : category:"politics"
}

$oss_search->execute(60);                                      //now it can execute the query
```

## Going further

### Using several values for one field
It is often valuable to allow users to first filter for one specific value for one field, and then add other values to their search. 
For example, one could decide to restrict documents to the ones having "politics" as their category, and then add to this set those documents with "usa" as their category. 
The example solution discussed above does not allow for this - each link filters on one specific value.

So here is an example of how to do it:

```php

// $facets holds the list of values returned by the query
// $existingFilters holds filters fetched from URL parameter 'f'

foreach($facets as $facetName => $facetValues) {
  print '<h3>'.$facetName.'</h3>';
  print '<ul>';
  foreach($facetValues as $value => $number) {  
    print '<li><a href="/search?q='.$keywords.'
                               &f='.http_build_query(
                                            mergeFacets(
                                               $existingFilters, 
                                               $facetName, 
                                               $value)
                              .'">'.$value.' <span>('.$number.')</span></a></li>';
  }
  print '</ul>';
}

function mergeFacets($existingFilters, $facetName, $facetValue)
{
  if(                                                                      // if this facet value already exists, exits
      !empty($existingFilters[$facetName])                                 
      && 	
      (
        (                                       
          is_array($existingFilters[$facetName])                           // there could be several filters set on the field
          && in_array($facetValue, $existingFilters[$facetName])           // and the given value could already be among those
        )
        ||  $existingFilters[$facetName] == $facetValue                    // or there could be only one filter for this field
                                                                           // which could be the given one
      )
  ) {
    return $existingFilters;	
  }
                                                                            // the given value is not already among the filters, 
                                                                            // it needs to be added
  if(!empty($existingFilters[$facetName])) {
    if(is_array($existingFilters[$facetName])) {                            //    if there are already several values for this
      $existingFilters[$facetName][] = $facetValue;                         //    field then add the given value to the array
    }
    else {                                                                  //    if there is already one value for this field   
      $existingFilters[$facetName] =                                        //    transform it into an array and
              array($existingFilters[$facetName], $facetValue);             //    add the given value
    }
  }
  else {                                                                    // otherwise if there is no value for this field yet
    $existingFilters[$facetName] = $facetValue;                             // simply add it          
  }
  return $existingFilters;		
}
```

`http_build_query()` is great to transform arrays returned by `mergeFacets()` into additional URL parameters.

**Here is one example:**
* the page is accessed at the URL `/search?q=open&f[]=europe`
* the category is a **multi-valued** field and some documents in the index have several categories (for example _europe_, _politics_ and _usa_)
* the facets returned by the query will look like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
...
  <faceting>
    <field name="category">
      <facet name="politics">3</facet>
      <facet name="usa">7</facet>
      <facet name="europe">9</facet>
...
```

And the links built for the _politics_ and _usa_ facets will look like this:
* `search?q=open&f[category][0]=europe&f[category][1]=politics`
* `search?q=open&f[category][0]=europe&f[category][1]=usa`

As you remember from the previous _"Apply filter"_ section, you can retrieve the `f` parameter from the URL to directly get an array :

```
(
  [category] => Array
  (
    [0] => europe
    [1] => politics
  )
)
```

### Displaying values that do not match the current query

When looking at results, most users expect to see for a given facet the number of results they _**could**_ get if they added another filter to their search.
Here is an example : an index contains 5 documents with `category=politics` and 5 other documents with `category=usa`.
By filtering on `category=politics` only 5 documents would be displayed to the users. Assuming no minimum count parameter, facets returned for this search would be:

```xml

  <faceting>
    <field name="category">
      <facet name="politics">5</facet>
      <facet name="usa">0</facet>
```

since 0 documents match both categories.

The displayed results would look like this:

***

**category**
* politics (5)
* [usa (0)]( )

***

It would usually be preferable to display:

***

**category**
* politics (5)
* [usa (5)]( )

***

One suggested solution is to execute several OpenSearchServer queries.

1. The first query is executed using the given keywords but without any filters. In this example it would return `...<field name="category"> <facet name="politics">5</facet> <facet name="usa">5</facet>...`. This query is used to build what we'll call _hypothetical_ values for the facets.
2. The second query is executed using both the given keywords and the given filters. This displays the results of the query that the user actually made.

Thus, you can display the documents returned by the second query along with the facets returned by the first query - a behaviour that better matches user expectations.

**Take care**: as soon as you work with multiple facets (that is, apply filters to several fields) you will need further queries to compute the _hypothetical_ facets. This is because a single query without any filter will not provide the expected results. One query per field will be needed, using the active filters for the other fields but no filter for the current field.

Depending on the wanted boolean behaviour (OR or AND) between the different facets this could get quite involved ;)
