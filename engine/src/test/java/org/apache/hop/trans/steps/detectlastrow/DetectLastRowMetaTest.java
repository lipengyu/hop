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

package org.apache.hop.trans.steps.detectlastrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.TransMeta.TransformationType;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;

public class DetectLastRowMetaTest {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Test
  public void testStepMeta() throws HopException {
    List<String> attributes = Arrays.asList( "ResultFieldName" );

    LoadSaveTester<DetectLastRowMeta> loadSaveTester = new LoadSaveTester<>( DetectLastRowMeta.class, attributes );
    loadSaveTester.testSerialization();
  }

  @Test
  public void testDefault() {
    DetectLastRowMeta meta = new DetectLastRowMeta();
    meta.setDefault();
    assertEquals( "result", meta.getResultFieldName() );
  }

  @Test
  public void testGetData() {
    DetectLastRowMeta meta = new DetectLastRowMeta();
    assertTrue( meta.getStepData() instanceof DetectLastRowData );
  }

  @Test
  public void testGetFields() throws HopStepException {
    DetectLastRowMeta meta = new DetectLastRowMeta();
    meta.setDefault();
    meta.setResultFieldName( "The Result" );
    RowMeta rowMeta = new RowMeta();
    meta.getFields( rowMeta, "this step", null, null, new Variables(), null, null );

    assertEquals( 1, rowMeta.size() );
    assertEquals( "The Result", rowMeta.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, rowMeta.getValueMeta( 0 ).getType() );
  }

  @Test
  public void testSupportedTransformationTypes() {
    DetectLastRowMeta meta = new DetectLastRowMeta();
    assertEquals( 1, meta.getSupportedTransformationTypes().length );
    assertEquals( TransformationType.Normal, meta.getSupportedTransformationTypes()[0] );
  }
}
