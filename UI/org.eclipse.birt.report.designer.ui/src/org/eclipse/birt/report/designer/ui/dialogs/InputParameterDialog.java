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

package org.eclipse.birt.report.designer.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.parameters.AbstractParameterGroup;
import org.eclipse.birt.report.designer.ui.parameters.CascadingParameterGroup;
import org.eclipse.birt.report.designer.ui.parameters.ListingParameter;
import org.eclipse.birt.report.designer.ui.parameters.ParameterUtil;
import org.eclipse.birt.report.designer.ui.parameters.RadioParameter;
import org.eclipse.birt.report.designer.ui.parameters.ScalarParameter;
import org.eclipse.birt.report.designer.ui.parameters.StaticTextParameter;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The dialog for inputting report parameter values when previewing report in
 * new preview prototype
 */
public class InputParameterDialog extends Dialog
{

	private static final String NULL_VALUE_STR = ParameterUtil.LABEL_NULL;

	private Shell parentShell;
	private Composite contentPane;
	private ScrolledComposite scrollPane;

	private List params;
	private Map paramValues = new HashMap( );
	private List isRequiredParameters = new ArrayList( );
	private List dataTypeCheckList = new ArrayList( );

	private static IParameterSelectionChoice nullValueChoice = new IParameterSelectionChoice( ) {

		public String getLabel( )
		{
			return NULL_VALUE_STR;
		}

		public Object getValue( )
		{
			return null;
		}

	};

	private static IParameterSelectionChoice blankValueChoice = new IParameterSelectionChoice( ) {

		public String getLabel( )
		{
			return ""; //$NON-NLS-1$
		}

		public Object getValue( )
		{
			return ""; //$NON-NLS-1$
		}
	};

	public InputParameterDialog( Shell parentShell, List params, Map paramValues )
	{
		super( parentShell );
		this.parentShell = parentShell;
		this.params = params;
		if ( paramValues != null )
		{
			this.paramValues.putAll( paramValues );
		}
	}

	protected void buttonPressed( int buttonId )
	{
		if ( buttonId == Window.OK )
		{
			Iterator itr = isRequiredParameters.iterator( );

			while ( itr.hasNext( ) )
			{
				String paramName = (String) itr.next( );
				Object paramValue = paramValues.get( paramName );

				if ( paramValue == null
						|| ( paramValue instanceof String && ( (String) paramValue ).trim( )
								.length( ) == 0 ) )
				{
					MessageDialog.openError( parentShell, "Error", paramName
							+ " cannot be NULL or blank" );
					return;
				}
			}

			itr = dataTypeCheckList.iterator( );
			while ( itr.hasNext( ) )
			{
				ScalarParameter scalarParameter = (ScalarParameter) itr.next( );
				String paramValue = (String) paramValues.get( scalarParameter.getHandle( )
						.getName( ) );
				try
				{
					paramValues.put( scalarParameter.getHandle( ).getName( ),
							scalarParameter.converToDataType( paramValue ) );
				}
				catch ( BirtException e )
				{
					// TODO: handle exception
					MessageDialog.openError( parentShell,
							"Invalid value type",
							"The value \""
									+ paramValue
									+ "\" is invalid with type "
									+ scalarParameter.getHandle( )
											.getDataType( ) );
					return;
				}
			}
		}

		super.buttonPressed( buttonId );
	}

	protected Control createDialogArea( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( );
		layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
		layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
		layout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
		layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
		composite.setLayout( layout );
		composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		applyDialogFont( composite );

		scrollPane = new ScrolledComposite( composite, SWT.H_SCROLL
				| SWT.V_SCROLL );
		scrollPane.setExpandHorizontal( true );
		scrollPane.setExpandVertical( true );

		scrollPane.setLayoutData( new GridData( GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL ) );

		createParameters( );

		return composite;
	}

	private void createParameters( )
	{
		if ( contentPane != null && !contentPane.isDisposed( ) )
		{
			contentPane.dispose( );
		}

		contentPane = new Composite( scrollPane, SWT.NONE );
		scrollPane.setContent( contentPane );

		contentPane.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		contentPane.setLayout( new GridLayout( ) );

		createParametersSection( params, contentPane );

		contentPane.setSize( contentPane.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		scrollPane.setMinSize( contentPane.computeSize( SWT.DEFAULT,
				SWT.DEFAULT ) );
	}

	private void createParametersSection( List children, Composite parent )
	{
		Iterator iterator = children.iterator( );
		while ( iterator.hasNext( ) )
		{
			Object obj = iterator.next( );
			if ( obj instanceof ScalarParameter
					&& !( (ScalarParameter) obj ).getHandle( ).isHidden( ) )
			{
				ScalarParameter param = (ScalarParameter) obj;
				createParamSection( param, parent );
			}
			else if ( obj instanceof AbstractParameterGroup )
			{
				AbstractParameterGroup group = (AbstractParameterGroup) obj;
				createParametersSection( group.getChildren( ),
						createParamGroupSection( group, parent ) );
			}
		}
	}

	private Composite createParamGroupSection(
			AbstractParameterGroup paramGroup, Composite parent )
	{
		Group group = new Group( parent, SWT.NONE );
		group.setText( paramGroup.getHandle( ).getDisplayLabel( ) );
		group.setLayoutData( new GridData( GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL ) );
		group.setLayout( new GridLayout( ) );
		return group;
	}

	private Composite createParamSection( ScalarParameter param,
			Composite parent )
	{
		boolean isRequired = param.getHandle( ).isRequired( );
		boolean isStringType = param.getHandle( )
				.getDataType( )
				.equals( DesignChoiceConstants.PARAM_TYPE_STRING );
		if ( isRequired )
		{
			isRequiredParameters.add( param.getHandle( ).getName( ) );
		}

		Composite container = new Composite( parent, SWT.NONE );
		container.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		GridLayout layout = new GridLayout( );
		layout.numColumns = 2;
		container.setLayout( layout );

		new Label( container, SWT.NONE ).setText( param.getHandle( )
				.getDisplayLabel( )
				+ ":" ); //$NON-NLS-1$

		if ( param instanceof StaticTextParameter )
		{
			final StaticTextParameter textParam = (StaticTextParameter) param;
			String value = textParam.getDefaultValue( );
			dataTypeCheckList.add( textParam );

			Text input = new Text( container, SWT.BORDER );
			input.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

			input.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					Text input = (Text) e.getSource( );
					paramValues.put( textParam.getHandle( ).getName( ),
							input.getText( ) );
				}
			} );

