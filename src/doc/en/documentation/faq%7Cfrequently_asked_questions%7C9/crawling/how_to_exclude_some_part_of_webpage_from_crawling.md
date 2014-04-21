OpenSearchServer allows you to exclude certain parts of a webpage from being crawled.

### Excluding content using opensearchserver.ignore CSS class

The content of any HTML tag with the class `opensearchserver.ignore` will be ignored while crawling.

The `opensearchserver.ignore` class can also be added with your existing CSS classes.

Example :

    <div class="opensearchserver.ignore">This content will not be indexed in OpenSearchServer.</div>
    <div class="content opensearchserver.ignore">This content will not be indexed in OpenSearchServer.</div>
