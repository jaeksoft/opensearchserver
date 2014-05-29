## How to allow search into words

Let's assume thay you have multiple indexed pages containing the word "ultraviolet". However, you would like to return this document when a user only searches for "ultra" or "violet".

This can be achieved by creating a new Analyzer, and applying this Analyzer to a new field of the schema.

The Analyzer will use the Ngram filter to derive multiple new words from the existings ones:
![ngram](ngram.png)

A new field must then be created. It will take its value from the existing `content` field and will use the new `NgramAnalyzer` on this content.

![ngram field](ngram_field.png)

The content must now be re-indexed, and the new `mega_content` field must be added to the fields searched by the query.
