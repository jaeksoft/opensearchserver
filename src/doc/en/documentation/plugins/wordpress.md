## OpenSearchServer plugin for Wordpress

The OpenSearchServer plugin for Wordpress can be downloaded at Wordpress.org: [https://wordpress.org/plugins/opensearchserver-search/](https://wordpress.org/plugins/opensearchserver-search/)

# Actions and filters

## At indexing time

* Action **oss_create_schema**:
    * Description: called when creating schema of the index.
    * Parameters: `$schema`, `$schema_xml`
    * Example: can be used to add other fields to the schema of the index:
    
    ```php
    add_action('oss_create_schema', 'oss_create_schema', 10, 2 );
    function oss_create_schema($schema, $schema_xml) {
        //add a field to store vendor's name
        opensearchserver_setField($schema,$schema_xml,'vendor_name','StandardAnalyzer','yes','yes','no','no','no');
    }   
    ```
    
* Filter **oss_autocomplete_value**
    * Description: called when indexing a document and giving value to its `autocomplete` field.
    * Parameters: 
        * `$value`: original value that would be put in autocomplete field and then used for autocompletion,
        * `$post`: content being indexed.
    * Example: can be used to add more text in the autocomplete field, allowing for more suggestion in the autocompletion feature:
    
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
    
* Action **oss_index_document**
    * Description: called at the end of the indexing of a document.
    * Parameters: `$document`, `$index`, `$lang`, `$post`, `$customFields`
    * Example: can be used to add values to some custom fields:
    
    ```php
    add_action('oss_index_document', 'oss_index_document', 10, 5 );
    function oss_index_document($document, $index, $lang, $post, $customFields) {
        $sold_by = getSoldBy($post);
        $document->newField('vendor_name', $sold_by);
    }   
    ```

## At query time

* Filter **oss_search** 
    * Description: called before submitting query to OpenSearchServer.
    * Parameter: `$oss_query`: query built with configuration made in admin page.
    * Example: can be used to customize the query, for instance forcing filtering on a particular value.
* Filter **oss_search_getsearchfacet_without_each_facet**
    * Description: called when queries to get facets are built.
    * Parameter: `$oss_query`
    * Example: can be used to customize the query, for instance forcing filtering on a particular value.
* Filter **oss_facets_slugs**
    * Description: called when writing slugs to use for facets' links.
    * Parameters: `$facetsSlugs`: slugs built from facets' names or from specific slugs values configured in admin page.
    * Example: can be used to override slugs, for instance taking them from a particular XML file.

