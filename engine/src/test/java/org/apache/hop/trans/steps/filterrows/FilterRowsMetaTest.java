/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.apache.hop.trans.steps.filterrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.hop.core.Condition;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.steps.dummytrans.DummyTransMeta;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;
import org.apache.hop.trans.steps.loadsave.validator.ConditionLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class FilterRowsMetaTest {
  LoadSaveTester loadSaveTester;
  Class<FilterRowsMeta> testMetaClass = FilterRowsMeta.class;
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    HopEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "condition", "send_true_to", "send_false_to" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "condition", new ConditionLoadSaveValidator() );
    attrValidatorMap.put( "trueStepName", new StringLoadSaveValidator() );
    attrValidatorMap.put( "falseStepname", new StringLoadSaveValidator() );

    getterMap.put( "send_true_to", "getTrueStepname" );
    setterMap.put( "send_true_to", "setTrueStepname" );
    getterMap.put( "send_false_to", "getFalseStepname" );
    setterMap.put( "send_false_to", "setFalseStepname" );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws HopException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testClone() {
    FilterRowsMeta filterRowsMeta = new FilterRowsMeta();
    filterRowsMeta.setCondition( new Condition() );
    filterRowsMeta.setTrueStepname( "true" );
    filterRowsMeta.setFalseStepname( "false" );

    FilterRowsMeta clone = (FilterRowsMeta) filterRowsMeta.clone();
    assertNotNull( clone.getCondition() );
    assertEquals( "true", clone.getTrueStepname() );
    assertEquals( "false", clone.getFalseStepname() );
  }

  @Test
  public void modifiedTarget() throws Exception {
    FilterRowsMeta filterRowsMeta = new FilterRowsMeta();
    StepMeta trueOutput = new StepMeta( "true", new DummyTransMeta() );
    StepMeta falseOutput = new StepMeta( "false", new DummyTransMeta() );

    filterRowsMeta.setCondition( new Condition() );
    filterRowsMeta.setTrueStepname( trueOutput.getName() );
    filterRowsMeta.setFalseStepname( falseOutput.getName() );
    filterRowsMeta.searchInfoAndTargetSteps( ImmutableList.of( trueOutput, falseOutput ) );

    trueOutput.setName( "true renamed" );
    falseOutput.setName( "false renamed" );

    assertEquals( "true renamed", filterRowsMeta.getTrueStepname() );
    assertEquals( "false renamed", filterRowsMeta.getFalseStepname() );
  }
}
