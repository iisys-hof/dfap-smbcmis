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

package de.iisys.smbcmis.extern.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Schmid on 25.11.16.
 *
 * POJO of a documentFolder. Can contain other {@link DocumentFolder} and {@link DocumentFile}.
 */
public class DocumentFolder implements DocumentEntry {

    private String id;
    private String path;
    private String title;
    @JsonProperty(value = "document")
    private List<DocumentFile> documentList = new ArrayList<>();
    @JsonProperty(value = "folder")
    private List<DocumentFolder> folderList = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    public List<DocumentFile> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(List<DocumentFile> documentList) {
        this.documentList = documentList;
    }

    public List<DocumentFolder> getFolderList() {
        return folderList;
    }

    public void setFolderList(List<DocumentFolder> folderList) {
        this.folderList = folderList;
    }

    @Override
    public String toString() {
        return "DocumentFolder{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", title='" + title + '\'' +
                ", documentList=" + documentList +
                ", folderList=" + folderList +
                '}';
    }
}
