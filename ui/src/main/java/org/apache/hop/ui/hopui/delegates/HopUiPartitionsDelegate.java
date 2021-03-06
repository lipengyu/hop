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

package org.apache.hop.ui.hopui.delegates;

import java.util.List;

import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.partition.PartitionSchema;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.partition.dialog.PartitionSchemaDialog;
import org.apache.hop.ui.hopui.HopUi;
import org.apache.hop.ui.hopui.tree.provider.PartitionsFolderProvider;

public class HopUiPartitionsDelegate extends HopUiSharedObjectDelegate {
  public HopUiPartitionsDelegate( HopUi hopUi ) {
    super( hopUi );
  }

  public void newPartitioningSchema( TransMeta transMeta ) {
    PartitionSchema partitionSchema = new PartitionSchema();

    PartitionSchemaDialog dialog =
        new PartitionSchemaDialog( hopUi.getShell(), partitionSchema, transMeta.getPartitionSchemas(), transMeta
            .getDatabases(), transMeta );
    if ( dialog.open() ) {
      List<PartitionSchema> partitions = transMeta.getPartitionSchemas();
      if ( isDuplicate( partitions, partitionSchema ) ) {
        new ErrorDialog(
          hopUi.getShell(), getMessage( "Spoon.Dialog.ErrorSavingPartition.Title" ), getMessage(
          "Spoon.Dialog.ErrorSavingPartition.Message", partitionSchema.getName() ),
          new HopException( getMessage( "Spoon.Dialog.ErrorSavingPartition.NotUnique" ) ) );
        return;
      }

      partitions.add( partitionSchema );

      if ( hopUi.rep != null ) {
        try {
          if ( !hopUi.rep.getSecurityProvider().isReadOnly() ) {
            hopUi.rep.save( partitionSchema, Const.VERSION_COMMENT_INITIAL_VERSION, null );
            if ( sharedObjectSyncUtil != null ) {
              sharedObjectSyncUtil.reloadTransformationRepositoryObjects( false );
            }
          } else {
            throw new HopException( BaseMessages.getString(
              PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser" ) );
          }
        } catch ( HopException e ) {
          showSaveErrorDialog( partitionSchema, e );
        }
      }

      refreshTree();
    }
  }

  public void editPartitionSchema( TransMeta transMeta, PartitionSchema partitionSchema ) {
    String originalName = partitionSchema.getName();
    PartitionSchemaDialog dialog =
        new PartitionSchemaDialog( hopUi.getShell(), partitionSchema, transMeta.getPartitionSchemas(),
            transMeta.getDatabases(), transMeta );
    if ( dialog.open() ) {
      if ( hopUi.rep != null && partitionSchema.getObjectId() != null ) {
        try {
          saveSharedObjectToRepository( partitionSchema, null );
          if ( sharedObjectSyncUtil != null ) {
            sharedObjectSyncUtil.synchronizePartitionSchemas( partitionSchema, originalName );
          }
        } catch ( HopException e ) {
          showSaveErrorDialog( partitionSchema, e );
        }
      }
      refreshTree();
    }
  }

  public void delPartitionSchema( TransMeta transMeta, PartitionSchema partitionSchema ) {
    try {
      int idx = transMeta.getPartitionSchemas().indexOf( partitionSchema );
      transMeta.getPartitionSchemas().remove( idx );

      if ( hopUi.rep != null && partitionSchema.getObjectId() != null ) {
        // remove the partition schema from the repository too...
        hopUi.rep.deletePartitionSchema( partitionSchema.getObjectId() );
        if ( sharedObjectSyncUtil != null ) {
          sharedObjectSyncUtil.deletePartitionSchema( partitionSchema );
        }
      }
      refreshTree();
    } catch ( HopException e ) {
      new ErrorDialog(
        hopUi.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorDeletingClusterSchema.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorDeletingClusterSchema.Message" ), e );
    }
  }

  private void showSaveErrorDialog( PartitionSchema partitionSchema, HopException e ) {
    new ErrorDialog( hopUi.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingPartition.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingPartition.Message", partitionSchema.getName() ), e );
  }

  private void refreshTree() {
    hopUi.refreshTree( PartitionsFolderProvider.STRING_PARTITIONS );
  }
}
