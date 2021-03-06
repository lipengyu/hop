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

package org.apache.hop.ui.hopui;

import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.apache.hop.core.Const;
import org.apache.hop.core.EngineMetaInterface;
import org.apache.hop.core.LastUsedFile;
import org.apache.hop.core.ObjectLocationSpecificationMethod;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopMissingPluginsException;
import org.apache.hop.core.extension.ExtensionPointHandler;
import org.apache.hop.core.extension.HopExtensionPoint;
import org.apache.hop.core.gui.OverwritePrompter;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.steps.jobexecutor.JobExecutorMeta;
import org.apache.hop.trans.steps.transexecutor.TransExecutorMeta;
import org.apache.hop.ui.core.PropsUI;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.gui.GUIResource;
import org.apache.hop.ui.trans.steps.missing.MissingTransDialog;
import org.w3c.dom.Node;

public class TransFileListener implements FileListener {

  private static Class<?> PKG = HopUi.class; // for i18n purposes, needed by Translator2!!

  public boolean open( Node transNode, String fname, boolean importfile ) throws HopMissingPluginsException {
    final HopUi hopUi = HopUi.getInstance();
    final PropsUI props = PropsUI.getInstance();
    try {
      // Call extension point(s) before the file has been opened
      ExtensionPointHandler.callExtensionPoint( hopUi.getLog(), HopExtensionPoint.TransBeforeOpen.id, fname );

      TransMeta transMeta = new TransMeta();
      transMeta.loadXML(
        transNode, fname, hopUi.getMetaStore(), hopUi.getRepository(), true, new Variables(),
        new OverwritePrompter() {

          public boolean overwritePrompt( String message, String rememberText, String rememberPropertyName ) {
            MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageHopUi() );
            Object[] res =
              hopUi.messageDialogWithToggle(
                BaseMessages.getString( PKG, "System.Button.Yes" ), null, message, Const.WARNING,
                new String[]{
                  BaseMessages.getString( PKG, "System.Button.Yes" ),
                  BaseMessages.getString( PKG, "System.Button.No" ) }, 1, rememberText, !props
                  .askAboutReplacingDatabaseConnections() );
            int idx = ( (Integer) res[0] ).intValue();
            boolean toggleState = ( (Boolean) res[1] ).booleanValue();
            props.setAskAboutReplacingDatabaseConnections( !toggleState );

            return ( ( idx & 0xFF ) == 0 ); // Yes means: overwrite
          }

        } );

      if ( transMeta.hasMissingPlugins() ) {
        StepMeta stepMeta = transMeta.getStep( 0 );
        MissingTransDialog missingDialog =
          new MissingTransDialog( hopUi.getShell(), transMeta.getMissingTrans(), stepMeta.getStepMetaInterface(),
            transMeta, stepMeta.getName() );
        if ( missingDialog.open() == null ) {
          return true;
        }
      }
      transMeta.setRepositoryDirectory( hopUi.getDefaultSaveLocation( transMeta ) );
      transMeta.setRepository( hopUi.getRepository() );
      transMeta.setMetaStore( hopUi.getMetaStore() );
      hopUi.setTransMetaVariables( transMeta );
      hopUi.getProperties().addLastFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, fname, null, false, null );
      hopUi.addMenuLast();

      // If we are importing into a repository we need to fix 
      // up the references to other jobs and transformations
      // if any exist.
      if ( importfile ) {
        if ( hopUi.getRepository() != null ) {
          transMeta = fixLinks( transMeta );
        }
      } else {
        transMeta.clearChanged();
      }

      transMeta.setFilename( fname );
      hopUi.addTransGraph( transMeta );
      hopUi.sharedObjectsFileMap.put( transMeta.getSharedObjects().getFilename(), transMeta.getSharedObjects() );

      // Call extension point(s) now that the file has been opened
      ExtensionPointHandler.callExtensionPoint( hopUi.getLog(), HopExtensionPoint.TransAfterOpen.id, transMeta );

