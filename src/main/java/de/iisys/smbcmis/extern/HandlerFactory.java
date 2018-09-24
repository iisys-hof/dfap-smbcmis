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

package de.iisys.smbcmis.extern;

import de.iisys.smbcmis.extern.fileHandler.FileHandler;
import de.iisys.smbcmis.extern.fileHandler.FileHandlerForCMIS;
import de.iisys.smbcmis.extern.fileHandler.FileHandlerForSMB;
import de.iisys.smbcmis.extern.fileHandler.FileHandlerType;
import de.iisys.smbcmis.extern.folderHandler.FolderHandler;
import de.iisys.smbcmis.extern.folderHandler.FolderHandlerForCMIS;
import de.iisys.smbcmis.extern.folderHandler.FolderHandlerForSMB;
import de.iisys.smbcmis.extern.folderHandler.FolderHandlerType;

import javax.inject.Inject;

public class HandlerFactory {

    @Inject
    private FileHandlerForCMIS cmisFileHandler;
    @Inject
    private FileHandlerForSMB smbFileHandler;

    @Inject
    private FolderHandlerForCMIS cmisFolderHandler;
    @Inject
    private FolderHandlerForSMB smbFolderHandler;

    public FileHandler fileHandlerForType(FileHandlerType type) {
        switch(type) {
            case CMIS: return cmisFileHandler;
            case SMB: return smbFileHandler;
            default:
                throw new IllegalArgumentException();
        }
    }

    public FolderHandler folderHandlerForType(FolderHandlerType type) {
        switch(type) {
            case CMIS: return cmisFolderHandler;
            case SMB: return smbFolderHandler;
            default:
                throw new IllegalArgumentException();
        }
    }
}
