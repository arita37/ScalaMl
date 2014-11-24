/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the 
 * concepts and algorithms presented in "Scala for Machine Learning" ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * Version 0.96
 */
package org.scalaml.workflow.data

import scala.util.{Try, Success, Failure}
import DocumentsSource._
import org.scalaml.util.Display
import org.apache.log4j.Logger


		/**
		 * <p>Class that define the extraction of a document from a corpus
		 * or a list of text file using the following format.
		 * <ul>
		 * <li>date</li>
		 * <li>Title</li>
		 * <li>Content</li>
		 * </ul>
		 * <pre><span style="font-size:9pt;color: #351c75;font-family: &quot;Helvetica Neue&quot;,Arial,Helvetica,sans-serif;">
		 * <b>pathName</b>   Relative path for the directory containing the corpus.
		 * </span></pre></p>
		 * @constructor Create a source for a set of documents in a given relative path.
		 * @throws IllegalArgumentException if the path is not defined
		 * 
		 * @author Patrick Nicolas
		 * @since January 15, 2014
		 * @note Scala for Machine Learning Chapter 5 Naive Bayes models
		 */
final class DocumentsSource(val pathName: String) {
	require(pathName != null && pathName.length > 1, "Cannot create a data source with undefined path")
	   
	private val logger = Logger.getLogger("TextSource")
	
	private val filesList: Array[String] = {   
		val file = new java.io.File(pathName)
		if( file.isDirectory) 
			file.listFiles.map( x => x.getName) 
		else 
			Array[String](pathName)
	}
    
		/**
		 * <p>Extract a Corpus from a set documents located in pathName directory.</p>
		 * @return corpus of document grouped and ordered by date.
		 */
	def |> : Corpus = {
		import scala.io.Source
		import java.io.{FileNotFoundException, IOException}
  	  
		filesList.map( fName => {
			val src = Source.fromFile(pathName + fName)	
			val fieldIter = src.getLines
	  	  	  
			val date = nextField(fieldIter)
			val title = nextField(fieldIter)
			val content = fieldIter.foldLeft(new StringBuilder)((b, str) => b.append(str.trim))

			src.close
			if(date == None || title == None)
				throw new IllegalStateException(s"DocumentsSource.|> title or date for $fName is malformatted")
			(date.get, title.get, content.toString) 
		}) 
	}
   

	private def nextField(iter: Iterator[String]): Option[String] = 
		iter.find( s=> (s != null && s.length > 1))
}


	/**
	 * <p>Companion object for the Document Source
	 */
object DocumentsSource {
	
		/**
		 * A corpus is defined as a sequence of {stringized data, title, content} tuples
		 */
	type Corpus = Array[(String, String, String)]
	def apply(pathName: String): DocumentsSource = new DocumentsSource(pathName)
}


// ---------------------------------  EOF --------------------------------------------------