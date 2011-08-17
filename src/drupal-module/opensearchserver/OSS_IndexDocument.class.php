<?php
/*
 *  This file is part of Jaeksoft OpenSearchServer.
 *
 *  Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 *
 *  http://www.open-search-server.com
 *
 *  Jaeksoft OpenSearchServer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 */

if (!class_exists('OSS_API')) { trigger_error("OSS_Search won't work whitout OSS_API", E_USER_ERROR); die(); }
if (!class_exists('ArrayObject')) { trigger_error("OSS_IndexDocument won't work whitout SPL ArrayObject", E_USER_ERROR); die(); }

/**
 * @author pmercier <pmercier@open-search-server.com>
 * @package OpenSearchServer
 */
class OSS_IndexDocument extends ArrayObject {

	/**
	 * @param string $language ISO 639-1 format (en, de, fr, ...)
	 * @return OSS_IndexDocument_Document
	 */
	public function newDocument($language = '') {
		$document = new OSS_IndexDocument_Document($this, $language);
		$this->append($document);
		
		return $document;
	}

	/**
	 * @param mixed $offset
	 * @param OSS_IndexDocument_Document $document
	 */
	public function offsetSet($offset, $document) {
		if (!$document instanceof OSS_IndexDocument_Document)
			throw new UnexpectedValueException("OSS_IndexDocument_Document was expected.");
		parent::offsetSet($offset, $document);
	}

	/**
	 * @param OSS_IndexDocument_Document $document
	 */
	public function append($document) {
		if (!$document instanceof OSS_IndexDocument_Document)
			throw new UnexpectedValueException("OSS_IndexDocument_Document was expected.");
		parent::append($document);
	}

	/**
	 * @return string XML
	 */
	public function toXML() {
		return $this->__toString();
	}

	public function __toString() {
		$return  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<index>";
		foreach ($this as $document)
			$return .= $document->__toString();
		return $return.'</index>';
	}

}

/**
 * @author pmercier <pmercier@open-search-server.com>
 * @package OpenSearchServer
 */
class OSS_IndexDocument_Document extends ArrayObject {

	/** @var OSS_IndexDocument */
	private $indexDocument;

	/** @var string */
	private $language = null;

	/** @var Array<OSS_DocumentField> */
	private $fieldByName = array();

	/**
	 * @param OSS_IndexDocument $indexDocument
	 * @param string $language ISO 639-1 format (en, de, fr, ...)
	 * @return OSS_DocumentNode
	 */
	public function __construct(OSS_IndexDocument $indexDocument, $language = '') {
		$this->indexDocument = $indexDocument;
		$this->setLanguage($language);
	}