      HopUiPerspectiveManager.getInstance().activatePerspective( MainHopUiPerspective.class );
      hopUi.refreshTree();
      return true;

    } catch ( HopMissingPluginsException e ) {
      throw e;
    } catch ( HopException e ) {
      new ErrorDialog(
        hopUi.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorOpening.Title" ), BaseMessages
        .getString( PKG, "Spoon.Dialog.ErrorOpening.Message" )
        + fname, e );
    }
    return false;
  }

  private TransMeta fixLinks( TransMeta transMeta ) {
    transMeta = processLinkedJobs( transMeta );
    transMeta = processLinkedTrans( transMeta );

    return transMeta;
  }

  protected TransMeta processLinkedJobs( TransMeta transMeta ) {
    for ( StepMeta stepMeta : transMeta.getSteps() ) {
      if ( stepMeta.getStepID().equalsIgnoreCase( "JobExecutor" ) ) {
        JobExecutorMeta jem = (JobExecutorMeta) stepMeta.getStepMetaInterface();
        ObjectLocationSpecificationMethod specMethod = jem.getSpecificationMethod();
        // If the reference is by filename, change it to Repository By Name. Otherwise it's fine so leave it alone
        if ( specMethod == ObjectLocationSpecificationMethod.FILENAME ) {
          jem.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
          String filename = jem.getFileName();
          String jobname = filename.substring( filename.lastIndexOf( "/" ) + 1, filename.lastIndexOf( '.' ) );
          String directory = filename.substring( 0, filename.lastIndexOf( "/" ) );
          jem.setJobName( jobname );
          jem.setDirectoryPath( directory );
        }
      }
    }
    return transMeta;
  }

  protected TransMeta processLinkedTrans( TransMeta transMeta ) {
    for ( StepMeta stepMeta : transMeta.getSteps() ) {
      if ( stepMeta.getStepID().equalsIgnoreCase( "TransExecutor" ) ) {
        TransExecutorMeta tem = (TransExecutorMeta) stepMeta.getStepMetaInterface();
        ObjectLocationSpecificationMethod specMethod = tem.getSpecificationMethod();
        // If the reference is by filename, change it to Repository By Name. Otherwise it's fine so leave it alone
        if ( specMethod == ObjectLocationSpecificationMethod.FILENAME ) {
          tem.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
          String filename = tem.getFileName();
          String jobname = filename.substring( filename.lastIndexOf( "/" ) + 1, filename.lastIndexOf( '.' ) );
          String directory = filename.substring( 0, filename.lastIndexOf( "/" ) );
          tem.setTransName( jobname );
          tem.setDirectoryPath( directory );
        }
      }
    }
    return transMeta;
  }

  public boolean save( EngineMetaInterface meta, String fname, boolean export ) {
    HopUi hopUi = HopUi.getInstance();
    EngineMetaInterface lmeta;
    if ( export ) {
      lmeta = (TransMeta) ( (TransMeta) meta ).realClone( false );
    } else {
      lmeta = meta;
    }

    try {
      ExtensionPointHandler.callExtensionPoint( hopUi.getLog(), HopExtensionPoint.TransBeforeSave.id, lmeta );
    } catch ( HopException e ) {
      // fails gracefully
    }

    boolean saveStatus = hopUi.saveMeta( lmeta, fname );

    if ( saveStatus ) {
      try {
        ExtensionPointHandler.callExtensionPoint( hopUi.getLog(), HopExtensionPoint.TransAfterSave.id, lmeta );
      } catch ( HopException e ) {
        // fails gracefully
      }
    }

    return saveStatus;
  }

  public void syncMetaName( EngineMetaInterface meta, String name ) {
    ( (TransMeta) meta ).setName( name );
  }

  public boolean accepts( String fileName ) {
    if ( fileName == null || fileName.indexOf( '.' ) == -1 ) {
      return false;
    }
    String extension = fileName.substring( fileName.lastIndexOf( '.' ) + 1 );
    return extension.equals( "ktr" );
  }

  public boolean acceptsXml( String nodeName ) {
    if ( "transformation".equals( nodeName ) ) {
      return true;
    }
    return false;
  }

  public String[] getFileTypeDisplayNames( Locale locale ) {
    return new String[]{ "Transformations", "XML" };
  }

  public String getRootNodeName() {
    return "transformation";
  }

  public String[] getSupportedExtensions() {
    return new String[]{ "ktr", "xml" };
  }

}
