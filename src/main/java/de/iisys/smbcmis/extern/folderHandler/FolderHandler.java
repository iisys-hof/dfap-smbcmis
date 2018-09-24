

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


import de.iisys.smbcmis.extern.model.DocumentFolder;

/**
 * Created by Thomas Winkler on 19.09.17
 * This interface is used for all folder entities
 */
public interface FolderHandler {
    /**
     * @param id String with folder id
     *
     * @return Returns a DocumentFolder with all folders and files within
     */
    DocumentFolder getFolderTree(String id);

    /**
     * @return DocumentFolder with all folders
     */
    DocumentFolder getRootTree();

    /**
     * @return Folder structure with folders and files for a specific machine and tool (defined in a task)
     */
    DocumentFolder getFolderByMachineAndTool(String machine, String tool);
    DocumentFolder getFolderByTool(String tool);
}