			if ( paramValues.containsKey( textParam.getHandle( ).getName( ) ) )
			{
				value = paramValues.get( textParam.getHandle( ).getName( ) )
						.toString( );
			}

			if ( value != null )
			{
				input.setText( value );
			}
		}
		else if ( param instanceof RadioParameter )
		{
			final RadioParameter radioParameter = (RadioParameter) param;
			Object value = null;

			try
			{
				value = radioParameter.converToDataType( radioParameter.getDefaultValue( ) );
			}
			catch ( BirtException e )
			{
			}

			if ( paramValues.containsKey( radioParameter.getHandle( ).getName( ) ) )
			{
				value = paramValues.get( radioParameter.getHandle( ).getName( ) );
			}

			List list = radioParameter.getValueList( );
			if ( !isRequired )
			{
				list.add( InputParameterDialog.nullValueChoice );
			}

			for ( int i = 0; i < list.size( ); i++ )
			{
				if ( i > 0 )
				{
					new Label( container, SWT.NONE );
				}

				IParameterSelectionChoice choice = (IParameterSelectionChoice) list.get( i );
				Button button = new Button( container, SWT.RADIO );
				button.setText( choice.getLabel( ) );
				button.setData( choice.getValue( ) );

				if ( choice.getValue( ) != null
						&& choice.getValue( ).equals( value ) )
				{
					button.setSelection( true );
				}
				else if ( value == null
						&& choice.getLabel( ).equals( NULL_VALUE_STR ) )
				{
					button.setSelection( true );
				}

				button.addSelectionListener( new SelectionListener( ) {

					public void widgetDefaultSelected( SelectionEvent e )
					{
					}

					public void widgetSelected( SelectionEvent e )
					{
						Button button = (Button) e.getSource( );
						paramValues.put( radioParameter.getHandle( ).getName( ),
								button.getData( ) );
					}
				} );
			}
		}
		else if ( param instanceof ListingParameter )
		{
			final ListingParameter listParam = (ListingParameter) param;
			Object value = null;

			try
			{
				value = listParam.converToDataType( listParam.getDefaultValue( ) );
			}
			catch ( BirtException e )
			{
			}

			if ( paramValues.containsKey( listParam.getHandle( ).getName( ) ) )
			{
				value = paramValues.get( listParam.getHandle( ).getName( ) );

				if ( value != null )
				{
					listParam.setSelectionValue( value.toString( ) );
				}
			}

			Combo combo = new Combo( container, SWT.BORDER );
			combo.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

			List list = new ArrayList( );
			if ( isStringType && !isRequired )
			{
				list.add( blankValueChoice );
				list.addAll( listParam.getValueList( ) );
			}
			else
			{
				list = listParam.getValueList( );
			}

			if ( !isRequired )
			{
				list.add( InputParameterDialog.nullValueChoice );
			}

			for ( Iterator iterator = list.iterator( ); iterator.hasNext( ); )
			{
				IParameterSelectionChoice choice = (IParameterSelectionChoice) iterator.next( );
				String label = (String) ( choice.getLabel( ) == null ? choice.getValue( )
						: choice.getLabel( ) );
				if ( label != null )
				{
					combo.add( label );
					combo.setData( label, choice.getValue( ) );
				}
			}

			if ( value == null && !isRequired )
			{
				combo.select( combo.getItemCount( ) - 1 );
			}
			else
			{
				for ( int i = 0; i < combo.getItemCount( ); i++ )
				{
					if ( combo.getData( combo.getItem( i ) ).equals( value ) )
					{
						combo.select( i );
						break;
					}
				}
			}

			combo.addSelectionListener( new SelectionListener( ) {

				public void widgetDefaultSelected( SelectionEvent e )
				{
				}

				public void widgetSelected( SelectionEvent e )
				{
					Combo combo = (Combo) e.getSource( );
					paramValues.put( listParam.getHandle( ).getName( ),
							combo.getData( combo.getItem( combo.getSelectionIndex( ) ) ) );

					if ( listParam.getParentGroup( ) instanceof CascadingParameterGroup )
					{
						CascadingParameterGroup group = (CascadingParameterGroup) listParam.getParentGroup( );
						if ( group.getPostParameter( listParam ) != null )
						{
							try
							{
								createParameters( );
							}
							catch ( RuntimeException e1 )
							{
								e1.printStackTrace( );
							}
						}
					}
				}
			} );

		}

		return container;
	}

	protected void configureShell( Shell newShell )
	{
		super.configureShell( newShell );
		newShell.setText( Messages.getString( "InputParameterDialog.msg.title" ) ); //$NON-NLS-1$
		newShell.setSize( 400, 400 );
	}

	public Map getParameters( )
	{
		return this.paramValues;
	}
}
