/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.testsupport;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.sys.AppContext;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;

public class TestDbPropertiesUtil {
    public static void initDbProperties(TestContainer testContainer) {
        String dataSourceProvider = AppContext.getProperty("cuba.dataSourceProvider");
        if (isDbParamsInitialized(testContainer))
        if (dataSourceProvider == null || "jndi".equals(dataSourceProvider)) {
            File contextXmlFile = new File("modules/core/web/META-INF/context.xml");
            if (!contextXmlFile.exists()) {
                contextXmlFile = new File("web/META-INF/context.xml");
            }
            if (!contextXmlFile.exists()) {
                throw new RuntimeException("Cannot find 'context.xml' file to read database connection properties. " +
                        "You can set them explicitly in this method.");
            }
            Document contextXmlDoc = Dom4j.readDocument(contextXmlFile);
            Element resourceElem = contextXmlDoc.getRootElement().element("Resource");

            testContainer.setDbDriver(resourceElem.attributeValue("driverClassName"));
            testContainer.setDbUrl(resourceElem.attributeValue("url"));
            testContainer.setDbUser(resourceElem.attributeValue("username"));
            testContainer.setDbPassword(resourceElem.attributeValue("password"));
        }
    }

    public static boolean isDbParamsInitialized(TestContainer testContainer) {

    }
}
