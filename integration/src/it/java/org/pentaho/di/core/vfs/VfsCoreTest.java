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

package org.apache.hop.core.vfs;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.Const;

public class VfsCoreTest extends TestCase {

  private static final String tmpDir = System.getProperty( "java.io.tmpdir" );
  private static final String content = "Just a small line of text\n";

  public void testWriteReadFile() throws Exception {
    // Write a text
    //
    FileObject tempFile = HopVFS.createTempFile( "prefix", "suffix", tmpDir );
    OutputStream outputStream = HopVFS.getOutputStream( tempFile, false );
    OutputStreamWriter writer = new OutputStreamWriter( outputStream );
    writer.write( content );
    writer.close();
    outputStream.close();

    // Read it back...
    //
    InputStream inputStream = HopVFS.getInputStream( tempFile );
    StringBuffer buffer = new StringBuffer();
    int c;
    while ( ( c = inputStream.read() ) >= 0 ) {
      buffer.append( (char) c );
    }
    inputStream.close();

    assertEquals( content, buffer.toString() );

    // Now open the data as a regular file...
    //
    String url = tempFile.getName().getURI();
    String textFileContent = HopVFS.getTextFileContent( url, Const.XML_ENCODING );

    // Now delete the file...
    //
    tempFile.delete();

    assertEquals( false, tempFile.exists() );

    assertEquals( content, textFileContent );
  }
}
