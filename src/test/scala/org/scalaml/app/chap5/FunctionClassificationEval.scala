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
package org.scalaml.app.chap5


import org.scalaml.supervised.bayes._
import org.scalaml.core.{types, XTSeries}
import org.scalaml.workflow.data.{DataSource,DocumentsSource}
import scala.collection.mutable.ArrayBuffer
import types.ScalaMl._
import org.scalaml.filtering.SimpleMovingAverage
import SimpleMovingAverage._
import scala.collection.immutable.HashSet
import org.scalaml.supervised.bayes.NaiveBayes
import scala.util.{Try, Success, Failure}
import org.apache.log4j.Logger
import org.scalaml.util.Display
import org.scalaml.filtering.DFT
import org.scalaml.app.Eval



		/**
		 * <p>Singleton to evaluate the Binomial Naive Bayes algorithm to classify
		 * mathematical functions. The two categories of mathematical functions are sinusoidal and 
		 * the rectangular function. The mathematical functions are characterized by the frequency spectrum
		 * of the time series generated by the functions. The indices of the ordered frequencies are used
		 * to score the mathematical functions. The frequencies are computed using the Discrete Fourier
		 * series.</p>
		 * 
		 * @author Patrick Nicolas
		 * @since July 3, 2014
		 * @note: Scala for Machine Learning Chapter 5: Naive Bayes Models
		 */
object FunctionClassificationEval extends Eval {
	import scala.util.Random
	
	val name: String = "NaiveBayesEval"
	private val logger = Logger.getLogger(name)
	type Input = Array[(Array[Double], Int)]
			
	private val DATA_SIZE = 1025
	private val ALPHA = 1e-3
	private val BETA = 1e-2
	private val GAMMA = ALPHA*0.1
	private val SPECTRUM = 24
      		
		/**
		 * <p>Execution of the scalatest for <b>NaiveBayes</b> class
		 * This method is invoked by the  actor-based test framework function, ScalaMlTest.evaluate</p>
		 * @param args array of arguments used in the test
		 * @return -1 in case error a positive or null value if the test succeeds. 
		 */
	def run(args: Array[String]): Int = {
			/**
			 * Labeled mathematical functions.
			 */
		val functionsGroup = Array[Double => Double] (
			(x: Double) => {
				val a = ALPHA*(0.5 + GAMMA*Random.nextDouble)
				val b = BETA*(0.5 + GAMMA*Random.nextDouble) 
				Math.sin(a*x) + Math.cos(b*x)
			},
			(x: Double) => if( x > 0.48 && x < 0.52) 1.0 else 0.0
		)
	  
		/**
		 * Training set using the labeled mathematical functions
		 */
		def trainingDatasets(numSamples: Int): Input =  {
			val dataRange = Range(0, DATA_SIZE)
			val data = new Array[(Array[Double], Int)](numSamples)
      	 
			Range(0, numSamples).foreach( n => {
				val index = Random.nextInt(functionsGroup.size)
				val res = createDatasets(functionsGroup(index), dataRange)
				data.update(n, (res, index))
			})
			data
		}
	  
	  
		/**
		 * Method to create datasets for training and testing. It normalizes the values, then computes
		 * the frequencies related to the datasets, ranks the data points in decreasing order of their
		 * frequency and return the SPECTUM indices of the data point with the highest frequency.
		 */
		def createDatasets(f: Double =>Double, dataRange: Range): DblVector = {
			val values = dataRange.map(_ /DATA_SIZE.toDouble).map(f(_))
			val min = values.min
			val delta = values.max - min
			val freq: XTSeries[Double] = (DFT[Double] |> XTSeries[Double](values.map(x =>(x -min)/delta).toArray))

			val freQ = freq.toArray
							.zipWithIndex
							.sortWith( _._1 > _._1 )
			freQ.take(SPECTRUM).map( _._2.toDouble/DATA_SIZE)
		}

		/**
		 * Method to generate datasets for testing
		 */
		def testDataset(f: Double =>Double): DblMatrix = createDatasets(f, Range(0, DATA_SIZE))
																.map(Array[Double](_))

		/**
		 * Our test functions.
		 */
		val g = (x: Double) => Math.cos(ALPHA*x)
		val h = (x: Double) => if( x >= 0.49 && x <= 0.51) 1.0 else 0.0
      
		/**
		 * Scoring function. The linear comparison is used instead of the Gaussian 
		 * distribution because the standard deviation is very small and potentially
		 * introduces significant rounding errors
		 */
		def scoring(x: Double*): Double = Math.abs(x(2) - x(0))
      	
		Try {
			val nb = NaiveBayes(1.0, XTSeries(trainingDatasets(3)), scoring)
			Display.show(s"\n** test#${Eval.testCount} $name Trained model for function classification${nb.toString}", logger)
	      
			val gr = nb |> XTSeries(testDataset(g))
			Display.show(s"$name Naive Bayes classification for 'cos(ALPHA*x)' class: ${gr(0)}", logger)
	      	      
			val hr = nb |> XTSeries(testDataset(h))
			Display.show(s"$name Naive Bayes classification for 'if(x ~ 0.5) 1.0 else 0' class: ${hr(0)}", logger)
		}
		match {
			case Success(res) => res
			case Failure(e) => Display.error(s"name.run Naive Bayes Function classification failed", logger, e)
		}
	}
}


// -----------------------------  EOF ------------------------------------