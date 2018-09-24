

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

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.share.DiskShare;
import de.iisys.smbcmis.extern.config.Configuration;
import de.iisys.smbcmis.extern.config.SMB;

import de.iisys.smbcmis.extern.model.DocumentFile;
import de.iisys.smbcmis.extern.model.DocumentFolder;
import de.iisys.smbcmis.extern.strategy.SearchPattern;
import de.iisys.smbcmis.extern.strategy.SearchWithIgnoreList;
import de.iisys.smbcmis.extern.strategy.SearchWithMachineAndOrder;
import de.iisys.smbcmis.extern.strategy.SearchWithTool;

import java.io.IOException;
import java.net.URLConnection;

/**
 * Specific FolderHandler for de.iisys.smbcmis.extern.config.SMB shares. Uses de.iisys.smbcmis.extern.config.SMB service which can be configured with de.iisys.smbcmis.extern.config.xml
 */
@SuppressWarnings("Duplicates")
public class FolderHandlerForSMB implements FolderHandler {

    private String separator;

    public FolderHandlerForSMB() {
        if(Configuration.getSmbSystem().equals("WINDOWS"))
            this.separator = "\\";
        else
            this.separator = "/";
        System.out.println("Seperator: " + separator);
    }
    /**
     * Returns a folder (and files) of an de.iisys.smbcmis.extern.config.SMB share with a specific path or ID
     *
     * @param id String with folder ID. ID can start with "/" but it doesn't need to. The "/" will be removed. ID is the absolut path, beginning after de.iisys.smbcmis.extern.config.SMB share
     *
     * @return All Folders as {@link DocumentFolder}
     */
    @Override
    public DocumentFolder getFolderTree(String id) {

        if (id.startsWith("/"))
            id = id.substring(1, id.length());

        if(Configuration.getSmbSystem().equals("WINDOWS"))
            id = id.replace("/", "\\");

        DocumentFolder folder = getFilesRecursive(SMB.getInstance().getDiskshare(), id, new SearchWithIgnoreList());
        return folder;
    }

    /**
     * Returns all folders (and files) of an de.iisys.smbcmis.extern.config.SMB share
     *
     * @return Returns a {@link DocumentFolder} with all folders within
     */
    @Override
    public DocumentFolder getRootTree() {
        DiskShare share = SMB.getInstance().getDiskshare();
        DocumentFolder folder = getFilesRecursive(share, "", new SearchWithIgnoreList());
        try {
            share.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folder;
    }

    /**
     * Returns a {@link DocumentFolder} stucture for a given Machine ID and Tool ID or String which is found in the de.iisys.smbcmis.extern.config.SMB share
     *
     * @param machine Machine ID or String (Regex)
     * @param tool    Tool ID or String (Regex)
     *
     * @return Returns a {@link DocumentFolder} stucture
     */
    @Override
    public DocumentFolder getFolderByMachineAndTool(String machine, String tool) {

        //TODO: Get Configuration, Is machine or tool a folder, where is the root
        DocumentFolder folder = getRootTree();
        DocumentFolder x = new DocumentFolder();
        getMachineAndTool(x, folder, new SearchWithMachineAndOrder(machine, tool));

        return x;
    }

    public DocumentFolder getFolderByTool(String tool) {

        //TODO: Get Configuration, Is machine or tool a folder, where is the root
        DocumentFolder folder = getRootTree();
        DocumentFolder x = new DocumentFolder();
        getMachineAndTool(x, folder, new SearchWithTool(tool));

        return x;
    }

    private void getMachineAndTool(DocumentFolder x, DocumentFolder folder, SearchPattern sp){
        for (DocumentFolder f : folder.getFolderList()) {

            if(Configuration.getSearchStrategy() == Configuration.SearchForMachineAndOrderStrategy.FOLDER ||
                    Configuration.getSearchStrategy() == Configuration.SearchForMachineAndOrderStrategy.FOLDER_AND_FILENAME) {
                if (sp.isFolderRequired(f.getPath() + separator + f.getTitle())) {
                    x.getFolderList().add(f);
                    return;
                }
            }else {
                for(DocumentFile file : f.getDocumentList()){
                    if(sp.isFileRequired(file.getTitle())){
                        x.getDocumentList().add(file);
                    }
                }
            }
            getMachineAndTool(x, f, sp);
        }
    }



    /**
     * Recursive method to go through a smb share and find folders and files within
     *
     * @param share {@link DiskShare} with the smb resource
     * @param path  String with the path to search within
     *
     * @return {@link DocumentFolder} with all sub folders and files (without content) within
     */
    private DocumentFolder getFilesRecursive(DiskShare share, String path, SearchPattern sp) {
        DocumentFolder folder = new DocumentFolder();
        for (FileIdBothDirectoryInformation f : share.list(path, "*")) {
            try {
                if (!(f.getFileName().equals(".") | f.getFileName().equals(".."))) {
                    if (path.length() == 0) {
                        if (share.getFileInformation(f.getFileName()).getStandardInformation().isDirectory()) { // If it is a directory, and root
                            folder.getFolderList().add(getFilesRecursive(share, f.getFileName(), sp));
                        } else if (sp.isFileRequired(f.getFileName())) { // If it is a file and in root
                            DocumentFile documentFile = new DocumentFile();
                            setFileMetadata(documentFile, path, f.getFileName());
                            folder.getDocumentList().add(documentFile);
                        }
                    } else {
                        if (share.getFileInformation(path + separator + f.getFileName()).getStandardInformation().isDirectory()) { // If it is a directory and not root
                            folder.getFolderList().add(getFilesRecursive(share, path + separator + f.getFileName(), sp));
                        } else if (sp.isFileRequired(f.getFileName())) { // If it it a file and not in root
                            DocumentFile documentFile = new DocumentFile();
                            setFileMetadata(documentFile, path, f.getFileName());
                            folder.getDocumentList().add(documentFile);
                        }
                    }
                }
            }catch (Exception e) {
                System.err.println("Error Reading files or folders");
                e.printStackTrace();
            }
        }
        setFolderMetaData(folder, path);
        return folder;
    }


    /**
     * Quite simple method to set the meta data of a folder
     *
     * @param entry {@link DocumentFolder} to add meta data
     * @param path  The path (or id in this case) of the folder
     */
    private void setFolderMetaData(DocumentFolder entry, String path) {
        path = path.replace('\\', '/');
        String p = "/" + path;


        // ID
        if(path.length() < 1)
            entry.setId("root");
        else
            entry.setId(path);

        // Path
        if (p.substring(0, p.lastIndexOf("/")).length() > 1)
            entry.setPath(p.substring(0, p.lastIndexOf("/")));
        else {
            entry.setPath("/");
        }
        // Title
        if (p.substring(p.lastIndexOf("/"), p.length()).length() > 1)
            entry.setTitle(p.substring(p.lastIndexOf("/") + 1, p.length()));
        else
            entry.setTitle("");
    }

    /**
     * This method stets the meta data of the file. It uses {@link URLConnection}.guessContentTypeFromName() to set the MIME type
     *
     * @param documentFile {@link DocumentFile} to add meta data
     * @param path         The path (or id in this case) of the file
     * @param name         The title of the file
     */
    private void setFileMetadata(DocumentFile documentFile, String path, String name) {
        path = path.replace('\\', '/');
        documentFile.setTitle(name);
        if(path.length()< 1)
            documentFile.setId(name);
        else
            documentFile.setId(path + "/" + name);
        documentFile.setMimeType(URLConnection.guessContentTypeFromName(name));
        String p = "/" + path;
        documentFile.setPath(p);
    }
}
