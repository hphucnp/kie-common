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

package org.kie.workbench.common.stunner.bpmn.definition;

import java.util.Set;

import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.definition.property.background.BackgroundSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.font.FontSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.TaskGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.TaskType;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.AdvancedData;
import org.kie.workbench.common.stunner.core.backend.definition.adapter.ReflectionAdapterUtils;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Labels;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BaseTaskTest {

    @Mock
    private AdvancedData advancedData;

    @Test
    public void testBaseTaskCanBeContainedByALane() throws Exception {

        final FakeBaseTask baseTask = new FakeBaseTask();
        final Set<String> labels = ReflectionAdapterUtils.getAnnotatedFieldValue(baseTask, Labels.class);

        assertNotNull(labels);
        assertTrue(labels.contains("lane_child"));
    }

    @Test
    public void testGetAdvancedData() {
        final FakeBaseTask baseTask = new FakeBaseTask(null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       advancedData);
        AdvancedData result = baseTask.getAdvancedData();
        assertEquals(advancedData, result);
    }

    @Test
    public void testSetAdvancedData() {
        final FakeBaseTask baseTask = new FakeBaseTask();
        assertNull(baseTask.advancedData);
        baseTask.setAdvancedData(advancedData);
        assertEquals(advancedData, baseTask.advancedData);
    }

    private class FakeBaseTask extends BaseTask {

        FakeBaseTask() {
            super(null,
                  null,
                  null,

                  null,

                  null,
                  null,
                  null);
        }

        FakeBaseTask(final TaskGeneralSet general,
                     final BackgroundSet backgroundSet,
                     final FontSet fontSet,
                     final RectangleDimensionsSet dimensionsSet,
                     final SimulationSet simulationSet,
                     final TaskType taskType,
                     final AdvancedData advancedData) {
            super(general,
                  backgroundSet,
                  fontSet,
                  dimensionsSet,
                  simulationSet,
                  taskType,
                  advancedData);
        }
    }
}
