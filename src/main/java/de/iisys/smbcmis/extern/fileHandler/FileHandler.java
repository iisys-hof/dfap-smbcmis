

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

import de.iisys.smbcmis.extern.model.DocumentFile;

/**
 * This interface is used for all file entities
 */
public interface FileHandler {
    /**
     * This method returns a DocumentFile for a specific id. But only the meta data.
     * @param id ID of DocumentFile
     * @return DocumentFile without content
     */
    DocumentFile getDocument(String id);

    /**
     * Return a Document and its content not only the meta data by a given id.
     * @param id ID of DocumentFile.
     * @return DocumentFile with content
     */
    DocumentFile getDocumentContent(String id);

    boolean writeFile(String base64Content, String path);
    boolean writeFile(String base64Content, String path, String filename);
}
