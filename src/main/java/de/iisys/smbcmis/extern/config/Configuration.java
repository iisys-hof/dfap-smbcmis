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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;


public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static String SMB_CONFIG_PATH = "smb.xml";
    private static String CMIS_CONFIG_PATH = "cmis.xml";


    private static String cmisUser;
    private static String cmisHost;
    private static String cmisPassword;

    private static String smbUser;
    private static String smbPassword;
    private static String smbHost;
    private static String smbSharename;
    private static String smbSystem;
    private static String smbDomain;

    private static String[] ignoredResources;
    private static SearchForMachineAndOrderStrategy searchStrategy;
    private static String[] searchPattern;

    private static boolean loaded = false;


    static {
        System.out.println("STATIC");
        if (!loaded) {
            loadConfig();
        }
    }

	
    public static void loadConfig() {
		//Todo: needs to be outsourced to config file
        smbUser = "your-user";
        smbPassword = "your-password";
        smbHost = "your-host";
        smbSharename = "Share";
        smbSystem = "WINDOWS";
        smbDomain = "your-domain";

        ignoredResources =  new String[2];
        ignoredResources[0] = ".DS_Store";
        ignoredResources[1] = "._";

        searchStrategy = SearchForMachineAndOrderStrategy.FILENAME;
        searchPattern = new String[5];
        searchPattern[3] = ".*";
        searchPattern[4] = ".*\\.pdf";

        loaded = true;

        if (Configuration.class.getClassLoader().getResource(SMB_CONFIG_PATH) != null) {
            loadConfig(Objects.requireNonNull(Configuration.class.getClassLoader().getResource(SMB_CONFIG_PATH)));
        }
        //If cmis.xml is present use this one.
        // There should only be one of them
    }

    public static void loadConfig(URL url) {

        File file = null;

        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            logger.debug("", e);
        }


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;

        try {
            logger.info("Parse configfile.");
            builder = factory.newDocumentBuilder();
            assert file != null;
            doc = builder.parse(file);

            doc.getDocumentElement().normalize();

            logger.info("Read document nodes.");

            smbUser = doc.getElementsByTagName("user").item(0).getTextContent();
            smbPassword = doc.getElementsByTagName("password").item(0).getTextContent();
            smbHost = doc.getElementsByTagName("host").item(0).getTextContent();
            smbSharename = doc.getElementsByTagName("sharename").item(0).getTextContent();
            smbDomain = doc.getElementsByTagName("domain").item(0).getTextContent();
            smbSystem = doc.getElementsByTagName("system").item(0).getTextContent();
            ignoredResources = doc.getElementsByTagName("ignored-resources").item(0).getTextContent().split(" ");

            searchStrategy = SearchForMachineAndOrderStrategy.valueOf(doc.getElementsByTagName("search-strategy").item(0).getTextContent());
            String searchString = doc.getElementsByTagName("search-pattern").item(0).getTextContent();
            searchPattern = searchString.split("\n");
            searchPattern[1] = searchPattern[1].replaceAll("\\s+","");
            searchPattern[2] = searchPattern[2].replaceAll("\\s+","");
            searchPattern[3] = searchPattern[3].replaceAll("\\s+","");
            searchPattern[4] = searchPattern[4].replaceAll("\\s+","");

        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.error("Configuration-Syntax is wrong. See config.xml.default.");
            logger.error("Cannot read configfile.");
            logger.debug("", e);
        }
    }

    public static String getCmisUser() {
        return cmisUser;
    }

    public static void setCmisUser(String cmisUser) {
        Configuration.cmisUser = cmisUser;
    }

    public static String getCmisHost() {
        return cmisHost;
    }

    public static void setCmisHost(String cmisHost) {
        Configuration.cmisHost = cmisHost;
    }

    public static String getCmisPassword() {
        return cmisPassword;
    }

    public static void setCmisPassword(String cmisPassword) {
        Configuration.cmisPassword = cmisPassword;
    }

    public static String getSmbUser() {
        return smbUser;
    }

    public static void setSmbUser(String smbUser) {
        Configuration.smbUser = smbUser;
    }

    public static String getSmbPassword() {
        return smbPassword;
    }

    public static void setSmbPassword(String smbPassword) {
        Configuration.smbPassword = smbPassword;
    }

    public static String getSmbHost() {
        return smbHost;
    }

    public static void setSmbHost(String smbHost) {
        Configuration.smbHost = smbHost;
    }

    public static String getSmbSharename() {
        return smbSharename;
    }

    public static void setSmbSharename(String smbSharename) {
        Configuration.smbSharename = smbSharename;
    }

    public static String getSmbSystem() {
        return smbSystem;
    }

    public static void setSmbSystem(String smbSystem) {
        Configuration.smbSystem = smbSystem;
    }

    public static String getSmbDomain() {
        return smbDomain;
    }

    public static void setSmbDomain(String smbDomain) {
        Configuration.smbDomain = smbDomain;
    }

    public static String[] getIgnoredResources() {
        return ignoredResources;
    }

    public static void setIgnoredResources(String[] ignoredResources) {
        Configuration.ignoredResources = ignoredResources;
    }

    public static SearchForMachineAndOrderStrategy getSearchStrategy() {
        return searchStrategy;
    }

    public static void setSearchStrategy(SearchForMachineAndOrderStrategy searchStrategy) {
        Configuration.searchStrategy = searchStrategy;
    }

    public static String[] getSearchPattern() {
        return searchPattern;
    }

    public static void setSearchPattern(String[] searchPattern) {
        Configuration.searchPattern = searchPattern;
    }

    public enum SearchForMachineAndOrderStrategy {FOLDER, FOLDER_AND_FILENAME, FILENAME}
}
