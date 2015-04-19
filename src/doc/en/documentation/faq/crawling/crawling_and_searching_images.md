## Crawling and searching images

This tutorial will explain how to set up crawling of images and how to build a search engine for images. It will be possible to search in image's name of `alt` text.

Here is one possible result: 
 
![Image search](oss_search_image_result.png)

Here are the main concepts that will be discussed in this tutorial:

* Images to index will be fetched from several web pages
* Two indexes will be necessary:
  * Index 1: to index web pages and "meta data" (width, height, area) of images
  * Index 2: to index `alt` texts for images and run full-text search on images
* Index 2 will be built from the `<img.../>` tags found when indexing pages in index 1

This tutoriel does not explain how to configure web crawling or web pages indexation. 
Prerequisite is to have an index crawling several web pages and knowing how to work  with parsers, analyzers, queries, etc.

# Index 1: full-text search on pages, images meta-data storing

This index is already configured to crawl, index and search web pages. To index images here are the necessary steps:

*	Create a new `imagesTags` fields that will be used to store for each page every `<img .../>` tags found 
  * Indexed: `yes`, stored: `no`, termvector: `no`
* Configure a new mapping in HTML parser to extract the `<img.../>` tags from the page and store them in the `imageTags` field.
* Configure a mapping in the HTML parser to extract tags `<img>` from the page and store them in the field `imageTags`:
  *	Source: `htmlSource`
  *	Linked in: `imageTags`
  *	Reg. exp.: `(?s)(<img[^>]* alt="[^"]+"[^>]*>)`

![Mapping](oss_search_image_map_source.png)

Now every tags `<img .../>` found in the pages will be stored in the field `imageTags` of the documents. Tags will be stored with this format: `<img src="{url image}" alt="{texte descriptif}" />`

HTML parser will also be able to detect URL from the `src` attributes (because they are URL, like URLs found in `href` attribute of the `a` tag). Thus the web crawler will be able to crawl the image directly. We will need to configure a parser to index some information about those images (size).

Index 1 will thus index two kinds of documents:

* Web pages, from which tags `<img ...>` will be extracted and stored in a particular field
* Images, that will be stored with their dimensions but without their `alt` text, because when the image is crawled the `alt` text is not available (it is only displayed in the page where the image is displayed).
  * It’s in the index 2 that we will be able to associate each image to its `alt` text, and then able to run full-text search on images

To index those images we need to:

* Add some fields in the index:
  *	**fileName**: 
     * Will index the name of the image
     * indexed: `yes`
     * stored: `no`
     * term vector: `no`
  * **imageFormat**: 
     * Will index type of the image (png, jpg, ...)
     * indexed: `yes`
     * stored: `no`
     * term vector: `no`
  * **imageWidth**: 
     * indexed: `yes`
     * stored: `yes`
     * term vector: `no`
     * Analyzer: IntegerAnalyzer
  * **imageHeight**: 
     * indexed: `yes`
     * stored: `yes`
     * term vector: `no`
     * Analyzer: IntegerAnalyzer
  * **imageArea**: 
     * Could be used to search image by "size" (large, small, ...)
     * indexed: `yes`
     * stored: `no`
     * term vector: `no`
     * Analyzer: IntegerAnalyzer
* Configure analyzer IntegerAnalyzer (tab Schema / Analyzer) as shown:

![Analyzer](oss_search_image_analyzer.png)


* Configure image parser as shown: 

![Image parser](oss_search_image_image_parser.png)

![Image parser](oss_search_image_image_parser2.png)

![Image parser](oss_search_image_image_parser3.png)

The "Field mapping" will give precise values to each field related to the image:

![Image parser](oss_search_image_image_parser4.png)

# Index 2: full-text search on images

Index 2 will use tags `<img .../>` stored in index 1 to create one document by image and associate it to its `alt` text.
We’ll see in this document how full information about an image (alt, url, area, format) coming from both index can be used in one query.


Create a new index with these fields:

* **imageTag**:
    * Will store full html tag of the image
    * indexed: `no`
    * stored: `yes`
    * term vector: `no`
* **url**:
    * Will store URL of the page where image has been found
    * indexed: `yes`
    * stored: `yes`
    * term vector: `no`
* **alt**:
    * indexed: `yes`
    * stored: `yes`
    * term vector: position_offsets
    * analyzer: TextAnalyzer
* **src**:
    * indexed: `no`
    * stored: `yes`
    * term vector: `no`
* **imgUrl**:
    * Will store full URL to the image
    * indexed: `yes`
    * stored: `yes`
    * term vector: `no`

![List of fields](oss_search_image_index_schema.png)

Then come the part where we use the data from the first index (tags `<img .../`>) to create documents in the second index. This is done with a "job" of the Scheduler.

But first some Analyzers need to be created to:

* extract the `src`
* extrat the `alt`
* build full absolute URL to the image

