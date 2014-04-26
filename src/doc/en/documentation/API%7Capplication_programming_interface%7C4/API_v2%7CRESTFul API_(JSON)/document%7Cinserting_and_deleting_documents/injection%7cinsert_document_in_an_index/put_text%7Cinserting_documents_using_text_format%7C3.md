The TEXT API will read one document for each line in the TEXT file. A regular expression must be provided to determine what will captured. Each capture is copied to a field of the schema.

Use this API to add to the index documents in plain TEXT formats (CSV, TTL).

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/document```

**Method:** ```PUT```

**Header**:
- _**Content-Type**_ (required): ```text/plain```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.

**Query parameters:**
- _**pattern**_ (required): A regular expression pattern capturing the field in the text line.
- _**field**_ (required): One field for each capture in the regular expression (field mapping).
- _**langpos**_ (optional): The number of the capture containing the language.
- _**charset**_ (optional): The charset of the text (default is UTF-8).
- _**buffersize**_ (optional): The size of the buffer when indexing the lines (default is 100).

**Raw data (PUT):**
Text lines.

    <http://fr.dbpedia.org/resource/!!!> <http://www.w3.org/2000/01/rdf-schema#comment> "!!!, qui se prononce tchik tchik tchik ou à la convenance toute autre syllabe répétée trois fois, est un groupe américain formé pendant l'été 1995 de la fusion d'une partie des groupes Black Liquorice et Popesmashers. Ce nom à but anticonformiste a l'inconvénient d'être, selon Spin Magazine, « le plus dur des noms de groupe pour Google »."@fr .
    <http://fr.dbpedia.org/resource/$5000_Reward,_Dead_or_Alive> <http://www.w3.org/2000/01/rdf-schema#comment> "$5000 Reward, Dead or Alive est un film muet américain réalisé par Allan Dwan et sorti en 1911."@fr .
    <http://fr.dbpedia.org/resource/$O$> <http://www.w3.org/2000/01/rdf-schema#comment> "$O$ est le premier album du groupe Sud-Africain Die Antwoord."@fr .
    
### Sample call

    curl -XPUT -H "Content-Type: text/plain" --upload-file my_file.txt 'http://localhost:8080/services/rest/index/gendarmerie_test/document?pattern=%5E%3C%28%5B%5E%3E%5D*%29%3E+%3C%5B%5E%3E%5D*%3E+%22%28%5B%5E%22%5D*%29%22%40%28%5Ba-zA-Z%5C-_%5D*%29+%5C.%24&field=url&field=abstract&field=lang&langpos=3&charset=UTF-8&buffersize=100'
    

### Success response
The document(s) has been created or updated.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "95 document(s) updated."
    }
    

### Error response

The creation/update failed. The reason is provided in the content.

**HTTP code:**
500
