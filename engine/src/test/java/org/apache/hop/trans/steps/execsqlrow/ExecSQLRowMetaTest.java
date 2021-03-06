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
package org.apache.hop.trans.steps.execsqlrow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;

public class ExecSQLRowMetaTest {
  LoadSaveTester loadSaveTester;
  Class<ExecSQLRowMeta> testMetaClass = ExecSQLRowMeta.class;
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    HopEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "sqlFieldName", "updateField", "insertField", "deleteField", "readField", "commitSize", "sqlFromfile", "sendOneStatement", "databaseMeta" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "sendOneStatement", "IsSendOneStatement" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "sendOneStatement", "SetSendOneStatement" );
      }
    };

    loadSaveTester = new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap );
  }

  @Test
  public void testSerialization() throws HopException {
    loadSaveTester.testSerialization();
  }
}
