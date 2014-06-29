## RESTFul API authentication

If you created a user, each API call requires to pass a `login` and an `API key`.

Users can be created in tab `Privileges` in OpenSearchServer interface. API keys are automatically generated.

**URL parameters:**
- _**login**_ (required): Login of the user
- _**key**_ (required): API key generated for this login

### Sample calls

* List indexes: 

`curl -XGET http://localhost:9090/services/rest/index?login=lorem&key=08762e43getye0042f875e86eaiu687f`
* Update document in JSON: 

```shell
curl -XPUT -H "Content-Type: application/json" \
    -d '[{"lang": "ENGLISH","fields": [{ "name": "id", "value": 1 }]}]' \
    http://localhost:9090/services/rest/index/my_index/document?login=lorem&key=08762e43getye0042f875e86eaiu687f
```