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
 * Created: 21.09.17 - 12:00
 *
 * @author Thomas Winkler <thomas.winkler@iisys.de>
 */
package de.iisys.smbcmis.extern.strategy;

public interface SearchPattern {

    Boolean isFileRequired(String name);
    Boolean isFolderRequired(String name);
}
