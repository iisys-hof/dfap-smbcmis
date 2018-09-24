

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

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.share.File;
import de.iisys.smbcmis.extern.config.Configuration;

import de.iisys.smbcmis.extern.config.SMB;
import de.iisys.smbcmis.extern.model.DocumentFile;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import java.io.*;
import java.net.URLConnection;
import java.util.EnumSet;

@SuppressWarnings("Duplicates")
@RequestScoped
public class FileHandlerForSMB implements FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandlerForCMIS.class);


    private String separator;

    public FileHandlerForSMB() {
        if(Configuration.getSmbSystem().equals("WINDOWS"))
            this.separator = "\\";
        else
            this.separator = "/";
        System.out.println("SEP: " + separator);
    }

    @Override
    public DocumentFile getDocument(String id) { logger.debug("Start loading document without content.");
        if (id.startsWith("/"))
            id = id.substring(1, id.length());

        if(Configuration.getSmbSystem().equals("WINDOWS"))
            id = id.replace("/", "\\");

        logger.debug("Start loading File without content.");
        DocumentFile df = new DocumentFile();

        logger.debug("Getting File for Path-ID.");
        System.out.println("ID: " + id);

        File f = SMB.getInstance().getDiskshare().openFile(id, EnumSet.of(AccessMask.MAXIMUM_ALLOWED), EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, EnumSet.of(SMB2CreateOptions.FILE_WRITE_THROUGH));
        logger.debug("Set File Metadata.");
        df = loadMetadata(df, f, id);

        logger.debug("End loading File without content.");
        return df;
    }

    @Override
    public DocumentFile getDocumentContent(String id) {
        if (id.startsWith("/"))
            id = id.substring(1, id.length());

        if(Configuration.getSmbSystem().equals("WINDOWS"))
            id = id.replace("/", "\\");

        logger.debug("Start loading File with content.");
        DocumentFile df = new DocumentFile();

        StringBuilder sb = new StringBuilder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        logger.debug("Getting File for Path-ID.");

        File f = SMB.getInstance().getDiskshare().openFile(id, EnumSet.of(AccessMask.MAXIMUM_ALLOWED), EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, EnumSet.of(SMB2CreateOptions.FILE_WRITE_THROUGH));

        logger.debug("Set File Metadata.");


        logger.debug("Load content of File.");
        try (InputStream inputStream = new BufferedInputStream(f.getInputStream())) {

            //byte[] buffer = new byte[4096];
            byte[] buffer = new byte[1];

            while (inputStream.read(buffer) != -1) {
                baos.write(buffer);
            }

            byte[] bytes = baos.toByteArray();
            sb.append(Base64.encodeBase64String(bytes));
            df.setContent(sb.toString());


        } catch (IOException ex) {
            ex.printStackTrace();
        }
        df = loadMetadata(df, f, id);
        return df;
    }

    @Override
    public boolean writeFile(String base64Content, String path) {
        return false;
    }

    private DocumentFile loadMetadata(DocumentFile df, File f, String id) {
        logger.debug("Start setting Metadata.");
        if(Configuration.getSmbSystem().equals("WINDOWS"))
        {
            id = id.replace("\\", "/");
            df.setMimeType(URLConnection.guessContentTypeFromName(id.substring(id.lastIndexOf("/")+1, id.length())));
            df.setTitle(id.substring(id.lastIndexOf("/")+1, id.length()));
            df.setPath("/" + id);
            df.setId(id);
        }
        else {
            try {
                String name = f.getFileInformation().getNameInformation();
                System.out.println("NAME: " + name);
                df.setMimeType(URLConnection.guessContentTypeFromName(name.substring(1)));
                df.setTitle(name.substring(name.lastIndexOf("\\")+1, name.length()));
                df.setPath("/" + id);
                df.setId(id);
            } catch (TransportException e) {
                e.printStackTrace();
            }
        }


        logger.debug("End setting Metadata.");
        f.close();
        return df;
    }


    public boolean writeFile(String base64Content, String path, String filename) {


        logger.debug("Start writing File.");
        String filePath = path + "/" + filename;
        String paths[] = path.split("/");
        String devider = "/";

        if (Configuration.getSmbSystem().equals("WINDOWS")) {
            filePath = filePath.replace("/", "\\");
            devider = "\\";
        }

        String pp = "";
        for(String p : paths) {
            pp = pp + p + devider;
            if (!SMB.getInstance().getDiskshare().folderExists(pp)){
                SMB.getInstance().getDiskshare().openDirectory(pp, EnumSet.of(AccessMask.MAXIMUM_ALLOWED), EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_CREATE, EnumSet.of(SMB2CreateOptions.FILE_WRITE_THROUGH)).close();
            }
        }

        File f = SMB.getInstance().getDiskshare().openFile(filePath, EnumSet.of(AccessMask.MAXIMUM_ALLOWED), EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, EnumSet.of(SMB2CreateOptions.FILE_WRITE_THROUGH));

        byte[] bytes = Base64.decodeBase64(base64Content);

        try {
            BufferedOutputStream stream = new BufferedOutputStream(f.getOutputStream());
            stream.write(bytes);
            stream.close();
            f.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        logger.debug("End writing File.");

        return true;
    }

}
