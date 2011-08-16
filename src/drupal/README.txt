OpenSearchServer for Drupal
====================
This module allows to use OpenSearchServer to enable full text search on Drupal-based websites.
This module replaces Drupal's built-in search functionality and it has a lot of 
features and better performance compared to core search in drupal.This module has some 
Key Features like

	* Filter search results by Facet Field,
	* Search through every type of document,
	* Easy to set up with just filling a form.
	
INSTALLATION
------------
	Prerequisite:
		* Java 5 or higher.
		* PHP 5 or higher.
		* OpenSearchServer 1.2 or higher
		* Drupal 6.x
	
1) Check that you have a running OpenSearchServer instance.If you have any issues 
   while installing OpenSearchServer check this documentation 
		http://www.open-search-server.com/documentation
	
2) Download OpenSearchServer drupal module from the http://drupal.org/sandbox/ekeller/1128202 and 
   unpack it in <Your-drupal-installation>/sites/modules/

CONFIGURATION
-------------
1)  Enabled the OpenSearchServer drupal module from 
	 http://your-website.com/admin/build/modules/
	 
2)  You'll now find a OpenSearchServer menu in the Site configuration
    administration page available at:
       http://your-website.com/admin/settings/opensearchserver
	   
3)  Enter the details of the running OpenSearchServer instance and details
    about the fields to be indexed click the "Create-index/Save" button
	to create or save the index details.
	
4)  Click the "Re-index site" for indexing first and for re-indexing.

5)	The username and the key values are obtained in the OpenSearchServer instance 
    under the privileges tab.


FAQ 
---

Q: What is OpenSearchServer?

	A: Open Search Server is a search engine software developed under the GPL v3 
	   open source licence. Read more on http://www.open-search-server.com

Q: How to update the search index?

	A: Using the Re-index Site button in the OpenSearchServer Settings page.
	   and for frequent updation enable the curl feature of drupal.
	  
Q: When i click Create-index/Save button or reindex button i got an exception with Bad credential

	A: Check the credential is correct that you have create in OpenSearchServer instance under the privilages tab.

Q: I get an error when I install opensearchserver "Fatal error: OSS_API won't work without 
   curl extension in "opensearchserver-search\OSS_API.class.php" on line 23"

	A: Check that you server is enabled with CURL extension else install it.
	