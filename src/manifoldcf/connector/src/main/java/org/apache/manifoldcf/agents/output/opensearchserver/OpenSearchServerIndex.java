package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;

public class OpenSearchServerIndex extends OpenSearchServerConnection {

	public OpenSearchServerIndex(String documentURI, InputStream inputStream,
			OpenSearchServerParam params) throws ManifoldCFException {
		super(params);
		try {
			StringBuffer url = getApiUrl("update");
			PutMethod put = new PutMethod(url.toString());
			RequestEntity entity = new StringRequestEntity(getXML(documentURI,
					inputStream), "text/xml", "UTF-8");
			put.setRequestEntity(entity);
			call(put);
		} catch (UnsupportedEncodingException e) {
			throw new ManifoldCFException(e);
		} catch (XMLStreamException e) {
			throw new ManifoldCFException(e);
		} catch (IOException e) {
			throw new ManifoldCFException(e);
		}
	}

	private String getXML(String documentURI, InputStream inputStream)
			throws XMLStreamException, IOException {
		String fileName = FilenameUtils.getName(documentURI);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter sw = new StringWriter();
		XMLStreamWriter writer = factory.createXMLStreamWriter(sw);
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeStartElement("index");
		writer.writeStartElement("document");
		writer.writeStartElement("field");
		writer.writeAttribute("name", "uri");
		writer.writeStartElement("value");
		writer.writeCData(documentURI);
		writer.writeEndElement(); // ends value
		writer.writeEndElement(); // ends field
		writer.writeStartElement("binary");
		writer.writeAttribute("fileName", fileName);
		writer.writeCharacters(new String(Base64.encodeBase64(
				IOUtils.toByteArray(inputStream), true)));
		writer.writeEndDocument();
		return sw.toString();
	}
}
