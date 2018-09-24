

/*
 * Copyright 2018 Thomas Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.iisys.smbcmis.extern.fileHandler;

import de.iisys.smbcmis.extern.DocumentConnector;
import de.iisys.smbcmis.extern.model.DocumentFile;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Alexander Schmid <alexander.schmid@hof-university.de>
 *
 *     This class provides methos for loading documents from the dms-server and converts it to pojos for the microservice.
 */
@SuppressWarnings("Duplicates")
@RequestScoped
public class FileHandlerForCMIS implements FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandlerForCMIS.class);

    @Inject
    private DocumentConnector documentConnector;

    /**
     * Loads the document without content as cmisObject from cmis-server with the given id. Converts it to {@link DocumentFile}.
     * @param id The id of the wanted document.
     * @return Return the document without content as {@link DocumentFile}.
     */
    @Override
    public DocumentFile getDocument(String id) {
        logger.debug("Start loading document without content.");
        DocumentFile document = new DocumentFile();
        logger.debug("Get cmis session.");
        Session session = documentConnector.getSession();
        ObjectId objectId = new ObjectIdImpl(id);
        logger.debug("Loading cmisObject from cmisSession.");
        CmisObject cmisObject = session.getObject(objectId);
        logger.debug("Converting cmisObject to DocumentFile.");
        Document rawDocument = getCmisObjectAsDocument(cmisObject);
        if(rawDocument == null) {
            logger.error("Loading cmisObject failed. Creating an empty document.");
        } else {
            document = loadMetadata(document, rawDocument);
        }

        logger.debug("End loading document without content.");
        return document;
    }

    /**
     * Loads the document with content as cmisObject from cmis-server with the given id. Converts it to {@link DocumentFile}.
     * @param id The id of the wanted document.
     * @return Return the document without content as {@link DocumentFile}.
     */
    @Override
    public DocumentFile getDocumentContent(String id) {
        logger.debug("Start loading document with content.");
        DocumentFile document = new DocumentFile();
        logger.debug("Get cmis session.");
        Session session = documentConnector.getSession();
        ObjectId objectId = new ObjectIdImpl(id);
        logger.debug("Loading cmisObject from cmisSession.");
        CmisObject cmisObject = session.getObject(objectId);
        logger.debug("Converting cmisObject to FullDocument.");
        Document rawDocument = getCmisObjectAsDocument(cmisObject);
        if(rawDocument == null) {
            logger.error("Loading cmisObject failed. Creating an empty document.");
        } else {
            document = loadMetadata(document, rawDocument);
            document = loadContent(document, rawDocument);
        }

        return document;
    }

    /**
     * Loads the metadata from rawDocument and sets the fields in return document.
     * @param document Document which will be returned
     * @param rawDocument CmisDocument with all the metadata.
     * @return Return the document with metadata.
     */
    private DocumentFile loadMetadata(DocumentFile document, Document rawDocument) {
        logger.debug("Start loading metadata for document.");
        if(rawDocument.getContentStreamMimeType() != null) {
            logger.debug("Setting mimetype.");
            document.setMimeType(rawDocument.getContentStreamMimeType());
        } else {
            logger.debug("No mimetype found, setting mimetype to text/plain.");
            document.setMimeType("text/plain");
        }
        logger.debug("Setting title.");
        document.setTitle(rawDocument.getName());
        logger.debug("Setting documentpath.");
        for (String next : rawDocument.getPaths()) {
            document.setPath(document.getPath() + next);
        }
        logger.debug("Setting id.");
        document.setId(rawDocument.getId());
        logger.debug("End loading metadata for document.");
        return document;
    }

    /**
     * Loads the content from rawDocument and setts the content in return document.
     * @param document Document which will be returned
     * @param rawDocument CmisDocument with the content.
     * @return Return the document with content.
     */
    private DocumentFile loadContent(DocumentFile document, Document rawDocument) {
        logger.debug("Start loading content for document.");
        try {
            document.setContent(getContentAsBase64(rawDocument.getContentStream()));
        } catch (NullPointerException e) {
            logger.error("No content available.");
            logger.debug("", e);
        }
        logger.debug("End loading content for document.");
        return document;
    }

    /**
     * Check if cmisObject is a valid cmisDocument and cast it.
     * @param cmisObject The potentially cmisDocument.
     * @return Return {@link Document} if casting was successfull. Null if not.
     */
    private Document getCmisObjectAsDocument(CmisObject cmisObject) {
        logger.debug("Start casting cmisObject to cmisDocument.");
        Document cmisDocument;
        logger.debug("Check if cmisObject is valid cmisDocument.");
        if(cmisObject.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
            cmisDocument = (Document) cmisObject;
        } else {
            cmisDocument = null;
        }
        logger.debug("End casting cmisObject to cmisDocument.");
        return cmisDocument;
    }

    /**
     * Reads the content as an bytestream and convete the bytes to an base64-string.
     *
     * @param stream The {@link ContentStream} from the CmisDocument.
     * @return Return the content of the document as an base64-string.
     */
    private String getContentAsBase64(ContentStream stream) {
        logger.debug("Start converting content to base64 string.");
        StringBuilder sb = new StringBuilder();
        InputStream reader = stream.getStream();

        logger.debug("Reads the content and safe it to an bytearray.");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[4 * 1024];
            while ((bytesRead = reader.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] bytes = baos.toByteArray();

            logger.debug("Convert the bytes to an base64 string.");
            sb.append(Base64.encodeBase64String(bytes));
        } catch (IOException e) {
            logger.error("", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        logger.debug("End converting content to base64 string.");
        return sb.toString();
    }

    public boolean writeFile(String base64Content, String path){
        return false;
    }

    public boolean writeFile(String base64Content, String path, String filename) {
        return false;
    }
}
