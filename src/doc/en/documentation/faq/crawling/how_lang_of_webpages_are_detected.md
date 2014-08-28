## How lang of webpages are detected

OpenSearcServer's web crawler is able to detect lang of webpages in several hierarchised ways. In association with HTML parser, Web Crawler will use one of these process:

1. Read `lang` attribute in tag `<html`
2. Read tag `<meta http-equiv="content-language"`
3. Read tag `<meta name="DC.Language"`
4. Try to infer language from analysis of content (this process is named `ngram detection` by WebCrawler)

Detected lang and used lang method can be seen when using feature Manual Crawl or when browsing URL Browser.
