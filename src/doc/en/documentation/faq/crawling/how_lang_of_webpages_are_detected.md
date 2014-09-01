## How lang of webpages are detected

The OpenSearchServer web crawler uses several methods to determine the `lang` (language) of a document. The first three attempts are done using a HTML parser. In order, these are :

1. Read the `lang` attribute in the `<html` tag
2. Read the `<meta http-equiv="content-language"` tag
3. Read the `<meta name="DC.Language"` tag

If all these fail, a content analysis will take place to deduce the language. This is called `ngram detection` by WebCrawler.

Detected lang and used lang methods can be observed when performing an OpenSearchServer Manual Crawl, or when using the URL Browser.