In tab Schema /Analyzer, create 3 new Analyzers:

* **ImageAltAnalyzer**:
  * Tokenizer: KeywordTokenizer
  * Filters:
    * RegularExpressionFilter / Query and Indexation / `alt="([^"]*)"`
    * DecodeHTMLEntitiesFilter / Query and indexation

This analyzer uses a Regular Expression to get content from the `alt` attribute and then decode HTML entities that it may contain:

![Image Alt Analyzer](oss_search_image_imagealtanalyzer.png)

*	ImageSrcAnalyzer: 
  * Tokenizer: KeywordTokenizer
  * Filters:
     * RegularExpressionFilter / Query and Indexation / `(?s)src="([^"]*)"`

This analyzer uses a Regular Expression to get the content from attribute `src`:
 
![Image Src Analyzer](oss_search_image_imagesrcanalyzer.png)

* URLNormalizerAnalyzer 
  * Tokenizer: KeywordTokenizer
  * Filters:
     * URLNormalizeFilter / Query and Indexation

This analyzer builds a full URL for the image from the input data. We will see in a few moments that it takes as input the URL of the page where the image has been found and the value of the `src` attribute from the `img` tag. One `filter` is used to check whether `src` is relative  or absolute et sends back an absolute URL.

![Image URL Analyzer](oss_search_image_urlnormalizeranalyzer.png)

It is now time to configure the retrieval of data from the first index using these analyzers.

In tab "Scheduler", create a new job.

* Name: **Get images**
  * Active: `Enabled`
  * Cron expression: run every 8 hours: `0 50 */8 * * ? *`
  * Tasks:
    * Add task "Delete all (truncate)"
    * Add task "Pull fields"
      * This task can get data from another index. We are going to configure it to get every `<img .../>` tags stored in the first index for each crawled page, and to process those tags with the analyzers we just created in order to create one document by image.
      * Index source: write name of first index
      * Login: a login that can read index 1
      * API Key: API key for the login
      * Source query: use `*:*` to get back all documents
      * Language: Undefined
      * Source field name: `imageTags`
      * Target field name: `imageTag`: this is used to copy every `<img .../>` tags from the field imageTags of the index 1 to the field `imageTags` in the index 2. `imageTags` is multivalued: **task "Pull fields" will create one document for each value from the field `imageTags`.**
      * Mapped fields on source: `url,url`: this will copy field `url` from index 1 to field `url` in index 2
      * Mapped fields on target:
         * `imageTag,alt,ImageAltAnalyzer`
         * `imageTag,src,ImageSrcAnalyzer`
         * `url|src,imgUrl,URLNormalizerAnalyzer`
         * here we ask for some specific process on the fields of index 2:
             * `imageTag` is copied into field `alt` after being processed by ImageAltAnalyzer
             * `imageTag` is copied into field `src` after being processed by ImageSrcAnalyzer
             * `url` is concatenated with src with a pipe and is copied to field `imgUrl` after being processed by URLNormalizerAnalyzer

![Pull fields](oss_search_image_pullfields.png)

# Building search queries

## Full-text search for images

As seen before the full-text search for images is done on values `alt`. Those values are stored in index 2: thus it is this index that must be used for querying.

However, index 1 stores some useful information about the images (name, area, size). We will configure a join between the two index for the search query to also send back these useful information.

### Index 2

4 queries must be created in index 2. First one will search for every images and the 3 others will be used to filter on image size.
 
![Queries in index 2](oss_search_image_queries1.png)

**Query "search":**

Returned fields:

![Returned fields](oss_search_image_returnedfields.png)
 
Here a "collapsing" must be configured in order to remove duplicates of images. When images where indexed, task "Pull fields" has maybe get back the same image several times, with some different `alt` for example.
Thus a "collapsing" on field `imgUrl` is done here:

![Collapsing](oss_search_image_collapsing.png)

We also configure a join on index 1 to get back more information about the image:

![Join](oss_search_image_join.png)

### Index 1

On index 1 4 queries must be created too, they will be used in the joins made in the queries of the index 2.

![Queries in index 1](oss_search_image_queries2.png)
 
**Query "imageSearch":**

Returned fields:

![Returned fields](oss_search_image_returnedfields2.png)

Here a filter is used to remove the very small images:

![Filters](oss_search_image_filters.png)

## Search images by size

### Index 2

Query "searchLarge" is the same than query "search" except for the join: here it is the query "imageSearchLarge" that is used for joining index1.

![Join](oss_search_image_join2.png)

### Index 1

Query "imageSearchLarge" is the same than query "imageSearch" with one more filter on field "imageArea": 

![Filters](oss_search_image_filters2.png)

Filters for query imageSearchSmall and imageSearchMedium will be built in the same way with some particular values:

_**Medium:**_

![Filters medium](oss_search_image_filtersmedium.png)
 
_**Small:**_

![Filters small](oss_search_image_filterssmall.png)
