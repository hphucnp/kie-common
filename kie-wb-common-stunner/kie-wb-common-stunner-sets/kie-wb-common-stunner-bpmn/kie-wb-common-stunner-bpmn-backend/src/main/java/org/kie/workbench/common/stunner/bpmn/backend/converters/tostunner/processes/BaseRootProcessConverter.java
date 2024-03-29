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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.processes;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.bpmn2.Process;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.ResultComposer;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.BaseConverterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.DefinitionsPropertyReader;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.ProcessPropertyReader;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagram;
import org.kie.workbench.common.stunner.bpmn.definition.property.cm.CaseFileVariables;
import org.kie.workbench.common.stunner.bpmn.definition.property.cm.CaseIdPrefix;
import org.kie.workbench.common.stunner.bpmn.definition.property.cm.CaseManagementSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.cm.CaseRoles;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.BaseDiagramSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.BaseProcessData;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.BaseRootProcessAdvancedData;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

/**
 * Convert the root Process with all its children to a BPMNDiagram
 */
public abstract class BaseRootProcessConverter<D extends BPMNDiagram<S, P, F>,
        S extends BaseDiagramSet, P extends BaseProcessData, F extends BaseRootProcessAdvancedData> {

    final ProcessConverterDelegate delegate;

    public BaseRootProcessConverter(TypedFactoryManager typedFactoryManager,
                                    PropertyReaderFactory propertyReaderFactory,
                                    DefinitionResolver definitionResolver,
                                    BaseConverterFactory factory) {
        this.delegate = new ProcessConverterDelegate(typedFactoryManager,
                                                     propertyReaderFactory,
                                                     definitionResolver,
                                                     factory);
    }

    public Result<BpmnNode> convertProcess() {
        Process process = delegate.definitionResolver.getProcess();
        String definitionsId = delegate.definitionResolver.getDefinitions().getId();
        BpmnNode processRoot = convertProcessNode(definitionsId, process);

        Result<Map<String, BpmnNode>> nodesResult = delegate.convertChildNodes(processRoot,
                                                                               process.getFlowElements(),
                                                                               process.getLaneSets());
        Map<String, BpmnNode> nodes = nodesResult.value();

        Result<Boolean> edgesResult = delegate.convertEdges(processRoot,
                                                            Stream.concat(process.getFlowElements().stream(),
                                                                          process.getArtifacts().stream()).collect(Collectors.toList()),
                                                            nodes);

        Result<BpmnNode> postConvertResult = delegate.postConvert(processRoot);

        return ResultComposer.compose(processRoot, nodesResult, edgesResult, postConvertResult);
    }

    private BpmnNode convertProcessNode(String id, Process process) {
        Node<View<D>, Edge> diagramNode = createNode(id);
        D definition = diagramNode.getContent().getDefinition();

        DefinitionsPropertyReader d = delegate.propertyReaderFactory.of(delegate.definitionResolver.getDefinitions());
        ProcessPropertyReader p = delegate.propertyReaderFactory.of(process);

        definition.setDiagramSet(createDiagramSet(process, p, d));

        definition.setCaseManagementSet(new CaseManagementSet(new CaseIdPrefix(p.getCaseIdPrefix()),
                                                              new CaseRoles(p.getCaseRoles()),
                                                              new CaseFileVariables(p.getCaseFileVariables())
        ));

        definition.setProcessData(createProcessData(p.getProcessVariables()));
        definition.setAdvancedData(createAdvancedData(p.getGlobalVariables(), p.getMetaDataAttributes()));

        diagramNode.getContent().setBounds(p.getBounds());

        definition.setFontSet(p.getFontSet());
        definition.setBackgroundSet(p.getBackgroundSet());

        return BpmnNode.of(diagramNode, p);
    }

    protected abstract Node<View<D>, Edge> createNode(String id);

    protected abstract S createDiagramSet(Process process, ProcessPropertyReader p, DefinitionsPropertyReader d);

    protected abstract P createProcessData(String processVariables);

    protected abstract F createAdvancedData(String globalVariables, String metaDataAttributes);
}