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

package org.apache.hop.ui.hopui.tree;

import org.eclipse.swt.graphics.Image;
import org.apache.hop.base.AbstractMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.ui.core.widget.tree.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bmorrise on 7/9/18.
 */
public class RootNode extends TreeNode {

  private List<TreeFolderProvider> treeFolderProviders = new ArrayList<>();
  private Map<AbstractMeta, TreeNode> abstractMetas = new HashMap<>();
  private Map<AbstractMeta, String> updates = new HashMap<>();

  public RootNode( String label, Image image, boolean expanded ) {
    super( label, image, expanded );
  }

  public void addProvider( TreeFolderProvider treeFolderProvider ) {
    treeFolderProviders.add( treeFolderProvider );
  }

  public void addProviders( List<TreeFolderProvider> treeFolderProviders ) {
    this.treeFolderProviders.addAll( treeFolderProviders );
  }

  public void addAbstractMeta( AbstractMeta abstractMeta, TreeNode treeNode ) {
    abstractMetas.put( abstractMeta, treeNode );
  }

  public TreeNode create( AbstractMeta abstractMeta, Image image, boolean expanded ) {
    TreeNode treeNode = new TreeNode( abstractMeta.getName(), image, expanded );
    addChild( treeNode );
    addAbstractMeta( abstractMeta, treeNode );
    treeFolderProviders.forEach( treeFolderProvider -> treeFolderProvider.create( abstractMeta, treeNode ) );
    return treeNode;
  }

  public void checkUpdate( AbstractMeta abstractMeta, String filter ) {
    TreeNode treeNode = abstractMetas.get( abstractMeta );
    if ( treeNode != null ) {
      for ( int i = 0; i < treeFolderProviders.size(); i++ ) {
        TreeNode childTreeNode = treeNode.getChildren().get( i );
        treeFolderProviders.get( i ).checkUpdate( abstractMeta, childTreeNode, filter );
        if ( !Utils.isEmpty( filter ) ) {
          childTreeNode.setExpanded( true );
        }
      }
    }
  }

  public String getNameByType( Class clazz ) {
    TreeFolderProvider treeFolderProvider = treeFolderProviders.stream()
            .filter( treeFolderProvider1 -> treeFolderProvider1.getType().equals( clazz ) )
            .findFirst()
            .orElse( null );
    if ( treeFolderProvider != null ) {
      return treeFolderProvider.getTitle();
    }
    return null;
  }

  public void remove( AbstractMeta abstractMeta ) {
    abstractMetas.remove( abstractMeta );
  }

  public TreeNode getTreeNode( AbstractMeta abstractMeta ) {
    return abstractMetas.get( abstractMeta );
  }

  public void update( String name ) {
    abstractMetas.keySet().forEach( abstractMeta -> updates.put( abstractMeta, name ) );
  }

  public void clearUpdates( AbstractMeta abstractMeta ) {
    updates.remove( abstractMeta );
  }

  public boolean hasNode( AbstractMeta abstractMeta ) {
    return abstractMetas.containsKey( abstractMeta );
  }

  public boolean shouldUpdate( AbstractMeta abstractMeta, String name ) {
    if ( updates.containsKey( abstractMeta ) && updates.get( abstractMeta ).equalsIgnoreCase( name ) ) {
      return true;
    }
    return false;
  }
}
