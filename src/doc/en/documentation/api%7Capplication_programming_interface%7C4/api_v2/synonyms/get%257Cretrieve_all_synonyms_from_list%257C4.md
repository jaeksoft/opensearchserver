Use this API to retrieve all synonyms from a list

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/synonyms/{list_name}```

**Method:** ```GET```

**Header**:
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**list_name**_ (required): The name of the list for which retrieve synonyms from.

### Success response
All synonymes from the list are returned. One group of synonyms by line, each synonym separated by a comma:

    
canapé,banquette,sofa
vaisselier,buffet
    

**HTTP code:**
200

**Content (application/json):**


### Error response

The command failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XGET http://localhost:8080/services/rest/index/my_index/synonyms/my_list
    

**Using jQuery:**

    $.ajax({ 
       type: "GET",
       dataType: "json",
       url: "http://localhost:8080/services/rest/index/my_index/synonyms/my_list"
    }).done(function (data) {
       console.log(data);
    });
   