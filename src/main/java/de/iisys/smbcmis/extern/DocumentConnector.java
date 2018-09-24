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


import de.iisys.smbcmis.extern.config.Configuration;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexander Schmid on 25.11.16.
 */
@SuppressWarnings("Duplicates")
@Named
@RequestScoped
public class DocumentConnector {


    private Session session;


    /**
     * Create a session with the cmis-dms-server with the given {@link Configuration}.
     */
    private void createSession(){

        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<>();

        parameter.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
        //from de.iisys.smbcmis.extern.config
        parameter.put(SessionParameter.USER, Configuration.getCmisUser());
        parameter.put(SessionParameter.PASSWORD, Configuration.getCmisPassword());
        parameter.put(SessionParameter.BROWSER_URL, Configuration.getCmisHost());

        parameter.put(SessionParameter.COOKIES, "true");
        //parameter.put(SessionParameter.AUTH_SOAP_USERNAMETOKEN, "true");
        //parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
        //parameter.put(SessionParameter.AUTH_OAUTH_BEARER, "false");
        parameter.put(SessionParameter.COMPRESSION, "true");
        parameter.put(SessionParameter.CLIENT_COMPRESSION, "false");
        parameter.put(SessionParameter.REPOSITORY_ID, "default");

        //create session
        session = factory.createSession(parameter);
    }

    /**
     * The session object is a singleton.
     * @return Return the singleton session. If session is null, a new one is created.
     */
    public Session getSession() {
        if(session == null) {
            createSession();
        }
        return session;
    }
}
