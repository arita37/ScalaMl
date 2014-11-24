/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the 
 * concepts and algorithms presented in "Scala for Machine Learning" ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * Version 0.96
 * 
 * This code uses the iitb CRF library 
 * Copyright (c) <2004> <Sunita Sarawagi Indian Institute of Technology Bombay> All rights reserved.
 */
package org.scalaml.supervised.crf

import iitb.CRF.{CRF, CrfParams, DataSequence, DataIter, FeatureGenerator}
import iitb.Model.{FeatureGenImpl, CompleteModel}
import org.scalaml.core.XTSeries
import org.scalaml.workflow.data.DataSource
import org.scalaml.core.design.{PipeOperator, Config}
import java.io.IOException
import org.scalaml.core.types.ScalaMl._




		/**
		 * <p>Class that defines the basic configuration of the CRF algorithm. The class generates a textual
		 * description of the configuration of CRF used by iitb library <br>
		 * <pre><span style="font-size:9pt;color: #351c75;font-family: &quot;Helvetica Neue&quot;,Arial,Helvetica,sans-serif;">
		 * <b>w0</b>         Initial values for the CRF weights/factors (lambdas).
		 * <b>maxIters</b>   Maximum number of iterations to be used for the training of CRF.
		 * <b>lambda</b>     L2-regularization penalty function 1/square(sigma) used in the log likelihood log p(Y|X).
		 * <b>eps</b>        Convergence criteria used on the log likelihood  delta( log p(Y|X)to exit from the training iteration.
		 * </span></pre></p>
		 * @constructor Create a configuration for the CRF. [w0] Initial values for the CRF weights/factors (lambdas). [maxIters] Maximum number of iterations to be used for the training of CRF. [lambda] L2-regularization penalty function 1/square(sigma) used in the log likelihood log p(Y|X). [eps] Convergence criteria used on the log likelihood  delta( log p(Y|X)to exit from the training iteration
		 * @author Patrick Nicolas
		 * @since April 3, 2014
		 * @note Scala for Machine Learning Chapter 7 Sequential data models/Conditional Random Fields.
		 */
protected class CrfConfig(w0: Double, maxIters: Int, lambda: Double, eps:Double) extends Config {
	import CrfConfig._
	check(w0, maxIters, lambda, eps)

	val persists: String = "config/crf"
    
		// textual description of the CRF configuration
	val params = s"initValue: ${String.valueOf(w0)}, maxIters: ${String.valueOf(maxIters)}, lambda: ${String.valueOf(lambda)}, scale: true, eps: $eps"
}


		/**
		 * <p>Companion object for the configuration of the conditional random field. The
		 * singleton is used to define constructors and boundaries for the class parameters..</p>
		 */
object CrfConfig {
	final val INIT_WEIGHTS_LIMITS = (0.1, 2.5)
	final val MAX_ITERS_LIMITS = (10, 250)
	final val LAMBDA_LIMITS = (1e-15, 1.5)
	final val EPS_LIMITS = (1e-5, 0.2)
	
	
	def apply(w0: Double, maxIters: Int, lambda: Double, eps:Double): CrfConfig = 
		new CrfConfig(w0, maxIters, lambda, eps)
	
	
	private def check(w0: Double, maxIters: Int, lambda: Double,  eps: Double): Unit = {
		require(w0 >= INIT_WEIGHTS_LIMITS._1 && w0 <= INIT_WEIGHTS_LIMITS._2, s"Initialization of the CRF weights $w0 is out of range")
		require( maxIters >= MAX_ITERS_LIMITS._1 && maxIters <= MAX_ITERS_LIMITS._2, s"Maximum number of iterations for CRF training $maxIters is out of range")
		require( lambda >= LAMBDA_LIMITS._1 && lambda <= LAMBDA_LIMITS._2, s"The factor for the L2 penalty for CRF $lambda is out of range")
		require( eps > EPS_LIMITS._1 && eps<= EPS_LIMITS._2, s"The convergence criteria for the CRF training $eps is out of range")
    }
}



// ---------------------------- EOF ------------------------------------------------------