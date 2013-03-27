<?php

/**
 * @file
 * Class to access miscellaneous functions
 * @package OpenSearchServer
 */

/**
 * Store and retrieve a value from the browser (In order REQUEST, COOKIE, DEFAULT)
 * @return unknown_type
 */
function config_request_value($key, $default, $request_field = NULL) {
  $value = NULL;
  if (!empty($_REQUEST[$request_field])) {
    $value = $_REQUEST[$request_field];
    setcookie($key, $value, time() + 3600 * 365, '/');
  }
  if (!$value && isset($_COOKIE[$key])) {
    $value = $_COOKIE[$key];
  }
  if (!$value) {
    $value = $default;
  }
  return $value;
}

/**
 * Retrieve an XML feed
 * @param string $url The feed URL
 * @param array $curl_info By Reference. If given, the informations provided by curl will be returned using the provided array
 * @return SimpleXMLElement Will return FALSE if something gone wrong
 */
function retrieve_xml($url, &$curl_info = NULL) {

  $rcurl = curl_init($url);
  curl_setopt($rcurl, CURLOPT_BINARYTRANSFER, TRUE);
  curl_setopt($rcurl, CURLOPT_RETURNTRANSFER, TRUE);
  curl_setopt($rcurl, CURLOPT_FOLLOWLOCATION, TRUE);
  curl_setopt($rcurl, CURLOPT_CONNECTTIMEOUT, 10);
  curl_setopt($rcurl, CURLOPT_TIMEOUT, 5);
  $content = curl_exec($rcurl);

  if ($curl_info !== NULL) {
    $curl_info = curl_getinfo($rcurl);
  }

  if ($content === FALSE) {
    trigger_error('CURL failed to execute on URL "' . $url . '"');
    return FALSE;
  }

  $previous_error_level = error_reporting(0);
  $xml = simplexml_load_string($content);
  error_reporting($previous_error_level);

  return (!$xml instanceof SimpleXMLElement) ? FALSE : $xml;

}

/**
 * Wrapper for reset to use arrays returned from functions and methods
 * @param $array
 * @return mixed
 */
function array_first($array) {
  return reset($array);
}

/**
 * Wrapper for end to use arrays returned from functions and methods
 * @param $array
 * @return mixed
 */
function array_last($array) {
  return end($array);
}

/**
 * Misc classes to parse and simplify usage of different feed format during the indexation
 */
abstract class NewsFeedParser extends ArrayObject {

  protected $feedFormat;
  protected $channelTitle;
  protected $channelSubtitle;
  protected $channelHome;

  /**
   * @param SimpleXMLElement $xml
   * @return NewsFeedParser
   */
  public static function factory(SimpleXMLElement $xml) {
    // Determine the format of the xml
    // RSS
    if (isset($xml->channel->item[0])) {
      return new NewsFeedParser_RSS($xml);
    }
    // Atom
    elseif (isset($xml->entry[0])) {
      return new NewsFeedParser_Atom($xml);
    }
  }

  public function getFeedFormat() {
    return $this->feedFormat;
  }

  public function getChannelTitle() {
    return $this->channelTitle;
  }

  public function getChannelSubtitle() {
    return $this->channelSubtitle;
  }

  public function getChannelHome() {
    return $this->channelHome;
  }

}

abstract class NewsFeedParser_Feed_Entry {

  protected $author;
  protected $content;
  protected $id;
  protected $link;
  protected $published;
  protected $summary;
  protected $title;
  protected $language;

  public function getLanguage() {
    return $this->language;
  }

  public function getAuthor() {
    return $this->author;
  }

  public function getContent() {
    return $this->content;
  }

  public function getId() {
    return $this->id;
  }

  public function getLink() {
    return $this->link;
  }

  public function getPublished() {
    return $this->published;
  }

  public function getSummary() {
    return $this->summary;
  }

  public function getTitle() {
    return $this->title;
  }
}

class NewsFeedParser_RSS extends NewsFeedParser {

  /**
   * @param SimpleXMLElement $xml
   * @return NewsFeedParser
   */
  public function __construct(SimpleXMLElement $xml) {

    $this->feedFormat = 'RSS';

    // Misc informations
    $this->channelTitle    = (string) $xml->channel->title;
    $this->channelSubtitle  = (string )$xml->channel->description;
    $this->channelHome    = (string) $xml->channel->link;

    // Entries
    $items = (array)$xml->xpath('channel/item');
    foreach ($items as $item) {
      $this->append(new NewsFeedParser_RSS_Entry($item));
    }
  }

}

