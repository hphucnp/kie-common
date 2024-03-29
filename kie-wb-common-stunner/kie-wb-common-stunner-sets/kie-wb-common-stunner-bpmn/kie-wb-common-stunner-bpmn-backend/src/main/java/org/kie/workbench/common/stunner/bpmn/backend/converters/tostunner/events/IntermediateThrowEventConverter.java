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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.events;

import java.util.List;

import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.ConditionalEventDefinition;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.EscalationEventDefinition;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Match;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.AbstractConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.NodeConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.EventDefinitionReader;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.EventPropertyReader;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.ThrowEventPropertyReader;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateCompensationEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateEscalationEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateLinkEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateMessageEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateSignalEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DataIOSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.compensation.ActivityRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.compensation.CompensationEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.escalation.EscalationEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.escalation.EscalationRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.link.LinkEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.link.LinkRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.MessageEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.MessageRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.signal.ScopedSignalEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.signal.SignalRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.signal.SignalScope;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Documentation;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.AdvancedData;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.marshaller.MarshallingRequest.Mode;

public class IntermediateThrowEventConverter extends AbstractConverter implements NodeConverter<IntermediateThrowEvent> {

    protected static final String NO_DEFINITION = "An intermediate throw event should contain exactly one definition";
    protected static final String MULTIPLE_DEFINITIONS = "Multiple definitions not supported for intermediate throw event";

    protected final TypedFactoryManager factoryManager;
    protected final PropertyReaderFactory propertyReaderFactory;

    public IntermediateThrowEventConverter(TypedFactoryManager factoryManager,
                                           PropertyReaderFactory propertyReaderFactory,
                                           Mode mode) {
        super(mode);
        this.factoryManager = factoryManager;
        this.propertyReaderFactory = propertyReaderFactory;
    }

    public Result<BpmnNode> convert(IntermediateThrowEvent event) {
        ThrowEventPropertyReader p = propertyReaderFactory.of(event);
        List<EventDefinition> eventDefinitions = p.getEventDefinitions();
        switch (eventDefinitions.size()) {
            case 0:
                throw new UnsupportedOperationException(NO_DEFINITION);
            case 1:
                return Match.of(EventDefinition.class, BpmnNode.class)
                        .when(SignalEventDefinition.class, e -> signalEvent(event))
                        .when(LinkEventDefinition.class, e -> linkEvent(event))
                        .when(MessageEventDefinition.class, e -> messageEvent(event, e))
                        .when(EscalationEventDefinition.class, e -> escalationEvent(event, e))
                        .when(CompensateEventDefinition.class, e -> compensationEvent(event, e))
                        .missing(ErrorEventDefinition.class)
                        .missing(ConditionalEventDefinition.class)
                        .mode(getMode())
                        .apply(eventDefinitions.get(0));
            default:
                throw new UnsupportedOperationException(MULTIPLE_DEFINITIONS);
        }
    }

    protected BpmnNode messageEvent(IntermediateThrowEvent event, MessageEventDefinition eventDefinition) {
        Node<View<IntermediateMessageEventThrowing>, Edge> node =
                factoryManager.newNode(event.getId(), IntermediateMessageEventThrowing.class);

        IntermediateMessageEventThrowing definition = node.getContent().getDefinition();
        EventPropertyReader p = propertyReaderFactory.of(event);

        node.getContent().setBounds(p.getBounds());

        definition.setGeneral(new BPMNGeneralSet(
                new Name(p.getName()),
                new Documentation(p.getDocumentation())
        ));
        definition.setAdvancedData(new AdvancedData(p.getMetaDataAttributes()));

        definition.setDimensionsSet(p.getCircleDimensionSet());
        definition.setFontSet(p.getFontSet());
        definition.setBackgroundSet(p.getBackgroundSet());

        definition.setDataIOSet(new DataIOSet(
                p.getAssignmentsInfo()
        ));

        definition.setExecutionSet(new MessageEventExecutionSet(
                new MessageRef(EventDefinitionReader.messageRefOf(eventDefinition),
                               EventDefinitionReader.messageRefStructureOf(eventDefinition))
        ));

        return BpmnNode.of(node, p);
    }

