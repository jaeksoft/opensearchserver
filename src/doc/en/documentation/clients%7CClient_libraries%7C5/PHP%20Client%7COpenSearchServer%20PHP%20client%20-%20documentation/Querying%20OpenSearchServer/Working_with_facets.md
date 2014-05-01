Concept of `facet` has two sides:
* a facet is a list of values for a specific field
* and it is also a filter that can be added to a query

## Configure facets
Facets configuration can be made in two ways:

1. Using OpenSearchServer's interface to build and configure a query. The "Facets" tab is quite easy to understand and to use for this purpose.
2. Using OssSearch class from our library. For instance:

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

This query will return the documents matching the given `$keywords` and will also return list of values for the field `category` for those documents. Here is an extract of the returned XML:

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

> Parameter `2` in `->facet('category', 2, true)` tells OpenSearchServer to not return values that are unique. For instance if only one of the document would have had "europe" as its `category` then this value would not have been returned in this "facet".
> This can be configured with any wanted number. Most of the times this would be `1`, to avoid getting back values with no match in the current query.

## Display facets

Function `getFacets` and `getFacet` will be used here to loop through available facets and build an associative array containing every field and values.

Since OssResults return some SimpleXMLElement objects we need to access data in a particular way :

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

This code would result in an array like this:

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

Data from the above example would be:

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

It's easy then to loop through this array to build a list of links to filter results:

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

In previous example this code would display:

***

**category**
* [politics (14575)](search?f[category]=politics)
* [usa (2467)](search?f[category]=usa)

***

In this code section we chose to build links using a parameter `f` which is an array. This allow the use of multiple facets or the use of multiple values for the same facet. Please see further section for more information.

## Apply filter

Applying filter is kind of straight forward: we need to get facet value from the URL and add a `filter()` call to the `oss_search` object, just like we did in the [Using pagination to navigate through results](Using-pagination-to-navigate-through-results) page.

For example:

```php
require_once(dirname(__FILE__).'/oss_api.class.php');
require_once(dirname(__FILE__).'/oss_results.class.php');

...

$facets = (isset($_GET['f'])) ? $_GET['f'] : array();          //retrieve facets from URL
                                                               //   since facets are written with an array notation in
                                                               //   URL, PHP automatically returns an array of values

$oss_search->query($keywords)                                     //start by configuring basic search
           ->lang('en')
           ->template('search')
           ->facet('category', 1, true) 
           ->rows(10);

foreach($facets as $facetField => $facetValue) {               //loop through facets found in URL
  $oss_search->filter($facetField . ':"' . $facetValue .'"');  //merely apply a filter on a specific field with a specific value.
                                                               //   this would for instance result in : category:"politics"
}

$oss_search->execute(60);                                      //finally execute query
```

## Go further

### Use several values for one field
It is often very useful to allow users to first filter for one specific value for one field but then add several other values to their search. 
For example, one could decide to restrict documents to the ones having "politics" as their category, and then also add documents with "usa" as their category. 
With the previously shown solution this would not be possible since each link filters on one specific value.

However implementing this feature is not so complicated.
This could for example be done this way:

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
                                                                            // given value is not already among the filters, 
                                                                            // it needs to be added
  if(!empty($existingFilters[$facetName])) {
    if(is_array($existingFilters[$facetName])) {                            //    if there is already several values for this
      $existingFilters[$facetName][] = $facetValue;                         //    field then add the given value in the array
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

`http_build_query()` easily transform array returned by `mergeFacets()` into some URL parameters.

**Let's take on example:**
* page is accessed with URL `/search?q=open&f[]=europe`
* category is a **multi-valued** field and some documents in the index have several categories (for example _europe_, _politics_ and _usa_)
* facets returned in the query will for example be:

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

Finally, links built for _politics_ and _usa_ will be:
* `search?q=open&f[category][0]=europe&f[category][1]=politics`
* `search?q=open&f[category][0]=europe&f[category][1]=usa`

Remember previous _"Apply filter"_ section: retrieving parameter `f` from URL will directly give an array:

```
(
  [category] => Array
  (
    [0] => europe
    [1] => politics
  )
)
```

### Display values that don't match current query

Most of the users are accustomed to find in the list of values for one facet what _**could**_ be the results by adding another filter to their search.
Let's say for instance an index contains 5 documents with `category=politics` and 5 other documents with `category=usa`.
By filtering on `category=politics` only 5 documents would be displayed to the users. Facets (with no "minimum" parameter) returned for this search would be:

```xml

  <faceting>
    <field name="category">
      <facet name="politics">5</facet>
      <facet name="usa">0</facet>
```
since 0 documents have both categories.

Display would be something like this:

***

**category**
* politics (5)
* [usa (0)]( )

***

It would be better to be able to display:

***

**category**
* politics (5)
* [usa (5)]( )

***

To achieved this a solution is to execute several queries to OpenSearchServer.

1. First query would be executed with searched keywords but without any filters. In this example it would return `...<field name="category"> <facet name="politics">5</facet> <facet name="usa">5</facet>...`. This query would be used to build what we can call _hypothetical_ values for facets.
2. A second query would be executed with searched keywords and given filters. This query would be used to display actual results of the precise query user made.

By displaying documents returned by the second query but facets returned by the first one behaviour explained above would be achievable.

**Take care**: as soon as you will work with several facets (= filters on several fields) you will need to run several queries to compute the _hypothetical_ facets, because a single query with no filter at all would not give expected results. One query by field will be needed, with active filters for other fields but no filter for the current field.

Depending on the wanted boolean behaviour (OR or AND) between the different facets this could possibly become a bit more complicated ;)