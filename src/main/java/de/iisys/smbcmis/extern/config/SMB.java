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

package de.iisys.smbcmis.extern.config;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SMB {
    private static SMB instance;
    private SMBClient client;

    public SMB() {
        SmbConfig cfg = SmbConfig.builder().build();

        client = new SMBClient(cfg);
    }

    public static SMB getInstance() {
        if (SMB.instance == null) {
            SMB.instance = new SMB();
        }
        return SMB.instance;
    }

    public DiskShare getDiskshare() {
        Connection connection = null;
        try {
            connection = client.connect(Configuration.getSmbHost());
        } catch (IOException e) {
            e.printStackTrace();
        }

        AuthenticationContext ac = new AuthenticationContext(Configuration.getSmbUser(),Configuration.getSmbPassword().toCharArray(), Configuration.getSmbDomain());
        Session session = connection.authenticate(ac);

        // Connect to Share
        DiskShare share = (DiskShare) session.connectShare(Configuration.getSmbSharename());


        return share;
    }
}
