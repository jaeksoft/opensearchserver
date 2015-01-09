## How to configure advanced authentication

OpenSearchServer embed a powerful authentication feature. It allows for returning different results for the same query depending on the user or groups used for the query.

### How the authentication work

Access information related to each document must be indexed alongside the documents. This information must go into 4 specific fields:

* one will store a _white list_ of users (commonly called `userAllow`)
* one will store a _black list_ of users (commonly called `groupDeny`)
* one will store a _white list_ of groups (commonly called `groupAllow`)
* the last one will store a _black list_ of groups (commonly called `groupDeny`)

When querying, you will need to pass two additionnal parameters: `user` and `groups`. OpenSearchServer will run the query as usual but will also compare values from these parameters to the values indexed in the specific fields.

* for a document, if value from the `user` parameter is found in the black list of users, **or** if one value of the `groups` parameter is found in the black list of groups then this result **will never be returned** for this query.
    * for a document to be filtered out from the results set there is no need to match both `userDeny` and `groupDeny`: as soon as one of the field match one of the given parameters the document is excluded from the results.
* on the other hand, if value from the `user` parameter is found in the white list of users, **or** if one value of the `groups` parameter is found in the white list of groups for a document then this result will be returned.

### Configuring schema

4 fields must be added to the schema of the index:

![Fields for authentication](auth_fields.png)

Then, going to tab Schema / Authentication, you will need to tell OpenSearchServer which field should have which role:

![Mapping fields](auth_role.png)

**Don't forget to check "Enable authentication"** for the authentication to be taken into account! As soon as this checkbox gets unchecked authentication is immediately deactivated.

### Querying the index

Let's imagine an index with these 3 documents:

![Adding documents](auth_add_docs.png)

1. First doc is visible by the "anonymous" group
2. Second doc is visible by the "authenticated" group, but users "john" and "jack" are forbidden! It means **even if "john" or "jack" belongs to the "authenticated" group they must not be able to see this document**.
3. Third doc is **only visible by two users**: "will" and "sarah"

Let's run some test queries:

See at the end of the page for details about the simple search template used for this example. "Postman" extension for Chrome is used.

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

