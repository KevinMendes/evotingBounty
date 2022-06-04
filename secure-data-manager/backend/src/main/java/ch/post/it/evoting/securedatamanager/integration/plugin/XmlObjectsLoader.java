/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

public class XmlObjectsLoader {

	private static final XMLInputFactory XML_INPUT_FACTORY;

	static {
		//Disable XXE
		XML_INPUT_FACTORY = XMLInputFactory.newFactory();
		XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		XML_INPUT_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, false);
	}

	private XmlObjectsLoader() {
	}

	public static Plugins unmarshal(final Path xml) throws IOException, JAXBException, SAXException, XMLStreamException {
		return unmarshal(xml, "/xsd/plugins.xsd", Plugins.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(final Path xml, final String schemaPath, final Class<?> clazz)
			throws IOException, JAXBException, SAXException, XMLStreamException {
		final InputStream is = Files.newInputStream(xml);
		final XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(is);
		return (T) create(schemaPath, clazz).unmarshal(reader);
	}

	private static Unmarshaller create(final String schemaPath, final Class<?> clazz) throws JAXBException, SAXException {
		final Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
		if (schemaPath != null) {
			final Schema schema = getSchema(schemaPath, clazz);
			unmarshaller.setSchema(schema);
		}
		return unmarshaller;
	}

	private static Schema getSchema(final String schemaPath, final Class<?> clazz) throws SAXException {
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		final URL resource = clazz.getResource(schemaPath);
		return schemaFactory.newSchema(resource);
	}

	/**
	 * Validate the path parameter
	 *
	 * @param xmlPath
	 * @return true if OK RuntimeException if NOK
	 * @throws IOException
	 */
	public static Boolean validatePath(final String xmlPath) throws IOException {
		final Boolean output;
		final File f = new File(xmlPath);
		if (f.exists() && !f.isDirectory()) {
			output = true;
		} else {
			throw new IOException("Resource at " + xmlPath + " cannot be found");
		}
		return output;
	}

	/**
	 * Load xml file
	 *
	 * @param path
	 * @return
	 * @throws SAXException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static Plugins loadFile(final String path) throws IOException, JAXBException, SAXException, URISyntaxException, XMLStreamException {
		final String resourcePath;
		final Plugins plugins;
		resourcePath = XmlObjectsLoader.class.getResource(path).toURI().getPath();
		plugins = unmarshal(new File(resourcePath).toPath());
		return plugins;
	}

}
