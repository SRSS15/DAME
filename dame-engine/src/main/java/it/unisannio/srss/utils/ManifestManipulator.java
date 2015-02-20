/*
 * Copyright 2015 
 * 	Danilo Cianciulli 			<cianciullidanilo@gmail.com>
 * 	Emranno Francesco Sannini 	<esannini@gmail.com>
 * 	Roberto Falzarano 			<robertofalzarano@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unisannio.srss.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Fornisce metodi per la manipolazione dei file
 * <code>AndroidManifest.xml</code>
 */
public class ManifestManipulator {

	private final static Logger LOG = LoggerFactory
			.getLogger(ManifestManipulator.class);

	private final static String PERMISSION_PREFIX = "android.permission.";
	
	private final Document manifest;
	private final Element manifestElement, applicationElement;
	private final File inputManifest;

	public ManifestManipulator(File inputManifest) throws FileNotFoundException {
		// controllo dell'input
		if (inputManifest == null) {
			String err = "Input manifest file cannot be null!";
			LOG.error(err);
			throw new IllegalArgumentException(err);
		}
		if (!inputManifest.canRead() || !inputManifest.isFile()) {
			String err = "Input manifest file does not exist or cannot be read: "
					+ inputManifest.getAbsolutePath();
			LOG.error(err);
			throw new FileNotFoundException(err);
		}
		this.inputManifest = inputManifest;
		// parsing del file di input
		LOG.info("Parsing the manifest: " + inputManifest.getAbsoluteFile());
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			manifest = docBuilder.parse(inputManifest);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		// un minimo di controllo di conformità
		NodeList nodes = manifest.getElementsByTagName("manifest");
		if (nodes.getLength() != 1) {
			String err = "Malformed input manifest!";
			LOG.error(err);
			throw new IllegalArgumentException(err);
		}
		manifestElement = (Element) nodes.item(0);
		nodes = manifestElement.getElementsByTagName("application");
		if (nodes.getLength() != 1) {
			String err = "Malformed input manifest!";
			LOG.error(err);
			throw new IllegalArgumentException(err);
		}
		applicationElement = (Element) nodes.item(0);
	}

	/**
	 * Sovrascrive il manifest (eventualmente modificato).
	 * 
	 * @return <code>true</code> se il l'operazione è andata a buon fine,
	 *         <code>false</code> altrimenti.
	 * @throws FileNotFoundException
	 *             Se il file di output non può essere creato o scritto.
	 */
	public boolean writeOutputManifest() throws FileNotFoundException {
		return writeOutputManifest(inputManifest);
	}

	/**
	 * Scrive il manifest (eventualmente modificato) su file.
	 * 
	 * @param output
	 *            Il file di output
	 * @return <code>true</code> se il l'operazione è andata a buon fine,
	 *         <code>false</code> altrimenti.
	 * @throws FileNotFoundException
	 *             Se il file di output non può essere creato o scritto.
	 */
	public boolean writeOutputManifest(File output)
			throws FileNotFoundException {
		if (output == null) {
			String err = "Output manifest file cannot be null!";
			LOG.error(err);
			throw new IllegalArgumentException(err);
		}
		if (output.isFile() && !output.canWrite()) {
			String err = "Could not write to output manifest file: "
					+ output.getAbsolutePath();
			LOG.error(err);
			throw new FileNotFoundException(err);
		}
		File parent = output.getParentFile();
		if ((!parent.isDirectory() && !parent.mkdirs())
				|| (parent.isDirectory() && !parent.canWrite())) {
			String err = "Could not write output manifest to folder: "
					+ parent.getAbsolutePath();
			LOG.error(err);
			throw new FileNotFoundException(err);
		}
		LOG.info("Saving the manifest: " + output.getAbsolutePath());
		DOMSource source = new DOMSource(manifest);
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			LOG.error(e.getMessage(), e);
			return false;
		}
		StreamResult result = new StreamResult(output);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			LOG.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * Aggiunge permessi al manifest
	 * 
	 * @param permissions
	 *            La lista di permessi da aggiungere.
	 */
	public void addPermissions(String... permissions) {
		if (permissions == null || permissions.length == 0) {
			LOG.warn("No permissions passed!");
			return;
		}
		// copia locale della lista dei permessi da inserire nel manifest
		Set<String> permissionsCopy = new HashSet<String>(permissions.length);
		for (String permission : permissions) {
			if(!permission.startsWith(PERMISSION_PREFIX))
				permission = PERMISSION_PREFIX + permission;
			LOG.debug("Required permission: " + permission);
			permissionsCopy.add(permission);
		}

		// si controllano i permessi già presenti nel manifest
		NodeList nodes = manifestElement
				.getElementsByTagName("uses-permission");
		Element permissionElement;
		for (int i = 0; i < nodes.getLength(); i++) {
			permissionElement = (Element) nodes.item(i);
			String permission = permissionElement.getAttribute("android:name");
			if (permissionsCopy.remove(permission))
				LOG.debug("Required permission \"" + permission
						+ "\" is already present in the input manifest");
		}
		for (String permission : permissionsCopy) {
			LOG.info("Adding permission: " + permission);
			permissionElement = manifest.createElement("uses-permission");
			permissionElement.setAttribute("android:name", permission);
			manifestElement.appendChild(permissionElement);
		}
		if (permissionsCopy.size() == 0)
			LOG.info("The AndroidManifest already contains all required permissions");
	}

	/**
	 * Aggiunge permessi al manifest
	 * 
	 * @param permissions
	 *            La lista di permessi da aggiungere.
	 */
	public void addPermissions(Collection<String> permissions) {
		if (permissions == null || permissions.size() == 0) {
			LOG.warn("No permissions passed!");
			return;
		}
		addPermissions(permissions.toArray(new String[0]));
	}

	public void addServices(String... services) {
		if (services == null || services.length == 0) {
			LOG.warn("No services passed!");
			return;
		}
		// copia locale della lista dei servizi da inserire nel manifest
		Set<String> servicesCopy = new HashSet<String>(services.length);
		for (String service : services) {
			LOG.debug("Required service: " + service);
			servicesCopy.add(service);
		}

		Element serviceElement;
		for (String service : servicesCopy) {
			LOG.info("Adding service: " + service);
			serviceElement = manifest.createElement("service");
			serviceElement.setAttribute("android:name", service);
			serviceElement.setAttribute("android:exported", "false");
			applicationElement.appendChild(serviceElement);
		}
	}

	public void addServices(Collection<String> services) {
		if (services == null || services.size() == 0) {
			LOG.warn("No services passed!");
			return;
		}
		addServices(services.toArray(new String[0]));
	}

	public Set<String> getMainActivities() {
		// TODO
		throw new UnsupportedOperationException("Not implemented");
	}
}
