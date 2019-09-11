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

package com.haulmont.cuba.gui.model.impl;

import com.google.common.base.Splitter;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.Range;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.ViewLoader;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component(ScreenViewXmlLoader.NAME)
public class ScreenViewXmlLoader {

    public static final String NAME = "cuba_ScreenViewXmlLoader";

    @Inject
    protected Metadata metadata;
    @Inject
    protected ViewRepository viewRepository;
    @Inject
    protected ViewLoader viewLoader;

    public View loadView(Element viewElem, Class<Entity> entityClass) {
        ViewLoader.ViewInfo viewInfo = viewLoader.getViewInfo(viewElem, metadata.getClassNN(entityClass));
        View.ViewParams viewParams = viewLoader.getViewParams(viewInfo, a -> viewRepository.getView(viewInfo.getMetaClass(), a));
        View view = new View(viewParams);
        viewLoader.loadViewProperties(viewElem, view, viewInfo.isSystemProperties(), (metaClass, viewName) -> viewRepository.getView(metaClass, viewName));
        return view;
    }
}
