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

package org.apache.hop.kitchen;

import junit.framework.TestCase;

import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.pan.CommandLineOption;

/**
 * Kitchen Tests
 *
 * @author jganoff
 */
public class KitchenIT extends TestCase {

  public void testArgumentMaxLogLines_valid() throws HopException {
    final String maxLogLinesArg = "50";
    int expected = 50;
    CommandLineOption opt = new CommandLineOption( "maxloglines", null, new StringBuilder( maxLogLinesArg ) );
    int maxLogLines = Kitchen.parseIntArgument( opt, 0 );
    assertEquals( expected, maxLogLines );
  }

  public void testArgumentMaxLogLines_invalid() {
    final String maxLogLinesArg = "fifty";
    CommandLineOption opt = new CommandLineOption( "maxloglines", null, new StringBuilder( maxLogLinesArg ) );
    try {
      Kitchen.parseIntArgument( opt, 0 );
      fail( "Argument should not be parsable" );
    } catch ( HopException expected ) {
      assertTrue( "Error is not as expected: " + expected.getMessage(), expected.getMessage().contains(
        "ERROR: maxloglines" ) );
    }
  }

  public void testArgumentMaxLogTimeout_valid() throws HopException {
    final String maxLogTimeoutArg = "658";
    int expected = 658;
    CommandLineOption opt = new CommandLineOption( "maxlogtimeout", null, new StringBuilder( maxLogTimeoutArg ) );
    int maxLogLines = Kitchen.parseIntArgument( opt, 0 );
    assertEquals( expected, maxLogLines );
  }

  public void testArgumentMaxLogTimeout_invalid() {
    final String maxLogTimeoutArg = "sixfiftyeight";
    CommandLineOption opt = new CommandLineOption( "maxlogtimeout", null, new StringBuilder( maxLogTimeoutArg ) );
    try {
      Kitchen.parseIntArgument( opt, 0 );
      fail( "Argument should not be parsable" );
    } catch ( HopException expected ) {
      assertTrue( "Error is not as expected: " + expected.getMessage(), expected.getMessage().contains(
        "ERROR: maxlogtimeout" ) );
    }
  }

  public void testConfigureLogging() throws HopException {
    final String maxLogTimeoutArg = "600";
    // Init Hop Environment so the default CentralLogStore is initialized
    HopEnvironment.init();
    // Change the max nr of lines
    final int maxNrLines = HopLogStore.getAppender().getMaxNrLines() + 50;
    final String maxLogLinesArg = String.valueOf( maxNrLines );
    CommandLineOption maxLogLinesOption =
      new CommandLineOption( "maxloglines", null, new StringBuilder( maxLogLinesArg ) );
    CommandLineOption maxLogTimeoutOption =
      new CommandLineOption( "maxlogtimeout", null, new StringBuilder( maxLogTimeoutArg ) );
    // Configure logging with the new options
    Kitchen.configureLogging( maxLogLinesOption, maxLogTimeoutOption );
    assertEquals( maxNrLines, HopLogStore.getAppender().getMaxNrLines() );
  }
}
