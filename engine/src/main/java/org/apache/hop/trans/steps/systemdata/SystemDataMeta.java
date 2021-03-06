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

package org.apache.hop.trans.steps.systemdata;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.injection.Injection;
import org.apache.hop.core.injection.InjectionSupported;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaDate;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNone;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.repository.ObjectId;
import org.apache.hop.repository.Repository;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.apache.hop.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 05-aug-2003
 *
 */
@InjectionSupported( localizationPrefix = "SystemDataMeta.Injection." )
public class SystemDataMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SystemDataMeta.class; // for i18n purposes, needed by Translator2!!

  @Injection( name = "FIELD_NAME" )
  private String[] fieldName;

  @Injection( name = "FIELD_TYPE", converter = SystemDataMetaInjectionTypeConverter.class )
  private SystemDataTypes[] fieldType;

  public SystemDataMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldName.
   */
  public String[] getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName
   *          The fieldName to set.
   */
  public void setFieldName( String[] fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * @return Returns the fieldType.
   */
  public SystemDataTypes[] getFieldType() {
    return fieldType;
  }

  /**
   * @param fieldType
   *          The fieldType to set.
   */
  public void setFieldType( SystemDataTypes[] fieldType ) {
    this.fieldType = fieldType;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode );
  }

  public void allocate( int count ) {
    fieldName = new String[count];
    fieldType = new SystemDataTypes[count];
  }

  @Override
  public Object clone() {
    SystemDataMeta retval = (SystemDataMeta) super.clone();

    int count = fieldName.length;

    retval.allocate( count );

    System.arraycopy( fieldName, 0, retval.fieldName, 0, count );
    System.arraycopy( fieldType, 0, retval.fieldType, 0, count );

    return retval;
  }

  private void readData( Node stepnode ) throws HopXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int count = XMLHandler.countNodes( fields, "field" );
      String type;

      allocate( count );

      for ( int i = 0; i < count; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldName[i] = XMLHandler.getTagValue( fnode, "name" );
        type = XMLHandler.getTagValue( fnode, "type" );
        fieldType[i] = SystemDataTypes.getTypeFromString( type );
      }
    } catch ( Exception e ) {
      throw new HopXMLException( "Unable to read step information from XML", e );
    }
  }

  @Override
  public void setDefault() {
    int count = 0;

    allocate( count );

    for ( int i = 0; i < count; i++ ) {
      fieldName[i] = "field" + i;
      fieldType[i] = SystemDataTypes.TYPE_SYSTEM_INFO_SYSTEM_DATE;
    }
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws HopStepException {
    for ( int i = 0; i < fieldName.length; i++ ) {
      ValueMetaInterface v;

      switch ( fieldType[i] ) {
        case TYPE_SYSTEM_INFO_SYSTEM_START: // All date values...
        case TYPE_SYSTEM_INFO_SYSTEM_DATE:
        case TYPE_SYSTEM_INFO_TRANS_DATE_FROM:
        case TYPE_SYSTEM_INFO_TRANS_DATE_TO:
        case TYPE_SYSTEM_INFO_JOB_DATE_FROM:
        case TYPE_SYSTEM_INFO_JOB_DATE_TO:
        case TYPE_SYSTEM_INFO_PREV_DAY_START:
        case TYPE_SYSTEM_INFO_PREV_DAY_END:
        case TYPE_SYSTEM_INFO_THIS_DAY_START:
        case TYPE_SYSTEM_INFO_THIS_DAY_END:
        case TYPE_SYSTEM_INFO_NEXT_DAY_START:
        case TYPE_SYSTEM_INFO_NEXT_DAY_END:
        case TYPE_SYSTEM_INFO_PREV_MONTH_START:
        case TYPE_SYSTEM_INFO_PREV_MONTH_END:
        case TYPE_SYSTEM_INFO_THIS_MONTH_START:
        case TYPE_SYSTEM_INFO_THIS_MONTH_END:
        case TYPE_SYSTEM_INFO_NEXT_MONTH_START:
        case TYPE_SYSTEM_INFO_NEXT_MONTH_END:
        case TYPE_SYSTEM_INFO_MODIFIED_DATE:
        case TYPE_SYSTEM_INFO_HOP_BUILD_DATE:
        case TYPE_SYSTEM_INFO_PREV_WEEK_START:
        case TYPE_SYSTEM_INFO_PREV_WEEK_END:
        case TYPE_SYSTEM_INFO_PREV_WEEK_OPEN_END:
        case TYPE_SYSTEM_INFO_PREV_WEEK_START_US:
        case TYPE_SYSTEM_INFO_PREV_WEEK_END_US:
        case TYPE_SYSTEM_INFO_THIS_WEEK_START:
        case TYPE_SYSTEM_INFO_THIS_WEEK_END:
        case TYPE_SYSTEM_INFO_THIS_WEEK_OPEN_END:
        case TYPE_SYSTEM_INFO_THIS_WEEK_START_US:
        case TYPE_SYSTEM_INFO_THIS_WEEK_END_US:
        case TYPE_SYSTEM_INFO_NEXT_WEEK_START:
        case TYPE_SYSTEM_INFO_NEXT_WEEK_END:
        case TYPE_SYSTEM_INFO_NEXT_WEEK_OPEN_END:
        case TYPE_SYSTEM_INFO_NEXT_WEEK_START_US:
        case TYPE_SYSTEM_INFO_NEXT_WEEK_END_US:
        case TYPE_SYSTEM_INFO_PREV_QUARTER_START:
        case TYPE_SYSTEM_INFO_PREV_QUARTER_END:
        case TYPE_SYSTEM_INFO_THIS_QUARTER_START:
        case TYPE_SYSTEM_INFO_THIS_QUARTER_END:
        case TYPE_SYSTEM_INFO_NEXT_QUARTER_START:
        case TYPE_SYSTEM_INFO_NEXT_QUARTER_END:
        case TYPE_SYSTEM_INFO_PREV_YEAR_START:
        case TYPE_SYSTEM_INFO_PREV_YEAR_END:
        case TYPE_SYSTEM_INFO_THIS_YEAR_START:
        case TYPE_SYSTEM_INFO_THIS_YEAR_END:
        case TYPE_SYSTEM_INFO_NEXT_YEAR_START:
        case TYPE_SYSTEM_INFO_NEXT_YEAR_END:
          v = new ValueMetaDate( fieldName[i] );
          break;
        case TYPE_SYSTEM_INFO_TRANS_NAME:
        case TYPE_SYSTEM_INFO_FILENAME:
        case TYPE_SYSTEM_INFO_ARGUMENT_01:
        case TYPE_SYSTEM_INFO_ARGUMENT_02:
        case TYPE_SYSTEM_INFO_ARGUMENT_03:
        case TYPE_SYSTEM_INFO_ARGUMENT_04:
        case TYPE_SYSTEM_INFO_ARGUMENT_05:
        case TYPE_SYSTEM_INFO_ARGUMENT_06:
        case TYPE_SYSTEM_INFO_ARGUMENT_07:
        case TYPE_SYSTEM_INFO_ARGUMENT_08:
        case TYPE_SYSTEM_INFO_ARGUMENT_09:
        case TYPE_SYSTEM_INFO_ARGUMENT_10:
        case TYPE_SYSTEM_INFO_MODIFIED_USER:
        case TYPE_SYSTEM_INFO_HOSTNAME:
        case TYPE_SYSTEM_INFO_HOSTNAME_REAL:
        case TYPE_SYSTEM_INFO_IP_ADDRESS:
        case TYPE_SYSTEM_INFO_HOP_VERSION:
        case TYPE_SYSTEM_INFO_HOP_BUILD_VERSION:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_LOG_TEXT:
          v = new ValueMetaString( fieldName[i] );
          break;
        case TYPE_SYSTEM_INFO_COPYNR:
        case TYPE_SYSTEM_INFO_TRANS_BATCH_ID:
        case TYPE_SYSTEM_INFO_JOB_BATCH_ID:
        case TYPE_SYSTEM_INFO_CURRENT_PID:
        case TYPE_SYSTEM_INFO_JVM_TOTAL_MEMORY:
        case TYPE_SYSTEM_INFO_JVM_FREE_MEMORY:
        case TYPE_SYSTEM_INFO_JVM_MAX_MEMORY:
        case TYPE_SYSTEM_INFO_JVM_AVAILABLE_MEMORY:
        case TYPE_SYSTEM_INFO_AVAILABLE_PROCESSORS:
        case TYPE_SYSTEM_INFO_JVM_CPU_TIME:
        case TYPE_SYSTEM_INFO_TOTAL_PHYSICAL_MEMORY_SIZE:
        case TYPE_SYSTEM_INFO_TOTAL_SWAP_SPACE_SIZE:
        case TYPE_SYSTEM_INFO_COMMITTED_VIRTUAL_MEMORY_SIZE:
        case TYPE_SYSTEM_INFO_FREE_PHYSICAL_MEMORY_SIZE:
        case TYPE_SYSTEM_INFO_FREE_SWAP_SPACE_SIZE:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_EXIT_STATUS:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_ENTRY_NR:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_ERRORS:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_FILES_RETRIEVED:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_DELETED:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_INPUT:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_OUTPUT:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_READ:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_REJECTED:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_UPDATED:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_NR_LINES_WRITTEN:
          v = new ValueMetaInteger( fieldName[i] );
          v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
          break;
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_RESULT:
        case TYPE_SYSTEM_INFO_PREVIOUS_RESULT_IS_STOPPED:
          v = new ValueMetaBoolean( fieldName[i] );
          break;
        default:
          v = new ValueMetaNone( fieldName[i] );
          break;
      }
      v.setOrigin( name );
      row.addValueMeta( v );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    <fields>" + Const.CR );

    for ( int i = 0; i < fieldName.length; i++ ) {
      retval.append( "      <field>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", fieldName[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "type",
              fieldType[i] != null ? fieldType[i].getCode() : "" ) );
      retval.append( "        </field>" + Const.CR );
    }
    retval.append( "      </fields>" + Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws HopException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        fieldType[i] = SystemDataTypes.getTypeFromString( rep.getStepAttributeString( id_step, i, "field_type" ) );
      }
    } catch ( Exception e ) {
      throw new HopException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws HopException {
    try {
      for ( int i = 0; i < fieldName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", fieldType[i] != null ? fieldType[i]
            .getCode() : "" );
      }
    } catch ( Exception e ) {
      throw new HopException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }

  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    // See if we have input streams leading to this step!
    int nrRemarks = remarks.size();
    for ( int i = 0; i < fieldName.length; i++ ) {
      if ( fieldType[i].ordinal() <= SystemDataTypes.TYPE_SYSTEM_INFO_NONE.ordinal() ) {
        CheckResult cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "SystemDataMeta.CheckResult.FieldHasNoType", fieldName[i] ), stepMeta );
        remarks.add( cr );
      }
    }
    if ( remarks.size() == nrRemarks ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SystemDataMeta.CheckResult.AllTypesSpecified" ), stepMeta );
      remarks.add( cr );
    }
  }

  /**
   * Default a step doesn't use any arguments. Implement this to notify the GUI that a window has to be displayed BEFORE
   * launching a transformation. You can also use this to specify certain Environment variable values.
   *
   * @return A row of argument values. (name and optionally a default value) Put up to 10 values in the row for the
   *         possible 10 arguments. The name of the value is "1" through "10" for the 10 possible arguments.
   */
  @Override
  public Map<String, String> getUsedArguments() {
    Map<String, String> stepArgs = new HashMap<String, String>();
    DecimalFormat df = new DecimalFormat( "00" );

    for ( int argNr = 0; argNr < 10; argNr++ ) {
      boolean found = false;
      for ( int i = 0; i < fieldName.length; i++ ) {
        if ( fieldType[i].ordinal() == SystemDataTypes.TYPE_SYSTEM_INFO_ARGUMENT_01.ordinal() + argNr ) {
          found = true;
        }
      }
      if ( found ) {
        stepArgs.put( df.format( argNr + 1 ), "" );
      }
    }

    return stepArgs;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new SystemData( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new SystemDataData();
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof SystemDataMeta ) ) {
      return false;
    }
    SystemDataMeta that = (SystemDataMeta) o;

    if ( !Arrays.equals( fieldName, that.fieldName ) ) {
      return false;
    }
    if ( !Arrays.equals( fieldType, that.fieldType ) ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode( fieldName );
    result = 31 * result + Arrays.hashCode( fieldType );
    return result;
  }
}
