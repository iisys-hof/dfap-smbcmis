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

/**
 * Project: duramentum-document
 * Package: de.iisys.duramentum.document.handler.de.iisys.smbcmis.extern.strategy
 * Created: 21.09.17 - 12:10
 *
 * @author Thomas Winkler <thomas.winkler@iisys.de>
 */
package de.iisys.smbcmis.extern.strategy;

import javax.validation.constraints.NotNull;

import static de.iisys.smbcmis.extern.config.Configuration.getIgnoredResources;

public class SearchWithIgnoreList implements SearchPattern {

    @Override
    public Boolean isFileRequired(String name) {
        return !isFileInIgnoreList(name);
    }

    @Override
    public Boolean isFolderRequired(String name) {
        return true;
    }

    /**
     * Files, which are specified in the FielIgnoredList won't be fetched. E.g. macOS metafiles
     *
     * @param name The String which will be searched for
     *
     * @return True if the file is on the ignore list. False if not.
     */
    @NotNull
    private Boolean isFileInIgnoreList(String name) {
        String[] iR = getIgnoredResources();

        for (String x : iR) {
            if (name.startsWith(x) | name.equals(x))
                return true;
        }
        return false;
    }
}
