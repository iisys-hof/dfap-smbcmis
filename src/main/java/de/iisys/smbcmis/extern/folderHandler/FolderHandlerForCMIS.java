
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

package de.iisys.smbcmis.extern.folderHandler;


import de.iisys.smbcmis.extern.config.Configuration;
import de.iisys.smbcmis.extern.DocumentConnector;
import de.iisys.smbcmis.extern.model.DocumentFile;
import de.iisys.smbcmis.extern.model.DocumentFolder;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.iisys.smbcmis.extern.strategy.SearchPattern;
import de.iisys.smbcmis.extern.strategy.SearchWithMachineAndOrder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Alexander Schmid <alexander.schmid@hof-university.de>
 * <p>
 * This class provdes methods for loading folders from the dms-server and converts it to pojos for the microservice.
 */
@SuppressWarnings("Duplicates")
@Named
@RequestScoped
public class FolderHandlerForCMIS implements FolderHandler {

    private static final Logger logger = LoggerFactory.getLogger(FolderHandlerForCMIS.class);

    @Inject
    private DocumentConnector documentConnector;

    /**
     * Load an cmis-object with the given id from cmis-session. Check if the cmis-object is a valid folder and converting it to {@link DocumentFolder}.
     *
     * @param id The id of the wanted folder.
     * @return Return the foldertree as a {@link DocumentFolder}.
     */
    @Override
    public DocumentFolder getFolderTree(String id) {
        logger.debug("Start of loading foldertree with id.");
        DocumentFolder folder = null;
        Session session = documentConnector.getSession();
        ObjectId objectId = new ObjectIdImpl(id);
        logger.debug("Load object from session with id.");
        CmisObject cmisObject = session.getObject(objectId);
        logger.debug("Converting from cmisObject to folder...");
        try {
            folder = convertFolder(cmisObject);
        } catch (Exception e) {
            //e.printStackTrace();
            logger.warn("");
            logger.debug("", e);
        }
        logger.debug("End of loading foldertree with id.");
        return folder;
    }

    /**
     * Load the cmis-rootfolder from cmis-session. Check if the cmis-rootfolder is a valid folder and converting it to {@link DocumentFolder}.
     *
     * @return Return the foldertree as a {@link DocumentFolder}.
     */
    @Override
    public DocumentFolder getRootTree() {
        logger.debug("Start of loading foldertree from root.");
        DocumentFolder folder = null;
        Session session = documentConnector.getSession();
        Folder rootFolder = session.getRootFolder();
        logger.debug("Converting from cmisFolder to folder...");
        try {
            folder = convertFolder(rootFolder);
        } catch (Exception e) {
            //e.printStackTrace();
            logger.warn("");
            logger.debug("", e);
        }
        logger.debug("End of loading foldertree from root.");
        System.out.println(folder);
        return folder;
    }


    @Override
    public DocumentFolder getFolderByMachineAndTool(String machine, String tool) {
        //TODO: Get Configuration, Is machine or tool a folder, where is the root
        DocumentFolder folder = getRootTree();
        DocumentFolder x = new DocumentFolder();
        getMachineAndTool(x, folder, new SearchWithMachineAndOrder(machine, tool));

        return x;
    }

    @Override
    public DocumentFolder getFolderByTool(String tool) {
        return null;
    }

    private void getMachineAndTool(DocumentFolder x, DocumentFolder folder, SearchPattern sp){

        for (DocumentFolder f : folder.getFolderList()) {

            if(Configuration.getSearchStrategy() == Configuration.SearchForMachineAndOrderStrategy.FOLDER ||
                    Configuration.getSearchStrategy() == Configuration.SearchForMachineAndOrderStrategy.FOLDER_AND_FILENAME) {
                if (sp.isFolderRequired(f.getPath() + "/" + f.getTitle())) {
                    x.getFolderList().add(f);
                    return;
                }
            }else {
                for(DocumentFile file : f.getDocumentList()){
                    System.out.println("TITLE: " + file.getTitle());
                    if(sp.isFileRequired(file.getTitle())){
                        System.out.println("REQUIRED");
                        x.getDocumentList().add(file);
                    }
                }
            }
            getMachineAndTool(x, f, sp);
        }
    }


    /**
     * Check if the CmisObject is a CmisFolder and convert it to {@link DocumentFolder}.
     * This method also add the complete tree with the given CmisFolder as root with recursive-call. If a folder has any documents, their will be added as {@link DocumentFile}.
     *
     * @param cmisObject The Cmisfolder
     * @return Return the converted {@link DocumentFolder}.
     */
    private DocumentFolder convertFolder(CmisObject cmisObject) {
        logger.debug("Start converting cmisObject to documentfolder.");
        DocumentFolder folder = new DocumentFolder();
        Folder cmisFolder = getCmisObjectAsFolder(cmisObject);
        if (cmisFolder == null) {
            logger.warn("Folder is not an valid cmisFolder. Return an empty documentfolder for this object.");
            folder.getFolderList().add(new DocumentFolder());
        } else {
            for (CmisObject next : cmisFolder.getChildren()) {
                if (next.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
                    logger.debug("Adding new folder to foldertree.");
                    folder.getFolderList().add(convertFolder(next));
                }
                if (next.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
                    logger.debug("Adding new document to foldertree.");
                    Document doc = (Document) next;
                    DocumentFile documentFile = new DocumentFile();
                    logger.debug("Setting the field for the document.");
                    documentFile.setTitle(doc.getName());
                    documentFile.setId(doc.getId());
                    documentFile.setMimeType(doc.getContentStreamMimeType());
                    documentFile.setPath(doc.getPaths().get(0));
                    folder.getDocumentList().add(documentFile);
                }
            }
            logger.debug("Setting the fields for the folder.");
            folder.setId(cmisFolder.getId());
            folder.setPath(cmisFolder.getPath());
            folder.setTitle(cmisFolder.getName());
        }
        logger.debug("End of converting cmisObject to documentfolder.");
        return folder;
    }

    /**
     * Check if cmisObject is a valid cmisFolder and cast it.
     *
     * @param cmisObject The potentially cmisFolder.
     * @return Return {@link Folder} if casting was successfull. Null if not.
     */
    private Folder getCmisObjectAsFolder(CmisObject cmisObject) {
        logger.debug("Start casting cmisObject to cmisFolder.");
        Folder cmisFolder;
        logger.debug("Check if cmisObject is valid cmisFolder.");
        if (cmisObject.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
            cmisFolder = (Folder) cmisObject;
        } else {
            cmisFolder = null;
        }
        logger.debug("End casting cmisObject to cmisFolder.");
        return cmisFolder;
    }


}
