## How to extract specific information from web pages

When crawling web pages with default index template `web crawler` lots of information are automatically extracted from pages: title, content, url, meta keywords and description, and so on.

**It can sometimes be useful to be able to extract more specific information** and save them in some specific fields. 

### Extracting information using HTML parser

For instance one can want to extract a price and a product name from every page when crawling his e-commerce website.
Let's take this example. Here is a product page from an imaginary website:

![Imaginary product page](extract_ecommercepage.png)

This page contains several information:
* product name: `Great computer`
* product price: `$400.00`

Two fields must be created in index's schema in order to hold these new information:

![Two new fields](extract_twofields.png)

Next step is to edit HTML parser in tab `Schema` / `Parser list`

![Edit HTML parser](extract_editparser.png)

Tab `Field mapping` defines lots of mapping between some information provided by built-in HTML parser and some fields of the schema. 

Each time the web crawler crawls an HTML web page it gives it to the HTML parser in order for it to parse page. During this parsing process the HTML parser extract some particular information from the page and make them usable in **fields mappings**. Available information given by HTML parser are : title, generated title, body, meta_keywords, meta_description, meta_robots, internal_link, internal_link_nofollow, external_link, external_link_nofollow, lang, htmlProvider and htmlSource. `htmlSource` is the full HTML source code of the web page.

Extracting specific information from a web page will merely consist in **adding a mapping** between **information `htmlSource`** and a particular field of the schema, **using a [regular expression](http://www.regular-expressions.info/) to restrict extracted data**.

HTML source code of the imaginary product page is:

```html
<html>
<head>
<title>My e-commerce page</title>
<style type="text/css">
	[...]
</style>
</head>
<body>
	<header>
		<h1>Great e-commerce website</h1>
	</header>
	<div id="product">
		<h2>Great computer</h2>
		<div id="product-description">
			<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent tristique ornare purus, ut pulvinar justo dictum ac. Etiam neque mi, venenatis ac tellus sit amet, luctus tempor odio.</p>
			<p>Sed ultricies sapien vitae augue congue, sed ultricies metus sodales. In quis elementum magna. Sed facilisis pharetra mi, non accumsan nisi efficitur sollicitudin. Vivamus tempus nisl eget dui congue porta. </p>
		</div>
		<div id="product-prices">
			<div id="old-price">$455.00</div>
			<div id="price">$400.00</div>
		</div>
	</div>
</body>
</html>
```

The following field mapping must be added in order to extract product name information:

![First mapping](extract_mappingproductname.png)

Regular expression is: `(?s)id="product">[^<]*<h2>(.*?)</h2>`.

In order to extract product price this mapping will be needed:

![Second mapping](extract_mappingproductprice.png)

Regular expression is: `(?s)id="price">\$(.*?)<\/div>`.	

Two new mappings are now added:

![Two mappings](extract_mappings.png)

Click button `Save` at the bottom of the page to save HTML parser.

This new configuration can be tested quickly by going to tab `Crawler` / `Web` / `Manual crawl`. Enter URL of test page and click button `Crawl`. Product name and price are extracted from the page:

![Extract](extract_test.png)

### Extracting information using crawler and Analyzers

In some particular case one may want to extract data from URL. URL is not an information provided by HTML parser, since it is the crawler which is in charge of handling URLs. Let's assume that URL to the previous imaginary page is `http://great-ecommerce-website.com/products/great-computer-4536GE7.html`. In this URL, `4536GE7` is the product reference. To extract it and index it in a particular fields the following steps are needed.

Create a new field in index's schema:

![Product reference](extract_productreference.png)

In tab `Crawler` / `Web`  / `Field mapping` add a mapping between information `url` and this new field:

![Mapping url](extract_urlmapping.png)

This tab has the same role than tab `Field mapping` in HTML parser, but here available information are provided by the web crawler and thus are different: url, crawl date, headers, and so on.

Field `product_reference` would now be filled by URL of pages. This is not yet what is wanted here. We now need to extract reference from URL in order to keep only this information in this new field.

Go to tab `Schema` / `Analyzers` and create a new Analyzer as shown:

![New analyzer](extract_analyzer.png)

This analyzer will **use a regular expression** (`http://great-ecommerce-website.com/.*-(.*?).html$`) to extract only reference part from URL. **It can be tested immediately** in "Analyzer test" section:

![Analyzer testing](extract_analyzertest.png)

Don't forget to click `Create` button. Go to tab `Schema` / `Fields`, click on previously create field `product_reference` and **configure it for using this new **:

![Configure product_reference](extract_productreference_edit.png)

Re-crawl the example page, for example using `Manual crawl` again. **Take care**, result of analyzers are not shown in data displayed by Manual Crawl feature. Manual crawl only shows results from field mappings made by crawler or parsers, but it does not show transformations applied by further indexation process. You must query the index to see the proper indexed value:

![Results](extract_queryresults.png)
