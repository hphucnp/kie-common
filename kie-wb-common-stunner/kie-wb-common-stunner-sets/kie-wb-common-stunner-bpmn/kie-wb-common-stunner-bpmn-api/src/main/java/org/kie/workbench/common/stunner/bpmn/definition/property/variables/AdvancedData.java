/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.definition.property.variables;

import java.util.Objects;

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.MetaDataAttributes;
import org.kie.workbench.common.stunner.bpmn.forms.model.MetaDataEditorFieldType;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;

@Portable
@Bindable
@FormDefinition(startElement = "metaDataAttributes")
public class AdvancedData implements BaseAdvancedData {

    @Property
    @FormField(
            type = MetaDataEditorFieldType.class
    )
    @Valid
    private MetaDataAttributes metaDataAttributes;

    public AdvancedData() {
        this(new MetaDataAttributes());
    }

    public AdvancedData(final @MapsTo("metaDataAttributes") MetaDataAttributes metaDataAttributes) {
        this.metaDataAttributes = metaDataAttributes;
    }

    public AdvancedData(final String metaDataAttributes) {
        this.metaDataAttributes = new MetaDataAttributes(metaDataAttributes);
    }

    @Override
    public MetaDataAttributes getMetaDataAttributes() {
        return metaDataAttributes;
    }

    public void setMetaDataAttributes(MetaDataAttributes metadataAttributes) {
        this.metaDataAttributes = metadataAttributes;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(metaDataAttributes);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AdvancedData) {
            AdvancedData other = (AdvancedData) o;
            return Objects.equals(metaDataAttributes, other.metaDataAttributes);
        }
        return false;
    }
}