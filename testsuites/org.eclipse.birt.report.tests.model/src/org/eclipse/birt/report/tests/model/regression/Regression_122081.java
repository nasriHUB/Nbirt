/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.tests.model.regression;

import java.io.IOException;

import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ParameterHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.command.LibraryException;
import org.eclipse.birt.report.tests.model.BaseTestCase;

/**
 * Regression description:
 * </p>
 * Extend a datasource/dataset/parameter from a library. No hint message when
 * removing the library from the report
 * </p>
 * Test description:
 * <p>
 * Library can't be removed if any decendent exists in report, and it could be
 * removed from the report before all decendents are removed
 * </p>
 */
public class Regression_122081 extends BaseTestCase
{

	private String filename = "Regression_122081.xml"; //$NON-NLS-1$

	/**
	 * @throws DesignFileException
	 * @throws SemanticException
	 * @throws IOException
	 */
	
	public void test_regression_122081( ) throws DesignFileException, SemanticException,
			IOException
	{
		openDesign( filename );

		// can't drop library since it has descendents
		
		libraryHandle = designHandle.getLibrary( "Lib" ); //$NON-NLS-1$
		try
		{
			designHandle.dropLibrary( libraryHandle ); 
			fail( );
		}
		catch ( Exception e )
		{
			assertEquals(
					LibraryException.DESIGN_EXCEPTION_LIBRARY_HAS_DESCENDENTS,
					( (LibraryException) e ).getErrorCode( ) );
		}
		

		// library can be removed after all decendents have been dropped
		
		ParameterHandle param = designHandle.findParameter( "NewParameter" ); //$NON-NLS-1$
		DataSourceHandle datasource = designHandle
				.findDataSource( "Data Source" ); //$NON-NLS-1$
		DataSetHandle dataset = designHandle.findDataSet( "Data Set" ); //$NON-NLS-1$
		param.drop( );
		datasource.drop( );
		dataset.drop( );
		try
		{
			designHandle.dropLibrary( libraryHandle );
		}
		catch ( Exception e )
		{
			fail( );
		}
	}
}
