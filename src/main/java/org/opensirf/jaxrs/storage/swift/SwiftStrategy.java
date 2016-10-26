package org.opensirf.jaxrs.storage.swift;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.opensirf.catalog.SIRFCatalog;
import org.opensirf.container.MagicObject;
import org.opensirf.container.ProvenanceInformation;
import org.opensirf.container.SIRFContainer;
import org.opensirf.format.ProvenanceInformationMarshaller;
import org.opensirf.format.SIRFCatalogMarshaller;
import org.opensirf.format.SIRFCatalogUnmarshaller;
import org.opensirf.jaxrs.config.ContainerConfiguration;
import org.opensirf.jaxrs.storage.IStorageContainerStrategy;

public class SwiftStrategy implements IStorageContainerStrategy {
	protected SwiftStrategy() {
		
	}
	
	public SwiftStrategy(ContainerConfiguration c) { 
		this.config = c;
	}
	
	public void setConfig(ContainerConfiguration c) {
		this.config = c;
	}

	public ContainerConfiguration getConfig() {
		return config;
	}

	private ContainerConfiguration config;

	@Override
	public MagicObject retrieveMagicObject(String containerName) {
		try {
			SwiftDriver driver = new SwiftDriver(config);
			MagicObject mo = driver.containerMetadata(containerName);
			driver.close();
			return mo;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
		
		return null;
	}

	@Override
	public void createContainer(String containerName) {
		try {
			SwiftDriver driver = new SwiftDriver(config);
			System.out.println("Calling driver, container name = " + containerName);
			driver.createContainerAndMagicObject(containerName);
			driver.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public SIRFCatalog getCatalog(String containerName) {
		SIRFCatalog catalog = null;

		try {
			SwiftDriver driver = new SwiftDriver(config);
			InputStream is = driver.getFileInputStream(containerName, SIRFContainer.SIRF_DEFAULT_CATALOG_ID);
			
			catalog = new SIRFCatalogUnmarshaller("application/json").unmarshalCatalog(is);
			is.close();
			driver.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (JAXBException jbe) {
			jbe.printStackTrace();
		} catch(NullPointerException npe) {
			return null;
		}

		return catalog;
	}

	@Override
	public StreamingOutput getPreservationObjectStreamingOutput(String containerName, String poName) {
		SwiftDriver driver = new SwiftDriver(config);
		StreamingOutput so = null;
		
		try {
			final InputStream is = driver.getFileInputStream(containerName, poName);
			
			so = new StreamingOutput() {
				public void write(OutputStream out) throws IOException, WebApplicationException {
                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = is.read(bytes)) != -1)
                        out.write(bytes, 0, read);
				}
			};
			
			is.close();
			driver.close();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		return so;
	}


	@Override
	public void deletePreservationObject(String poName, String containerName) {
		try {
			SwiftDriver driver = new SwiftDriver(config);
			driver.deleteObject(containerName, poName);
			driver.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public InputStream getPreservationObjectInputStream(String containerName, String poUUID) {
		try {
			SwiftDriver driver = new SwiftDriver(config);
			InputStream is = driver.getFileInputStream(containerName, poUUID);
			driver.close();
			return is;
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return null;
	}

	public String marshalConfig(SwiftConfiguration config) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(SwiftConfiguration.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			jaxbMarshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
			StringWriter w = new StringWriter();
			jaxbMarshaller.marshal(config,w);			
			return w.toString();
			
		} catch(JAXBException je) {
			je.printStackTrace();
		}
		
		return null;
	}
	
	public SwiftConfiguration unmarshalConfig(String configPath) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(SwiftConfiguration.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			jaxbUnmarshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
			return (SwiftConfiguration) jaxbUnmarshaller.unmarshal(new StreamSource(new FileInputStream(configPath)), SwiftConfiguration.class).getValue();
		} catch(JAXBException je) {
			je.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		return null;
	}

	@Override
	public void pushProvenanceInformation(String authorName, String containerName) {
		SwiftDriver driver = new SwiftDriver(config);

		try {
			driver.uploadObjectFromString(containerName, SIRFContainer.SIRF_DEFAULT_PROVENANCE_MANIFEST_FILE,
				new ProvenanceInformationMarshaller("application/json")
				.marshalProvenanceInformation(new ProvenanceInformation(authorName)));

			driver.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (JAXBException jbe) {
			jbe.printStackTrace();
		}
	}

	@Override
	public void pushCatalog(SIRFCatalog catalog, String containerName) {
		SwiftDriver driver = new SwiftDriver(config);

		try {
			SIRFCatalog existingCatalog = getCatalog(containerName);
			
			// Only metadata updates
			if(existingCatalog != null && existingCatalog.getSirfObjects() != null) {					
				if (existingCatalog.getSirfObjects().size() >= 0 &&
						catalog.getSirfObjects().size() == 0) {
					catalog.getSirfObjects().addAll(existingCatalog.getSirfObjects());
				}
			}
			
			// TODO: else throw exception; number of POs can only change via a PO upload
			
			driver.uploadObjectFromString(containerName, SIRFContainer.SIRF_DEFAULT_CATALOG_ID,
					new SIRFCatalogMarshaller("application/json").marshalCatalog(catalog));
			driver.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (JAXBException jbe) {
			jbe.printStackTrace();
		}
	}

	@Override
	public void pushPreservationObject(String poUUID, byte[] b, String containerName) {
		try {
			SwiftDriver driver = new SwiftDriver(config);
			driver.uploadObjectFromByteArray(containerName, poUUID, b);
			driver.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void deleteContainer(String containerName) {
		SwiftDriver driver = new SwiftDriver(config);

		try {
			driver.deleteContainer(containerName);
			driver.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
}