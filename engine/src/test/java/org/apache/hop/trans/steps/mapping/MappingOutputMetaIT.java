/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.apache.hop.trans.steps.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.repository.Repository;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.steps.mappingoutput.MappingOutputMeta;
import org.apache.hop.metastore.api.IMetaStore;

public class MappingOutputMetaIT {

  private Repository repository = mock( Repository.class );
  private IMetaStore metaStore = mock( IMetaStore.class );
  private VariableSpace space = mock( VariableSpace.class );
  private StepMeta nextStep = mock( StepMeta.class );
  private RowMetaInterface[] info = new RowMetaInterface[] { mock( RowMetaInterface.class ) };

  @Test
  @Ignore // TODO figure out what's going on here!
  public void testGetFields_OutputValueRenames_WillRenameOutputIfValueMetaExist() throws HopStepException {
    ValueMetaInterface valueMeta1 = new ValueMetaBase( "valueMeta1" );
    ValueMetaInterface valueMeta2 = new ValueMetaBase( "valueMeta2" );

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( valueMeta1 );
    rowMeta.addValueMeta( valueMeta2 );

    List<MappingValueRename> outputValueRenames = new ArrayList<MappingValueRename>();
    outputValueRenames.add( new MappingValueRename( "valueMeta2", "valueMeta1" ) );
    MappingOutputMeta meta = new MappingOutputMeta();
    meta.setOutputValueRenames( outputValueRenames );
    meta.getFields( rowMeta, null, info, nextStep, space, repository, metaStore );

    //we must not add additional field
    assertEquals( 2, rowMeta.getValueMetaList().size() );

    //we must not keep the first value meta since we want to rename second
    assertEquals( valueMeta1, rowMeta.getValueMeta( 0 ) );

    //the second value meta must be other than we want to rename since we already have value meta with such name
    assertFalse( "valueMeta1".equals( rowMeta.getValueMeta( 1 ).getName() ) );

   //the second value meta must be other than we want to rename since we already have value meta with such name. 
    //It must be renamed according the rules from the #RowMeta
    assertTrue( "valueMeta1_1".equals( rowMeta.getValueMeta( 1 ).getName() ) );
  }

}