	/**
	 * Define the document language
	 * @param string $language ISO 639-1 format (en, de, fr, ...). Null to unset the language attribute.
	 * @return boolean True if language is supported. Null if language was unset.
	 * @throw UnexpectedValueException When language is not supported
	 */
	public function setLanguage($language) {
		static $supportedLanguages = null;

		if ($language === null) {
			$this->language = $language;
			return null;
		}

		if ($supportedLanguages === null)
			$supportedLanguages = OSS_API::supportedLanguages();
		
		if (isset($supportedLanguages[$language]))
			$this->language = (string)$language;
		else {
			if (class_exists('OSSException'))
				throw new UnexpectedValueException('Language "'.$language.'" is not supported.');
			trigger_error(__CLASS__.'::'.__METHOD__.'($lang): Language "'.$language.'" is not supported.', E_USER_ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Return the defined language of the document
	 * @return string ISO 639-1 format (en, de, fr, ...)
	 */
	public function getLanguage() {
		return $this->language;
	}

	/**
	 * Create a new field inside the document
	 * @param string $name The name of the field
	 * @param mixed $values The string to append. Can be an Array<String>
	 * @return OSS_IndexDocument_Field
	 * Note: If the field by that name already exist, it'll be returned
	 */
	public function newField($name, $values = null) {
		if (isset($this->fieldByName[$name]))
			return $this->fieldByName[$name];
		$field = new OSS_IndexDocument_Field($this, $name);
		$this->append($field);
		if ($values !== null)
			$field->addValues($values);
		return $field;
	}

	/**
	 * Retrieve a field using his name
	 * @param string $name The name of the field to retrieve
	 * @return OSS_IndexDocument_Field If field don't exist, null is returned
	 */
	public function getField($name) {
		if (isset($this->fieldByName[$name]))
			return $this->fieldByName[$name];
		return null;
	}

	/**
	 * @param mixed $offset
	 * @param OSS_IndexDocument_Field $field
	 */
	public function offsetSet($offset, $field) {
		if (!$field instanceof OSS_IndexDocument_Field)
			throw new UnexpectedValueException("OSS_IndexDocument_Field was expected.");
		parent::offsetSet($offset, $field);
		$this->fieldByName[$field->getName()] = $field;
	}

	/**
	 * @param OSS_IndexDocument_Field $field
	 */
	public function append($field) {
		if (!$field instanceof OSS_IndexDocument_Field)
			throw new UnexpectedValueException("OSS_IndexDocument_Field was expected.");
		$fieldName = $field->getName();
		if (isset($this->fieldByName[$fieldName])) {
			$storedField = $this->fieldByName[$fieldName];
			foreach ($field as $value) {
				$storedField->append($value);
			}
		}
		else {
			parent::append($field);
			$this->fieldByName[$field->getName()] = $field;
		}
	}

	public function __toString() {
		$data = '';
		foreach ($this as $field) {
			$field = $field->__toString();
			$data .= $field;
		}
		if (empty($data)) return null;
		$return = '<document';
		if ($this->language !== null)
			$return .= ' lang="'.$this->language.'"';
		$return .= '>';

		return $return.$data.'</document>';
	}

}

/**
 * @author pmercier <pmercier@open-search-server.com>
 * @package OpenSearchServer
 */
class OSS_IndexDocument_Field extends ArrayObject {

	/** @var OSS_IndexDocument_Document */
	protected $document;

	/** @var string */
	protected $name;

	/**
	 * @param OSS_IndexDocument_Document $document
	 * @param string $name The name of the field
	 * @return OSS_IndexDocument_Field
	 */
	public function __construct(OSS_IndexDocument_Document $document, $name) {
		$this->document = $document;
		$this->name = $name;
	}

	/**
	 * Return the name of the field
	 * @return string
	 */
	public function getName() {
		return $this->name;
	}

	/**
	 * Create a new value inside the field
	 * @param string $value The string to append
	 * @param boolean $removeTag Ask the indexator to remove the tags
	 * @return OSS_IndexDocument_Value
	 */
	public function newValue($value, $removeTag = false) {
		$value = new OSS_IndexDocument_Value($this, $value);
		$value->setRemoveTag($removeTag);
		$this->append($value);
		return $value;
	}

	/**
	 * Add one or many values to the field
	 * @param mixed $values The string to append. Can be an Array<String>
	 */
	public function addValues($values) {
		foreach ((array)$values as $value)
			$this->append(new OSS_IndexDocument_Value($this, $value));
	}

	/**
	 * @param mixed $offset
	 * @param OSS_IndexDocument_Value $value
	 */
	public function offsetSet($offset, $value) {
		if (!$value instanceof OSS_IndexDocument_Value)
			throw new UnexpectedValueException("OSS_IndexDocument_Value was expected.");
		parent::offsetSet($offset, $value);
	}

	/**
	 * @param mixed $offset
	 * @param OSS_IndexDocument_Value $value
	 */
	public function append($value) {
		if (!$value instanceof OSS_IndexDocument_Value)
			throw new UnexpectedValueException("OSS_IndexDocument_Value was expected.");
		parent::append($value);
	}

	public function __toString() {
		$return = '';
		foreach ($this as $value) {
			$value = $value->__toString();
			if ($value !== false)
				$return .= $value;
		}
		if (empty($return)) return null;
		return '<field name="'.$this->name.'">'.$return.'</field>';
	}

}

/**
 * @author pmercier <pmercier@open-search-server.com>
 * @package OpenSearchServer
 */
class OSS_IndexDocument_Value {

	/** @var string */
	private $field;

	/** @var boolean */
	private $removeTag = false;

	/** @var string */
	private $value = '';

	/**
	 * @param OSS_IndexDocument_Field $field
	 * @param string $value The value
	 * @return OSS_IndexDocument_Value
	 */
	public function __construct(OSS_IndexDocument_Field $field, $value) {
		$this->field = $field;
		$this->value = (string)$value;
	}


	/**
	 * Set the value
	 * @param string $value The value
	 */
	public function setValue($value) {
		$this->value = (string)$value;
	}

	/**
	 * Retrieve the value
	 * @return string
	 */
	public function getValue() {
		return $this->value;
	}

	/**
	 *  Ask the indexator to remove the tags
	 * @param boolean $bool
	 */
	public function setRemoveTag($bool) {
		$this->removeTag = (bool)$bool;
	}

	/**
	 * @return boolean
	 */
	public function getRemoveTag() {
		return $this->removeTag;
	}

	public function __toString() {
		$data = str_replace(']]>', ']]]]><![CDATA[>', $this->value);
		if (empty($data)) return null;
		$return = '<value';
		if ($this->removeTag) $return .= ' removeTag="yes"';
		return $return.'><![CDATA['.$data.']]></value>';
	}

}