/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.lanes;

import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.LanePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.Lane;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.bpmn2;

public class LaneConverter {

    private final PropertyWriterFactory propertyWriterFactory;

    public LaneConverter(PropertyWriterFactory propertyWriterFactory) {
        this.propertyWriterFactory = propertyWriterFactory;
    }

    public Result<LanePropertyWriter> toElement(Node<View<? extends BPMNViewDefinition>, ?> node) {
        return NodeMatch.fromNode(BPMNViewDefinition.class, LanePropertyWriter.class)
                .when(Lane.class, n -> {
                    org.eclipse.bpmn2.Lane lane = bpmn2.createLane();
                    lane.setId(n.getUUID());

                    LanePropertyWriter p = propertyWriterFactory.of(lane);

                    Lane definition = n.getContent().getDefinition();
                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());
                    p.setMetaData(definition.getAdvancedData().getMetaDataAttributes());

                    p.setAbsoluteBounds(n);

                    return p;
                })
                .ignore(Object.class)
                .apply(node);
    }
}
