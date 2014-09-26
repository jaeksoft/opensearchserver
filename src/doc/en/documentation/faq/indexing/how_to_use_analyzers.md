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

Here is an example: for a field `title`, if a document named "Planet Of The Apes" is indexed, tokens will be:

1. In case 1 (no analyzer): `Planet Of The Apes`.
2. In case 2 (using a `TextAnalyzer`): `planet`, `of`, `the`, `ape`.

#### At query time

Same behaviour is used when querying the index. Search keywords are given to analyzer configured on each field of the query, and resulting tokens are actually used to make the final query to the index.

For example, following above example and querying on field `title`, query `"Planet of the apes"` would actually result in theses queries

1. In case 1: `title:"Planet of the apes"`
1. In case 2: `title:"planet of the ape"`

---

### List of available Tokenizers

Several tokenizers are available in OpenSearchServer. They differ in the way to split sentences in tokens: some of them will split on white spaces, others on some special characters, etc.

Below examples are made with sentence "A two-hours walk to London.".

#### StandardTokenizer

Splits sentences on whitespace and some other characters (period, semi column) but keeps some others (like single quote).

_Example:_
    
![StandardTokenizer](analyzer_standard.png)

#### LetterOrDigitTokenizerFactory

Splits sentences on every character not being a letter or a digit. This includes white space, quote, period, etc. This tokenizer provides a field "Additional characters" that can be used to white-lists some other characters.

_Example where "Additional characters" contains `-`:_
	
![LetterOrDigitTokenizer](analyzer_letterordigit.png)
#### KeywordTokenizer

Does not split sentence at all.

_Example:_

![KeywordTokenizer](analyzer_keyword.png)
#### WhitespaceTokenizer

Splits sentences on white spaces only.

_Example:_
	
![WhiteSpaceTokenizer](analyzer_whitespace.png)

#### NGramTokenizer

Splits sentences in group of characters, called "ngram". Fields `Min gram size` and `Max gram size` are used to configure minimum and maximum number of characters in theses groups.

_Example with `Min gram size` = 2 and `Max gram size` = 3. First tokens only are shown here:_
	
![NGramTokenizer](analyzer_ngramtokenizer.png)

#### EdgeNGramTokenizer

Splits sentences in group of characters starting from one side (front or back) of the sentence only. Fields `Min gram size` and `Max gram size` are used to configure minimum and maximum number of characters in theses groups.
 
_Example with `Min gram size` = 1, `Max gram size` = 10 and `Edge side` = front:_
	
![EdgeNGramTokenizer](analyzer_edgengramtokenizer.png)

---

### List of some filters

OpenSearchServer comes with lots of really useful filters. Some of them are detailed below.

#### DecodeHtmlEntitiesFilter

Decodes characters encoded as HTML entities. 

_Example:_

![DecodeHtmlEntitiesFilter](analyzer_decodehtmlentitiesfilter.png)

#### DomainFilter

Extracts domain parts from an URL. Can extract tld only (`com` for example in `http://editions.cnn.com`), hostname only (`cnn`), hostname and tld (`cnn.com`), or tld, hostname, and hostname and tld (`com`, `cnn.com`, `editions.cnn.com`).

Example with option `domain.tld only`:

![DomaiNFilter](analyzer_domainfilter.png)

#### ElisionFilter

Removes elisions. List of elisions can be configured.

_Example with phrase `l'autre m'appelle l'idiot, c'est grossier`:_

![ElisionFilter](analyzer_elisionfilter.png)

#### EdgeNgramFilter

Creates ngram starting from one side of tokens. Parameters are minimum and maximum size of ngram and side to use. 

_Example with min size = 1, max size = 20 and side = Front:_ 

![EdgeNgramFilter](analyzer_edgengramfilter.png)

This filter may be used to build suggestions for auto-completion.

#### ExpressionLookupFilter

Keeps tokens that are found in a list. Lists can be created in tab `Schema` / `Stop words`. 

_Example with default list of english stop words:_

![ExpressionLookupFilter](analyzer_expressionlookupfilter.png)

This filter may be used for extracting some particular keywords from a text.


#### IsoLatin1AccentFilter

Removes accents. 

_Example:_

![IsoLatin1AccentFilter](analyzer_isolatin1accentfilter.png)


#### LowerCaseFilter

Puts text in lower case.

_Example:_

![LowerCaseFilter](analyzer_lowercasefilter.png)

#### NGramFilter

Creates ngram.

_Example with `Min gram size` = 2 and `Max gram size` = 4 (first ngrams only are shown):_

![NGramFilter](analyzer_ngramfilter.png)

#### NumberFormatFilter

Transforms a number in a format understanble by OpenSearchServer. In OpenSearchServer every data is text data, there is no other type of data. When indexed, numbers must be prefixed by some `0` in order to be used in sorting and scoring. For example: number `110` is greater than number `20` but aplhabetically `"20"` will be sorted after `"110"`, because `2` is placed after `1`. However, if numbers are prefixed by `0` we will get for example `00110` (two 0) and `00020` (three 0). Here correct sorting will apply: `"00020"` will come before `"00110"`.

To differentiate between positive and negative number another character must be added in front of the number. For instance `<` and `>` can be used (respectively for negative and positive numbers), since alphabetically `<` comes before `>`.

A full configuration for NumberFormatFilter will be:

![NumberFormatFilter configuration](analyzer_numberformatfilter_configuration.png)

Giving these results:

![NumberFormatFilter](analyzer_numberformatfilter.png)
![NumberFormatFilter](analyzer_numberformatfilter2.png)


#### RegularExpressionFilter

Applies a [regular expression](regular-expressions.info) and keeps [captured groups](http://www.regular-expressions.info/brackets.html).

_Example with regexp `product-([0-9]*).php`:_

![RegularExpressionFilter](analyzer_regularexpressionfilter.png)

#### RegularExpressionReplaceFilter

Applies a [regular expression](regular-expressions.info) and allow re-use of [captured groups _via_ backreferencing](http://www.regular-expressions.info/refcapture.html).

_Example with regexp `^(.*?)thumbnail\/10x60(.*?)$` and replacement `$1images$2`:_

![RegularExpressionReplaceFilter](analyzer_regularexpressionreplacefilter.png)

#### ShingleFilter

Creates shingles, which are groups of words.

_Example with token separator being a white space, `Max shingle size` = 4 and `Min shingle size` = 1:_

![ShingleFilter](analyzer_shinglefilter.png)

#### StopFilter

Removes terms found in a list. Lists can be managed in tab `Schema` / `Stop words`.

_Example with default english list of stop words:_

![StopFilter](analyser_stopfilter.png)

See [page How to set up stop words](how_to_set_up_stop_words.md) for more information.

#### YouTubeFilter and DailymotionFilter

These filters use API from YouTube and DailyMotion to get information about a video from an URL. 
See [page How to parse YouTube URLs and extract data](../parsing/how_to_parse_YouTube_URLs_and_extract_data.md) for more information.
