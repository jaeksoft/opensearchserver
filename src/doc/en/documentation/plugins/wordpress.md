## OpenSearchServer plugin for WordPress

The OpenSearchServer plugin for WordPress can be downloaded at Wordpress.org: [https://wordpress.org/plugins/opensearchserver-search/](https://wordpress.org/plugins/opensearchserver-search/)

# Actions and filters

## At indexing time

### Action **oss_create_schema**

* Description: called when creating the schema of the index.
* Parameters: `$schema`, `$schema_xml`
* Example: can be used to add new fields to the schema:
    
```php
    add_action('oss_create_schema', 'oss_create_schema', 10, 2 );
    function oss_create_schema($schema, $schema_xml) {
        //add a field to store the vendor's name
        opensearchserver_setField($schema,$schema_xml,'vendor_name','StandardAnalyzer','yes','yes','no','no','no');
    }   
```

### Filter **oss_autocomplete_value**

* Description: called when indexing a document and assigning a value to its `autocomplete` field.
* Parameters: 
    * `$value`: the original value that would be inserted in the autocomplete field and then used for autocompletion.
    * `$post`: the content being indexed.
* Example: can be used to add text to the autocomplete field, allowing for more suggestions from the autocompletion feature:
    
```php
    add_filter('oss_autocomplete_value', 'oss_autocomplete_value', 1, 2);
    function oss_autocomplete_value($value, $post) {
        $autocompleteValues = array($value);  
        //get name of vendor
        $autocompleteValues[] = getSoldBy($post);
        //get attribute "Fournisseur"
        $terms = get_the_terms( $post->ID, 'pa_fournisseur');
        if ( $terms && ! is_wp_error( $terms ) ) {
            foreach ( $terms as $term ) {
                $autocompleteValues[] = $term->name;
            }
        }
        return $autocompleteValues;
    }
```

### Action **oss_index_document**

* Description: called when completing the indexing of a document.
* Parameters: `$document`, `$index`, `$lang`, `$post`, `$customFields`
* Example: can be used to add values to custom fields:
    
```php
    add_action('oss_index_document', 'oss_index_document', 10, 5 );
    function oss_index_document($document, $index, $lang, $post, $customFields) {
        $sold_by = getSoldBy($post);
        $document->newField('vendor_name', $sold_by);
    }   
```

## At querying time

### Filter **oss_search** 

* Description: called before submitting a query to OpenSearchServer.
* Parameter: `$oss_query`: a query built using the configuration made on the admin page.
* Example: can be used to customize the query -- for instance by forcing filtering on a particular value.

### Filter **oss_search_getsearchfacet_without_each_facet**

* Description: called when building queries to get facets.
* Parameter: `$oss_query`
* Example: can be used to customize the query --for example by forcing filtering on a particular value.

### Filter **oss_facets_slugs**

* Description: called when writing slugs used as links for the facets.
* Parameters: `$facetsSlugs`: slugs built either using the facets' names, or using specific slugs values configured on the admin page.
* Example: can be used to override slugs, for instance by drawing them from a particular XML file.

# Translations

If you created translation files in your own language, please send them to us. We will gladly add them to the plugin and list your name on this page.

_Serbian translation provided by Ogi Djuraskovic [http://firstsiteguide.com](http://firstsiteguide.com)_.

