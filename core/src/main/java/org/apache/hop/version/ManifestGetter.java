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

package org.apache.hop.version;

import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.Manifest;

public class ManifestGetter {
  public Manifest getManifest() throws Exception {
    URL url = this.getClass().getResource( BuildVersion.REFERENCE_FILE );
    JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
    return jarConnection.getManifest();
  }
}
