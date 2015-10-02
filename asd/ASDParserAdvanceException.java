/*

Copyright 2001 James A. Mason

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy
of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.

 */

package asd;

/**
   Exceptions that may be useful in particular pragmatic
   applications to tell the application that an attempt to
   advance the parse has failed.    These can be thrown
   by methods in the application.  They assume that the
   application is driving the parser with the parser's advance()
   and backup() methods, not with its parse() method.  They
   should be caught by the application's own parse driver.
 */
public class ASDParserAdvanceException extends Exception
{  public ASDParserAdvanceException() {}
   public ASDParserAdvanceException(String message)
   {  super(message);
   }
}
