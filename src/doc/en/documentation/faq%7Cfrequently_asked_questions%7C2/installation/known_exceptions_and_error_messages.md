## Bad credential

**Error:**

    <entry key="Exception">com.jaeksoft.searchlib.web.ServletException: com.jaeksoft.searchlib.SearchLibException: Bad credential</entry>

This exception occurs when the privileges have been created, but were not used while accessing through the API.
If one or more user have been created in OpenSearchServer, then every API call **must** contain some `login` and `key` information.

**Example:**

http://localhost:8080/select?user=index1&login=admin&key=54a51ee4f27cbbcb7a771352b980567f


See also: [https://github.com/jaeksoft/opensearchserver/wiki/Authentication](https://github.com/jaeksoft/opensearchserver/wiki/Authentication)

## Expected to get a directory path

**Error:**

    <entry key="Exception">
      com.jaeksoft.searchlib.web.ServletException: com.jaeksoft.searchlib.SearchLibException: Expected to get a directory path
    </entry>

This exception occurs when the index was not found. Verify whether the index exists in the indexes list.
 
**Example:**

http://localhost:8080/select?user=index1 

Index `index1` should be present in the list of indexes.
 
## PHP Library: OSS_API won't work whitout curl extension

**Error :**  
 
    Fatal error: OSS_API won't work whitout curl extension in OSS_API.class.php on line 23

The OpenSearchServer PHP client needs the `php-curl` extension. Install and enable the `php-curl` extension, then restart your web server.
