## How to configure advanced authentication

**Requires OpenSearchServer > 1.5.10**

OpenSearchServer embed a powerful authentication feature. It allows for returning different results for the same query depending on the user or groups used for the query.

### How the authentication works

Access information related to each document must be indexed alongside the documents. This information must go into 4 specific fields:

* one will store a _white list_ of users (commonly called `userAllow`)
* one will store a _black list_ of users (commonly called `userDeny`)
* one will store a _white list_ of groups (commonly called `groupAllow`)
* the last one will store a _black list_ of groups (commonly called `groupDeny`)

When querying, you will need to **pass two additionnal parameters: `user` and `groups`**. OpenSearchServer will run the query as usual but will also **compare values from these parameters to the values indexed in the specific fields**.

* for a document, if value from the `user` parameter is found in the _black list_ of users, **or** if one value of the `groups` parameter is found in the _black list_ of groups then this result **will never be returned** for this query.
    * for a document to be filtered out from the results set there is no need to match both `userDeny` and `groupDeny`: as soon as one of the field match one of the given parameters the document is excluded from the results.
* on the other hand, if value from the `user` parameter is found in the _white list_ of users, **or** if one value of the `groups` parameter is found in the _white list_ of groups for a document then this result will be returned.

### Configuring schema

4 fields must be added to the schema of the index:

![Fields for authentication](auth_fields.png)

Then, going to tab Schema / Authentication, you will need to tell OpenSearchServer **which field should have which role**
:

![Mapping fields](auth_role.png)

**Don't forget to check "Enable authentication"** for the authentication to be taken into account! As soon as this checkbox gets unchecked authentication is immediately deactivated.

### Querying the index

Let's imagine an index with these 3 documents:

![Adding documents](auth_add_docs.png)

1. First doc is visible by the "anonymous" group
2. Second doc is visible by the "authenticated" group, but users "john" and "jack" are forbidden! It means **even if "john" or "jack" belongs to the "authenticated" group they must not be able to see this document**.
3. Third doc is **only visible by two users**: "will" and "sarah"

Let's run some test queries:

See below for details about the simple search template used for this example. "Postman" extension for Chrome is used.

* If a query is run without any parameters related to the authentication, no results will be returned :


```json
{
  "query": "cnn"
}
```

![Empty results](auth_query_empty.png)

* If the only given group is "anonymous", result is the only document visible by this group

```json
{
  "query": "cnn",
  "groups": ["anonymous"],
  "user": ""
}
```

![Anonymous](auth_anonymous.png)


* If groups are "anonymous" and "authenticated" but user is "john" result is the same because "john" can not see the doc #2:

```json
{
  "query": "cnn",
  "groups": ["anonymous", "authenticated"],
  "user": "john"
}
```

![Forbidden user](auth_john.png)


* If group is "anonymous" and user is "sarah", doc #1 and #3 are returned as expected:


```json
{
  "query": "cnn",
  "groups": ["anonymous"],
  "user": "sarah"
}
```

![White user](auth_sarah.png)


----

Search template used for this example:

```json
{   
  "start":0,
  "rows":10,
  "lang":"ENGLISH",
  "operator":"AND",
  "emptyReturnsAll":true,
  "returnedFields":[  
	 "title"
  ],
  "searchFields":[  
	 {  
		"field":"title",
		"mode":"TERM_AND_PHRASE",
		"boost":10.0,
		"phraseBoost":10.0
	 },
	 {  
		"field":"content",
		"mode":"TERM_AND_PHRASE",
		"boost":1.0,
		"phraseBoost":1.0
	 }
  ]
}
```

## Using an external index for storing authentication information

Storing authentication information with each document can leads to an hard to maintain index in case rights often change. Re-indexing of the whole document would be needed each time a right changes.

This issue can be easily solved **by using a dedicated index for storing authentication information**.

This index must have 5 fields : the 4 fields described above (`userAllow`,`userDeny`,`groupAllow`,`groupDeny`) plus a field that would be used to join information with the index storing the documents. In our example, that field would be `url`.

Here would be the schema for such an index:

`GET` on `http://localhost:9090/services/rest/index/articles_auth_access_info/field`:

```json
{  
   "successful":true,
   "info":"5 field(s)",
   "fields":[  
      {  
         "name":"userAllow",
         "indexed":"YES",
         "stored":"NO",
         "termVector":"YES"
      },
      {  
         "name":"userDeny",
         "indexed":"YES",
         "stored":"NO",
         "termVector":"YES"
      },
      {  
         "name":"groupAllow",
         "indexed":"YES",
         "stored":"NO",
         "termVector":"YES"
      },
      {  
         "name":"groupDeny",
         "indexed":"YES",
         "stored":"NO",
         "termVector":"YES"
      },
      {  
         "name":"url",
         "indexed":"YES",
         "stored":"NO",
         "termVector":"NO"
      }
   ],
   "unique":"url",
   "default":"url"
}
```

Field `url` is marked as **default** and **unique** field.

Fields `userAllow`,`userDeny`,`groupAllow` and `groupDeny` are deleted from first index `articles_auth` and its Authentication settings are changed:

![Auth settings](auth_settings.png)

Authentication settings are configured on the second index `article_auth_access_info`:

![Auth settings](auth_settings2.png)

Finally, authentication information is indexed:

![Add docs](auth_add_docs2.png)

That's all! Now when rights of documents change **you will only need to update the dedicated index** without having to index again all the document.