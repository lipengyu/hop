/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.trans.steps.randomvalue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;
import org.apache.hop.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class RandomValueMetaTest {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Test
  public void testStepMeta() throws HopException {
    List<String> attributes = Arrays.asList( "name", "type" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "name", "getFieldName" );
    getterMap.put( "type", "getFieldType" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "name", "setFieldName" );
    setterMap.put( "type", "setFieldType" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidatorAttributeMap.put( "name",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );
    fieldLoadSaveValidatorAttributeMap.put( "type",
      new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( RandomValueMeta.functions.length ), 25 ) );

    LoadSaveTester loadSaveTester = new LoadSaveTester( RandomValueMeta.class, attributes, getterMap, setterMap,
      fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );
    loadSaveTester.testSerialization();
  }
}
