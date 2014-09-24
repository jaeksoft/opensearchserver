## How to use analyzers

Analyzers are an essential part of OpenSearchServer. They are responsible for processing text data during indexing and also at query time. They can do a lot of work on text, like for example:

* Splitting sentence in words
* Extracting some particular text based on a pattern (by using regular expression)
* Filtering words
* Enabling use of synonyms
* Stemming words
* Converting units
* Transform text into numbers
* And lots of other process

### Structure of an analyzer

3 main properties define an analyzer:

1. Language of documents it must be applied to. This can be `Undefined`: analyzer will be applied to every document.
2. Tokenizer to use when indexing documents and when querying. Tokenizers are used to split input text into several `tokens`.
3. List of filters used to process input text.

Analyzers are then used in several locations:

* They can be configured in index's schema to be used for a particuler field. A field in the schema may define one analyzer only.
* They can also be used in some other locations, like in some parsers, when fields mapping are created.

### What are `tokens`

`Tokens` are the pieces of information indexed in fields or used when querying the index. It is important to understand that when searching the index OpenSearchServer actually compares `tokens`: tokens given in the query with tokens indexed in fields.

For instance: a document titled "Red cars" will have values `red` and `car` in its field `title` because this field uses a `TextAnalyzer`. When querying for example "red car" this document will match, since tokens `red` and `car` will be found in this document. Search "red cars" will also match this document because at query time the same process is applied to input text, resulting in tokens `red` and `car`.

### Role of an analyzer

Analyzers' goal is to process an input text to transform it in `tokens`. These tokens will be the information that will be indexed in the corresponding field (when used at indexing time) or information that will be used in search query (when used at query time).

#### At indexing time

When documents are indexed the fields of the schema receive values, according to different `mappings` made at different locations (for example in the `Field mapping` tab of the web crawler and the `Field mapping` tab of the HTML parser). If the corresponding fields are configured to index data (property `Indexed` set to `yes` on the field) then two cases may occur:

1. Field is not configured to use any analyzer: full text is directly indexed as one `token`, as it was given, without any transformation.
2. Field is configured to use an analyzer: text is given to this analyzer and `tokens` resulting of the processing by the analyzer are indexed.

Here is an example: for a field `title` using a `TextAnalyzer`, if a document named "Planet Of The Apes" is indexed, tokens will be:

1. In case 1: `Planet Of The Apes`.
2. In case 2: `planet`, `of`, `the`, `ape`.

#### At query time

Same behaviour is used when querying the index. Search keywords are given to analyzer configured on each field of the query, and resulting tokens are actually used to make the final query to the index.

For example, following above example and querying on field `title`, query `"Planet of the apes"` would actually result in theses queries

1. In case 1: `title:"Planet of the apes"`
1. In case 2: `title:"planet of the ape"`

### List of available Tokenizers

Several tokenizers are available in OpenSearchServer. They differ in the way to split sentences in tokens: some of them will split on white spaces, others on some special characters, etc.

Below examples are made with sentence "A two-hours walk to London.".

* **StandardTokenizer**: splits sentences on whitespace and some other characters (period, semi column) but keeps some others (like single quote).
    * Example:
	![StandardTokenizer](analyzer_standard.png)
* **LetterOrDigitTokenizerFactory**: splits sentences on every character not being a letter or a digit. This includes white space, quote, period, etc. This tokenizer provides a field "Additional characters" that can be used to white-lists some other characters.
    * Example where "Additional characters" contains `'`:
	![LetterOrDigitTokenizer](analyzer_letterordigit.png)
* **KeywordTokenizer**: does not split sentence at all.
    * Example:
	![KeywordTokenizer](analyzer_keyword.png)
* **WhitespaceTokenizer**: splits sentences on white spaces only.
    * Example:
	![WhiteSpaceTokenizer](analyzer_whitespace.png)
* **NGramTokenizer**: splits sentences in group of characters, called "ngram". Fields `Min gram size` and `Max gram size` are used to configure minimum and maximum number of characters in theses groups.
    * Example with `Min gram size` = 2 and `Max gram size` = 3. First tokens only are shown here:
	![NGramTokenizer](analyzer_ngramtokenizer.png)
* **EdgeNGramTokenizer**: splits sentences in group of characters starting from one side (front or back) of the sentence only. Fields `Min gram size` and `Max gram size` are used to configure minimum and maximum number of characters in theses groups.
    * Example with `Min gram size` = 1, `Max gram size` = 10 and `Edge side` = front:
	![EdgeNGramTokenizer](analyzer_edgengramtokenizer.png)

### List of built-in analyzers

When an index is created in OpenSearchServer it comes with several analyzers.

### StandardAnalyzer

* Lang: `Undefined`
* Index tokenizer: `LetterOrDigitTokenizerFactory`