## Installing the OpenSearchServer module for Drupal 7

### Requirements

In addition to the required modules detailed below you will need a running instance of OpenSearchServer. [SaaS hostings](http://www.opensearchserver.com/#saas) are perfectly suited for this.

### Installing dependencies

Several modules are required by the OpenSearchServer module. 

The main one is **Search API**. This module also has some dependencies.

Here is the full list of modules that must be enabled for OpenSearchServer module to work porperly:

* Search API
* Entity
* Facet API
* Libraries

You may also want to install and enable from now on these modules. They will be useful for creating search pages:

* Views
* CTools
 
#### Full installation process

Here are the full steps to follow for a fresh new Drupal website for being able to start OpenSearchServer module:

##### Installing Search API

You could for example use the built in installation feature. In _/admin/modules_, click "Install new module".

![Add new module](drupal_newmodule.png)

Copy/past link for a `.tar.gz` or a `.zip` link for module Search API.

![Install Search API](drupal_searchapi.png)

Click "Install", and then click "Enable newly added modules" to come back to the Modules page.

![Go back to list of modules](drupal_enable.png)

##### Installing required modules for Search API

You now see that Search API, Search facets and Search views also have dependencies. We will install them all.

![Dependencies for Search API](drupal_searchapi_dependencies.png)

Following the above process, install modules **Entity**, **Views** and **Facetapi**.

New dependencies are now shown. Install module **CTools**.

##### Enabling modules

Check modules **Search API**, **Search facets**, **Search views** and click on "Save configuration". 
 
![Enable Search API](drupal_enablemodules.png)

On next page click on button "Continue" to also enable required modules.

![Accept enabling of other modules](drupal_enablemodules2.png)

### Installing OpenSearchServer module

Download the module (_-- download link will be added soon --_) and upload it to `/sites/all/modules`. Go to page _/admin/modules_.

OpenSearchServer module needs module **Libraries**. Using steps explained above, install this module.

![Dependency for OpenSearchServer](drupal_searchossdependency.png)
 
#### Enabling OpenSearchServer

Check module "Search API OpenSearchServer" and click on "Save configuration". 

![Enable OpenSearchServer](drupal_searchossenable.png)

On next page click on button "Continue".
 
![Enable OpenSearchServer](drupal_searchossenable2.png)
 
### Installing OpenSearchServer's PHP library

Download version 1 of OpenSearchServer's PHP library: [https://github.com/jaeksoft/opensearchserver-php-client/archive/1.x.zip](https://github.com/jaeksoft/opensearchserver-php-client/archive/1.x.zip).

Unzip it to `/sites/all/libraries` and rename folder from `opensearchserver-php-client-1.x` to `opensearchserver`.

**Take care**: destination folder is **`/sites/all/libraries`**, not `/sites/all/modules/libraries`.


---

Installation is over! It's time for some configuration.