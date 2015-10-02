/*

Copyright 2003 James A. Mason

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
import java.io.*;
import java.lang.reflect.*;

/**
   Merges grammars in files whose names are listed second through last
   in lines from System.in and writes the result to the file whose name
   is listed in the first line from System.in.
   <BR><BR>
   Command line usage:
   <BR><tt><b> java asd/MergeGrammars < buildList.txt</b></tt>
   <BR>
   where buildList.txt contains a list of file names, one per grammar, on
   separate lines, starting with the name of the file for the resulting merged
   grammar and followed by the names of the files to be merged, in the order
   in which they are to be merged.

   @author James A. Mason
   @version 1.01 2003 July
 */

public class MergeGrammars
{  public static void main(String[] args)
      throws IOException, ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  if (args.length > 0)
      {  System.out.println(
            "Usage should be: java asd/MergeGrammars < buildListFile");
         System.exit(0);
      }
      BufferedReader inputStream = new BufferedReader(
         new InputStreamReader(System.in));
      String inputLine = null;
      String resultFileName = null;
      String inputFileName = null;
      boolean inputException = false;
      ASDDigraph mergedDiagram = null;

      try
      {  inputLine = inputStream.readLine();
      }
      catch(IOException e)
      {  inputException = true;
      }
      if (inputLine == null || inputException)
      {  System.out.println(
            "no file name found for the merged grammar");
         System.exit(0);
      }
      resultFileName = inputLine.trim();

      try
      {  inputLine = inputStream.readLine();
      }
      catch(IOException e)
      {  inputException = true;
      }
      if (inputLine == null || inputException)
      {  System.out.println(
            "no file name found for first grammar to be merged");
         System.exit(0);
      }
      inputFileName = inputLine.trim();

      mergedDiagram = new ASDDigraph(inputFileName, null);
      while (true)
      {  try
         {  inputLine = inputStream.readLine();
         }
         catch(IOException e)
         {  inputException = true;
            System.out.println(
               "An input exception occurred; treated as end of file.");
         }
         if (inputLine == null || inputException) break;
         inputFileName = inputLine.trim();
         if (inputFileName.length() > 0)
            mergedDiagram.mergeInGrammar(inputFileName, null);
      }

      mergedDiagram.saveToFile(resultFileName);
   }
} // end class MergeGrammars