    protected BpmnNode signalEvent(IntermediateThrowEvent event) {

        Node<View<IntermediateSignalEventThrowing>, Edge> node =
                factoryManager.newNode(event.getId(), IntermediateSignalEventThrowing.class);

        IntermediateSignalEventThrowing definition = node.getContent().getDefinition();
        EventPropertyReader p = propertyReaderFactory.of(event);

        node.getContent().setBounds(p.getBounds());

        definition.setGeneral(new BPMNGeneralSet(
                new Name(p.getName()),
                new Documentation(p.getDocumentation())
        ));
        definition.setAdvancedData(new AdvancedData(p.getMetaDataAttributes()));

        definition.setDimensionsSet(p.getCircleDimensionSet());
        definition.setFontSet(p.getFontSet());
        definition.setBackgroundSet(p.getBackgroundSet());

        definition.setDataIOSet(new DataIOSet(
                p.getAssignmentsInfo()
        ));

        definition.setExecutionSet(new ScopedSignalEventExecutionSet(
                new SignalRef(p.getSignalRef()),
                new SignalScope(p.getSignalScope())
        ));



        return BpmnNode.of(node, p);
    }

    protected BpmnNode linkEvent(IntermediateThrowEvent event) {

        Node<View<IntermediateLinkEventThrowing>, Edge> node =
                factoryManager.newNode(event.getId(), IntermediateLinkEventThrowing.class);

        IntermediateLinkEventThrowing definition = node.getContent().getDefinition();
        EventPropertyReader p = propertyReaderFactory.of(event);

        node.getContent().setBounds(p.getBounds());

        definition.setGeneral(new BPMNGeneralSet(
                new Name(p.getName()),
                new Documentation(p.getDocumentation())
        ));
        definition.setAdvancedData(new AdvancedData(p.getMetaDataAttributes()));

        definition.setDimensionsSet(p.getCircleDimensionSet());
        definition.setFontSet(p.getFontSet());
        definition.setBackgroundSet(p.getBackgroundSet());

        definition.setDataIOSet(new DataIOSet(
                p.getAssignmentsInfo()
        ));

        definition.setExecutionSet(new LinkEventExecutionSet(
                new LinkRef(p.getLinkRef())
        ));

        return BpmnNode.of(node, p);
    }

    protected BpmnNode escalationEvent(IntermediateThrowEvent event, EscalationEventDefinition eventDefinition) {

        Node<View<IntermediateEscalationEventThrowing>, Edge> node =
                factoryManager.newNode(event.getId(),
                                       IntermediateEscalationEventThrowing.class);

        IntermediateEscalationEventThrowing definition = node.getContent().getDefinition();
        EventPropertyReader p = propertyReaderFactory.of(event);

        node.getContent().setBounds(p.getBounds());

        definition.setGeneral(new BPMNGeneralSet(
                new Name(p.getName()),
                new Documentation(p.getDocumentation())
        ));
        definition.setAdvancedData(new AdvancedData(p.getMetaDataAttributes()));

        definition.setDimensionsSet(p.getCircleDimensionSet());
        definition.setFontSet(p.getFontSet());
        definition.setBackgroundSet(p.getBackgroundSet());

        definition.setDataIOSet(new DataIOSet(
                p.getAssignmentsInfo()
        ));

        definition.setExecutionSet(new EscalationEventExecutionSet(
                new EscalationRef(EventDefinitionReader.escalationRefOf(eventDefinition))
        ));

        return BpmnNode.of(node, p);
    }

    protected BpmnNode compensationEvent(IntermediateThrowEvent event, CompensateEventDefinition eventDefinition) {

        Node<View<IntermediateCompensationEventThrowing>, Edge> node =
                factoryManager.newNode(event.getId(),
                                       IntermediateCompensationEventThrowing.class);

        IntermediateCompensationEventThrowing definition = node.getContent().getDefinition();
        EventPropertyReader p = propertyReaderFactory.of(event);

        node.getContent().setBounds(p.getBounds());

        definition.setGeneral(new BPMNGeneralSet(
                new Name(p.getName()),
                new Documentation(p.getDocumentation())
        ));
        definition.setAdvancedData(new AdvancedData(p.getMetaDataAttributes()));

        definition.setDimensionsSet(p.getCircleDimensionSet());
        definition.setFontSet(p.getFontSet());
        definition.setBackgroundSet(p.getBackgroundSet());

        definition.setDataIOSet(new DataIOSet(
                p.getAssignmentsInfo()
        ));

        definition.setExecutionSet(new CompensationEventExecutionSet(
                new ActivityRef(EventDefinitionReader.activityRefOf(eventDefinition))
        ));

        return BpmnNode.of(node, p);
    }
}