class NewsFeedParser_RSS_Entry extends NewsFeedParser_Feed_Entry {

  public function __construct(SimpleXMLElement $xml) {

    $this->id = md5((string)$xml->guid);
    $this->link = $xml->link;
    $this->published = date('Y-m-d\TH:i:sO', strtotime((string)$xml->pubDate));
    $this->summary = (string)$xml->description;
    $this->title = $xml->title;

    // Only RSSS2.0
    $this->author  = (string)$xml->author;
    if (empty($this->author)) {
      $this->author  = $xml->children("http://purl.org/dc/elements/1.1/")->creator;
    }
    $this->content = (string)$xml->children('http://purl.org/rss/1.0/modules/content/');

  }

}

class NewsFeedParser_Atom extends NewsFeedParser {

  /**
   * @param SimpleXMLElement $xml
   * @return NewsFeedParser
   */
  public function __construct(SimpleXMLElement $xml) {

    $this->feedFormat = 'ATOM';

    // Misc informations
    $this->channelTitle    = (string)$xml->title;
    $this->channelSubtitle  = (string)$xml->subtitle;
    $this->channelHome    = preg_replace('/(\.\w+)\/.*$/', '$1', (string)$xml->id);

    // Entries
    foreach ($xml->entry as $item) {
      $this->append(new NewsFeedParser_Atom_Entry($item));
    }
  }

}

class NewsFeedParser_Atom_Entry extends NewsFeedParser_Feed_Entry {

  public function __construct(SimpleXMLElement $xml) {

    $this->id = md5((string)$xml->id);
    $this->link = $xml->link['href'];
    $this->published = date('Y-m-d\TH:i:sO', strtotime((string)$xml->published));
    $this->summary = (string)$xml->content;
    $this->title = $xml->title;

    // Only RSSS2.0
    $this->author  = (string)$xml->author->name;
    $this->content = (string)$xml->content;

  }

}


function indentXML($string) {

  function indentXML_pregCallback($matches) {
    static $indent = 0;
    static $indentExclusion = array('?');
    if (substr($matches[0], 0, 9) == "<[CDATA[!") {
      $pad = str_repeat(' ', max(0, $indent));
    }
    elseif ($matches[0][1] == '?') {
      $pad = str_repeat(' ', max(0, $indent));
    }
    elseif ($matches[0][1] == '/') {
      $indent--;
      $pad = str_repeat(' ', max(0, $indent));
    }
    elseif (substr($matches[0], -2, 1) != '/') {
      $indent++;
      $pad = str_repeat(' ', max(0, $indent-1));
    }
    return $pad . $matches[0] . ($indent ? "\n" : "");
  }

  return preg_replace_callback('/<[^>]+>/', "indentXML_pregCallback", $string);

}

function beautifulXML($string) {

  function beautifulXML_tagging($string) {
    $string = preg_replace('/^(\w+)/i', '<span class="nodeName">$1</span>', $string);
    return $string;
  }

  function beautifulXML_pregCallback($matches) {
    $before = '';
    $after  = '';
    if (substr($matches[0], 0, 9) == "<![CDATA[") {
      $before  = '<div class="node"><span class="delimiter">&lt;[!CDATA[</span><span class="cdata">';
      $content = substr($matches[0], 9, -3);
      $after   = '</span><span class="delimiter">]]&gt;</span></div>';
    }
    elseif ($matches[0][1] == '?') {
      $before = '<div class="node"><span class="delimiter">&lt;?</span>';
      $content = beautifulXML_tagging(substr($matches[0], 2, -2));
      $after  = '<span class="delimiter">?&gt;</span></div>';
    }
    elseif ($matches[0][1] == '/') {
      $before = '<span class="delimiter">&lt;/</span>';
      $content = beautifulXML_tagging(substr($matches[0], 2, -1));
      $after  = '<span class="delimiter">&gt;</span></div>';
    }
    elseif (substr($matches[0], -2, 1) != '/') {
      $before = '<div class="node"><span class="delimiter">&lt;</span>';
      $content = beautifulXML_tagging(substr($matches[0], 1, -1));
      $after  = '<span class="delimiter">&gt;</span>';
    }

    return $before . $content . $after;
  }

  return preg_replace_callback('/<[^>]+>/', "beautifulXML_pregCallback", $string);

}
?>