## Creating or updating a list of synonyms

Use this API to create or to update a list of synonyms.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/synonyms/{list_name}```

**Method:** ```PUT```

**Header**:
- _**Content-Type**_ (required): ```text/plain```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**list_name**_ (required): The name of the list to create or update.


**Raw data (PUT):**

One line for each group of synonyms. The synonyms within a group are separated by commas.
    
couch,sofa,divan

desk,bureau
    

### Success response
The synonyms list has been created or updated.

**Update:**

    {
        "successful": true,
        "info": "Item mylist updated"
    }
    

**Create:**

    {
        "successful": true,
        "info": "Item mylist2 created"
    }
    


**HTTP code:**
200

**Content (application/json):**


### Error response
The command failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: text/plain" \
        -d '...' \
        http://localhost:8080/services/rest/index/my_index/synonyms/my_list
