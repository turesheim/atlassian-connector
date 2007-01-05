/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core.ui.wizards;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylar.internal.jira.core.JiraCustomQuery;
import org.eclipse.mylar.internal.jira.core.JiraServerFacade;
import org.eclipse.mylar.internal.jira.core.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.DatePicker;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.search.AbstractRepositoryQueryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.tigris.jira.core.model.Component;
import org.tigris.jira.core.model.IssueType;
import org.tigris.jira.core.model.Priority;
import org.tigris.jira.core.model.Project;
import org.tigris.jira.core.model.Resolution;
import org.tigris.jira.core.model.Status;
import org.tigris.jira.core.model.Version;
import org.tigris.jira.core.model.filter.ComponentFilter;
import org.tigris.jira.core.model.filter.ContentFilter;
import org.tigris.jira.core.model.filter.CurrentUserFilter;
import org.tigris.jira.core.model.filter.DateFilter;
import org.tigris.jira.core.model.filter.DateRangeFilter;
import org.tigris.jira.core.model.filter.FilterDefinition;
import org.tigris.jira.core.model.filter.IssueTypeFilter;
import org.tigris.jira.core.model.filter.NobodyFilter;
import org.tigris.jira.core.model.filter.PriorityFilter;
import org.tigris.jira.core.model.filter.ProjectFilter;
import org.tigris.jira.core.model.filter.ResolutionFilter;
import org.tigris.jira.core.model.filter.SpecificUserFilter;
import org.tigris.jira.core.model.filter.StatusFilter;
import org.tigris.jira.core.model.filter.UserFilter;
import org.tigris.jira.core.model.filter.UserInGroupFilter;
import org.tigris.jira.core.model.filter.VersionFilter;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Brock Janiczak
 * @author Eugene Kuleshov (layout and other improvements)
 * @author Mik Kersten (generalized for search dialog)
 */
@SuppressWarnings("unchecked")
public class JiraQueryPage extends AbstractRepositoryQueryPage {

	private static final String TITLE_PAGE = "JIRA Query";

	final Placeholder ANY_FIX_VERSION = new Placeholder("Any");

	final Placeholder NO_FIX_VERSION = new Placeholder("No Fix Version");

	final Placeholder ANY_REPORTED_VERSION = new Placeholder("Any");

	final Placeholder NO_REPORTED_VERSION = new Placeholder("No Version");

	final Placeholder UNRELEASED_VERSION = new Placeholder("Unreleased Versions");

	final Placeholder RELEASED_VERSION = new Placeholder("Released Versions");

	final Placeholder ANY_COMPONENT = new Placeholder("Any");

	final Placeholder NO_COMPONENT = new Placeholder("No Component");

	// attributes

	final Placeholder ANY_ISSUE_TYPE = new Placeholder("Any");

	final Placeholder ANY_RESOLUTION = new Placeholder("Any");

	final Placeholder UNRESOLVED = new Placeholder("Unresolved");

	final Placeholder UNASSIGNED = new Placeholder("Unassigned");

	final Placeholder ANY_REPORTER = new Placeholder("Any");

	final Placeholder NO_REPORTER = new Placeholder("No Reporter");

	final Placeholder CURRENT_USER_REPORTER = new Placeholder("Current User");

	final Placeholder SPECIFIC_USER_REPORTER = new Placeholder("Specified User");

	final Placeholder SPECIFIC_GROUP_REPORTER = new Placeholder("Specified Group");

	final Placeholder ANY_ASSIGNEE = new Placeholder("Any");

	final Placeholder CURRENT_USER_ASSIGNEE = new Placeholder("Current User");

	final Placeholder SPECIFIC_USER_ASSIGNEE = new Placeholder("Specified User");

	final Placeholder SPECIFIC_GROUP_ASSIGNEE = new Placeholder("Specified Group");

	final Placeholder ANY_STATUS = new Placeholder("Any");

	final Placeholder ANY_PRIORITY = new Placeholder("Any");

	private final JiraServer server;

	private ListViewer project;

	private ListViewer reportedIn;

	private ListViewer components;

	private ListViewer fixFor;

	private ListViewer issueType;

	private ListViewer status;

	private ListViewer resolution;

	private ListViewer priority;

	private ComboViewer assigneeType;

	private Text assignee;

	private ComboViewer reporterType;

	private Text reporter;

	private Text queryString;

	private Button searchSummary;

	private Button searchDescription;

	private Button searchComments;

	private Button searchEnvironment;

	private DatePicker dueStartDatePicker;

	private DatePicker dueEndDatePicker;

	private DatePicker updatedStartDatePicker;

	private DatePicker updatedEndDatePicker;

	private DatePicker createdStartDatePicker;

	private DatePicker createdEndDatePicker;

	private final boolean isNew;

	private final FilterDefinition workingCopy;

	private boolean namedQuery = false;

	public JiraQueryPage(TaskRepository repository, FilterDefinition workingCopy, boolean isNew, boolean namedQuery) {
		super(TITLE_PAGE);
		this.repository = repository;
		this.server = JiraServerFacade.getDefault().getJiraServer(repository);
		this.workingCopy = workingCopy;
		this.isNew = isNew;
		this.namedQuery = namedQuery;

		setDescription("Add search filters to define query.");
		setPageComplete(false);
	}

	public void createControl(final Composite parent) {
		Composite c = null;

		c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(3, false));
		setControl(c);
		
		if (namedQuery) {
			Label lblName = new Label(c, SWT.NONE);
			final GridData gridData = new GridData();
			lblName.setLayoutData(gridData);
			lblName.setText("Name:");

			title = new Text(c, SWT.BORDER);
			title.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			title.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validatePage();
				}

			});
		}

		SashForm sashForm = new SashForm(c, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1));

		{
			SashForm cc = new SashForm(sashForm, SWT.HORIZONTAL);

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout(1, false);
				gridLayout.marginWidth = 0;
				comp.setLayout(gridLayout);

				Label label = new Label(comp, SWT.NONE);
				label.setText("Project:");
				createProjectsViewer(comp);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout(1, false);
				gridLayout.marginWidth = 0;
				comp.setLayout(gridLayout);

				new Label(comp, SWT.NONE).setText("Fix For:");
				createFixForViewer(comp);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout(1, false);
				gridLayout.marginWidth = 0;
				comp.setLayout(gridLayout);

				new Label(comp, SWT.NONE).setText("In Components:");
				createComponentsViewer(comp);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout(1, false);
				gridLayout.marginWidth = 0;
				comp.setLayout(gridLayout);

				Label label = new Label(comp, SWT.NONE);
				label.setText("Reported In:");
				createReportedInViewer(comp);
			}
			cc.setWeights(new int[] { 1, 1, 1, 1 });
		}

		{
			SashForm cc = new SashForm(sashForm, SWT.NONE);

			ISelectionChangedListener selectionChangeListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					validatePage();
				}
			};

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout();
				gridLayout.marginWidth = 0;
				comp.setLayout(gridLayout);

				Label typeLabel = new Label(comp, SWT.NONE);
				typeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
				typeLabel.setText("Type:");

				issueType = new ListViewer(comp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
				issueType.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				issueType.setContentProvider(new IStructuredContentProvider() {

					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}

					public void dispose() {
					}

					public Object[] getElements(Object inputElement) {
						JiraServer server = (JiraServer) inputElement;
						Object[] elements = new Object[server.getIssueTypes().length + 1];
						elements[0] = ANY_ISSUE_TYPE;
						System.arraycopy(server.getIssueTypes(), 0, elements, 1, server.getIssueTypes().length);

						return elements;
					}
				});

				issueType.setLabelProvider(new LabelProvider() {

					public String getText(Object element) {
						if (element instanceof Placeholder) {
							return ((Placeholder) element).getText();
						}

						return ((IssueType) element).getName();
					}

				});

				issueType.addSelectionChangedListener(selectionChangeListener);

				issueType.setInput(server);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout();
				gridLayout.marginWidth = 0;
				comp.setLayout(gridLayout);

				Label statusLabel = new Label(comp, SWT.NONE);
				statusLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
				statusLabel.setText("Status:");

				status = new ListViewer(comp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
				status.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				status.setContentProvider(new IStructuredContentProvider() {

					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}

					public void dispose() {
					}

					public Object[] getElements(Object inputElement) {
						JiraServer server = (JiraServer) inputElement;
						Object[] elements = new Object[server.getStatuses().length + 1];
						elements[0] = ANY_STATUS;
						System.arraycopy(server.getStatuses(), 0, elements, 1, server.getStatuses().length);

						return elements;
					}
				});

				status.setLabelProvider(new LabelProvider() {

					public String getText(Object element) {
						if (element instanceof Placeholder) {
							return ((Placeholder) element).getText();
						}

						return ((Status) element).getName();
					}

				});

				status.addSelectionChangedListener(selectionChangeListener);
				status.setInput(server);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout();
				gridLayout.marginWidth = 0;
				comp.setLayout(gridLayout);

				Label resolutionLabel = new Label(comp, SWT.NONE);
				resolutionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
				resolutionLabel.setText("Resolution:");

				resolution = new ListViewer(comp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
				resolution.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				resolution.setContentProvider(new IStructuredContentProvider() {

					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}

					public void dispose() {
					}

					public Object[] getElements(Object inputElement) {
						JiraServer server = (JiraServer) inputElement;
						Object[] elements = new Object[server.getResolutions().length + 2];
						elements[0] = ANY_RESOLUTION;
						elements[1] = UNRESOLVED;
						System.arraycopy(server.getResolutions(), 0, elements, 2, server.getResolutions().length);

						return elements;
					}
				});

				resolution.setLabelProvider(new LabelProvider() {

					public String getText(Object element) {
						if (element instanceof Placeholder) {
							return ((Placeholder) element).getText();
						}

						return ((Resolution) element).getName();
					}

				});

				resolution.addSelectionChangedListener(selectionChangeListener);
				resolution.setInput(server);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout();
				gridLayout.marginWidth = 0;
				comp.setLayout(gridLayout);

				Label priorityLabel = new Label(comp, SWT.NONE);
				priorityLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
				priorityLabel.setText("Priority:");

				priority = new ListViewer(comp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
				priority.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				priority.setContentProvider(new IStructuredContentProvider() {

					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}

					public void dispose() {
					}

					public Object[] getElements(Object inputElement) {
						JiraServer server = (JiraServer) inputElement;
						Object[] elements = new Object[server.getPriorities().length + 1];
						elements[0] = ANY_PRIORITY;
						System.arraycopy(server.getPriorities(), 0, elements, 1, server.getPriorities().length);

						return elements;
					}
				});

				priority.setLabelProvider(new LabelProvider() {

					public String getText(Object element) {
						if (element instanceof Placeholder) {
							return ((Placeholder) element).getText();
						}

						return ((Priority) element).getName();
					}

				});
				priority.addSelectionChangedListener(selectionChangeListener);
				priority.setInput(server);
			}

			cc.setWeights(new int[] { 1, 1, 1, 1 });
		}
		sashForm.setWeights(new int[] { 1, 1 });

		Label lblQuery = new Label(c, SWT.NONE);
		lblQuery.setLayoutData(new GridData());
		lblQuery.setText("Query:");
		queryString = new Text(c, SWT.BORDER);
		queryString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		// TODO put content assist here and a label describing what is
		// available

		queryString.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
				validatePage();
			}

		});

		Label lblFields = new Label(c, SWT.NONE);
		lblFields.setText("Fields:");
		lblFields.setLayoutData(new GridData());

		{
			SelectionAdapter selectionAdapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					validatePage();
				}
			};

			Composite comp = new Composite(c, SWT.NONE);
			comp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			comp.setLayout(new FillLayout());

			searchSummary = new Button(comp, SWT.CHECK);
			searchSummary.setText("Summary");
			searchSummary.addSelectionListener(selectionAdapter);

			searchDescription = new Button(comp, SWT.CHECK);
			searchDescription.setText("Description");
			searchDescription.addSelectionListener(selectionAdapter);

			searchComments = new Button(comp, SWT.CHECK);
			searchComments.setText("Comments");
			searchComments.addSelectionListener(selectionAdapter);

			searchEnvironment = new Button(comp, SWT.CHECK);
			searchEnvironment.setText("Environment");
			searchEnvironment.addSelectionListener(selectionAdapter);
		}

		{
			Label reportedByLabel = new Label(c, SWT.NONE);
			reportedByLabel.setText("Reported By:");

			reporterType = new ComboViewer(c, SWT.BORDER | SWT.READ_ONLY);
			GridData gridData_1 = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gridData_1.widthHint = 133;
			reporterType.getControl().setLayoutData(gridData_1);

			reporterType.setContentProvider(new IStructuredContentProvider() {

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					return new Object[] { ANY_REPORTER, NO_REPORTER, CURRENT_USER_REPORTER, SPECIFIC_USER_REPORTER,
							SPECIFIC_GROUP_REPORTER };
				}

			});

			reporterType.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((Placeholder) element).getText();
				}
			});

			reporterType.setInput(server);

			reporter = new Text(c, SWT.BORDER);
			reporter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			reporter.setEnabled(false);

			reporter.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validatePage();
				}

			});
		}

		{
			Label assignedToLabel = new Label(c, SWT.NONE);
			assignedToLabel.setText("Assigned To:");

			assigneeType = new ComboViewer(c, SWT.BORDER | SWT.READ_ONLY);
			GridData gridData_2 = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gridData_2.widthHint = 133;
			assigneeType.getCombo().setLayoutData(gridData_2);

			assigneeType.setContentProvider(new IStructuredContentProvider() {

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					return new Object[] { ANY_ASSIGNEE, UNASSIGNED, CURRENT_USER_ASSIGNEE, SPECIFIC_USER_ASSIGNEE,
							SPECIFIC_GROUP_ASSIGNEE };
				}

			});

			assigneeType.setLabelProvider(new LabelProvider() {

				public String getText(Object element) {
					return ((Placeholder) element).getText();
				}

			});

			assigneeType.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (SPECIFIC_USER_ASSIGNEE.equals(selection) || SPECIFIC_GROUP_ASSIGNEE.equals(selection)) {
						assignee.setEnabled(true);
					} else {
						assignee.setEnabled(false);
						assignee.setText(""); //$NON-NLS-1$
					}
					validatePage();
				}

			});

			assigneeType.setInput(server);

			assignee = new Text(c, SWT.BORDER);
			assignee.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			assignee.setEnabled(false);
			assignee.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validatePage();
				}

			});
		}

		{
			Label createdLabel = new Label(c, SWT.NONE);
			createdLabel.setText("Created:");

			Composite composite = new Composite(c, SWT.NONE);
			FillLayout fillLayout = new FillLayout();
			fillLayout.spacing = 5;
			composite.setLayout(fillLayout);
			composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

			createdStartDatePicker = new DatePicker(composite, SWT.BORDER, "<start date>");
			createdEndDatePicker = new DatePicker(composite, SWT.BORDER, "<end date>");
		}

		{
			Label updatedLabel = new Label(c, SWT.NONE);
			updatedLabel.setText("Updated:");

			Composite composite = new Composite(c, SWT.NONE);
			FillLayout fillLayout = new FillLayout();
			fillLayout.spacing = 5;
			composite.setLayout(fillLayout);
			composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

			updatedStartDatePicker = new DatePicker(composite, SWT.BORDER, "<start date>");
			updatedEndDatePicker = new DatePicker(composite, SWT.BORDER, "<end date>");
		}

		{
			Label dueDateLabel = new Label(c, SWT.NONE);
			dueDateLabel.setText("Due Date:");

			Composite composite = new Composite(c, SWT.NONE);
			FillLayout fillLayout = new FillLayout();
			fillLayout.spacing = 5;
			composite.setLayout(fillLayout);
			composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

			dueStartDatePicker = new DatePicker(composite, SWT.BORDER, "<start date>");
			dueEndDatePicker = new DatePicker(composite, SWT.BORDER, "<end date>");
		}

		// new FillLayout()f validation here
		if (isNew) {
			loadFromDefaults();
		} else {
			loadFromWorkingCopy();
		}
	}

	private void createReportedInViewer(Composite c) {
		reportedIn = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 55;
		gridData.widthHint = 90;
		reportedIn.getControl().setLayoutData(gridData);

		reportedIn.setContentProvider(new IStructuredContentProvider() {
			private Project project;

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				project = (Project) newInput;
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (project != null) {
					Version[] versions = project.getVersions();
					Version[] releasedVersions = project.getReleasedVersions();
					Version[] unreleasedVersions = project.getUnreleasedVersions();

					Object[] elements = new Object[versions.length + 4];
					elements[0] = ANY_REPORTED_VERSION;
					elements[1] = NO_REPORTED_VERSION;
					elements[2] = RELEASED_VERSION;
					System.arraycopy(releasedVersions, 0, elements, 3, releasedVersions.length);

					elements[releasedVersions.length + 3] = UNRELEASED_VERSION;

					System.arraycopy(unreleasedVersions, 0, elements, releasedVersions.length + 4,
							unreleasedVersions.length);
					return elements;
				}
				return new Object[] { ANY_REPORTED_VERSION };
			}

		});
		reportedIn.setLabelProvider(new VersionLabelProvider());
		reportedIn.setInput(null);
	}

	private void createComponentsViewer(Composite c) {
		components = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 63;
		gridData.widthHint = 90;
		components.getControl().setLayoutData(gridData);

		components.setContentProvider(new IStructuredContentProvider() {
			private Project project;

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				project = (Project) newInput;
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (project != null) {
					Component[] components = project.getComponents();

					Object[] elements = new Object[components.length + 2];
					elements[0] = ANY_COMPONENT;
					elements[1] = NO_COMPONENT;
					System.arraycopy(components, 0, elements, 2, components.length);
					return elements;
				}
				return new Object[] { ANY_COMPONENT };
			}

		});
		components.setLabelProvider(new ComponentLabelProvider());
		components.setInput(null);
	}

	private void createFixForViewer(Composite c) {
		fixFor = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 57;
		gridData.widthHint = 90;
		fixFor.getControl().setLayoutData(gridData);

		fixFor.setContentProvider(new IStructuredContentProvider() {
			private Project project;

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				project = (Project) newInput;
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (project != null) {
					Version[] versions = project.getVersions();
					Version[] releasedVersions = project.getReleasedVersions();
					Version[] unreleasedVersions = project.getUnreleasedVersions();

					Object[] elements = new Object[versions.length + 4];
					elements[0] = ANY_FIX_VERSION;
					elements[1] = NO_FIX_VERSION;
					elements[2] = RELEASED_VERSION;
					System.arraycopy(releasedVersions, 0, elements, 3, releasedVersions.length);

					elements[releasedVersions.length + 3] = UNRELEASED_VERSION;

					System.arraycopy(unreleasedVersions, 0, elements, releasedVersions.length + 4,
							unreleasedVersions.length);
					return elements;
				}
				return new Object[] { ANY_REPORTED_VERSION };
			}

		});
		fixFor.setLabelProvider(new VersionLabelProvider());
		fixFor.setInput(null);
	}

	private void createProjectsViewer(Composite c) {
		project = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 59;
		gridData.widthHint = 90;
		project.getControl().setLayoutData(gridData);

		project.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				JiraServer server = (JiraServer) inputElement;
				Object[] elements = new Object[server.getProjects().length + 1];
				elements[0] = new Placeholder("All Projects");
				System.arraycopy(server.getProjects(), 0, elements, 1, server.getProjects().length);
				return elements;
			}

		});

		project.setLabelProvider(new LabelProvider() {

			public String getText(Object element) {
				if (element instanceof Placeholder) {
					return ((Placeholder) element).getText();
				}

				return ((Project) element).getName();
			}

		});

		project.setInput(server);
		project.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = ((IStructuredSelection) event.getSelection());
				Project selectedProject = null;
				if (!selection.isEmpty() && !(selection.getFirstElement() instanceof Placeholder)) {
					selectedProject = (Project) selection.getFirstElement();
				}

				updateCurrentProject(selectedProject);
				validatePage();
			}

		});
	}

	void updateCurrentProject(Project project) {
		this.fixFor.setInput(project);
		this.components.setInput(project);
		this.reportedIn.setInput(project);

	}

	// public boolean isPageComplete() {
	// if (namedQuery && name != null && name.getText().length() == 0) {
	// return false;
	// } else {
	// return true;
	// }
	// }

	void validatePage() {
		if (namedQuery && super.isPageComplete()) {
			setErrorMessage("Name is mandatory");
			setPageComplete(false);
			return;
		} else {
			setPageComplete(true);
			setErrorMessage(null);
		}
	}

	private void loadFromDefaults() {
		project.setSelection(new StructuredSelection(new Placeholder("All Projects")));
		searchSummary.setSelection(true);
		searchDescription.setSelection(true);

		issueType.setSelection(new StructuredSelection(ANY_ISSUE_TYPE));
		reporterType.setSelection(new StructuredSelection(ANY_REPORTER));
		assigneeType.setSelection(new StructuredSelection(ANY_ASSIGNEE));
		status.setSelection(new StructuredSelection(ANY_STATUS));
		resolution.setSelection(new StructuredSelection(ANY_RESOLUTION));
		priority.setSelection(new StructuredSelection(ANY_PRIORITY));
	}

	private void loadFromWorkingCopy() {
		if (namedQuery && workingCopy.getName() != null) {
			title.setText(workingCopy.getName());
		}

		if (workingCopy.getDescription() != null) {
		}

		if (workingCopy.getProjectFilter() != null) {
			project.setSelection(new StructuredSelection(workingCopy.getProjectFilter().getProject()));
		} else {
			project.setSelection(new StructuredSelection(new Placeholder("All Projects")));
		}

		if (workingCopy.getFixForVersionFilter() != null) {
			if (workingCopy.getFixForVersionFilter().hasNoVersion()) {
				fixFor.setSelection(new StructuredSelection(new Object[] { NO_FIX_VERSION }));
			} else if (workingCopy.getFixForVersionFilter().isReleasedVersions()
					&& workingCopy.getFixForVersionFilter().isUnreleasedVersions()) {
				fixFor.setSelection(new StructuredSelection(new Object[] { RELEASED_VERSION, UNRELEASED_VERSION }));
			} else if (workingCopy.getFixForVersionFilter().isReleasedVersions()) {
				fixFor.setSelection(new StructuredSelection(RELEASED_VERSION));
			} else if (workingCopy.getFixForVersionFilter().isUnreleasedVersions()) {
				fixFor.setSelection(new StructuredSelection(UNRELEASED_VERSION));
			} else {
				fixFor.setSelection(new StructuredSelection(workingCopy.getFixForVersionFilter().getVersions()));
			}
		} else {
			fixFor.setSelection(new StructuredSelection(ANY_FIX_VERSION));
		}

		if (workingCopy.getReportedInVersionFilter() != null) {
			if (workingCopy.getReportedInVersionFilter().hasNoVersion()) {
				reportedIn.setSelection(new StructuredSelection(new Object[] { NO_REPORTED_VERSION }));
			} else if (workingCopy.getReportedInVersionFilter().isReleasedVersions()
					&& workingCopy.getReportedInVersionFilter().isUnreleasedVersions()) {
				reportedIn.setSelection(new StructuredSelection(new Object[] { RELEASED_VERSION, UNRELEASED_VERSION }));
			} else if (workingCopy.getReportedInVersionFilter().isReleasedVersions()) {
				reportedIn.setSelection(new StructuredSelection(RELEASED_VERSION));
			} else if (workingCopy.getReportedInVersionFilter().isUnreleasedVersions()) {
				reportedIn.setSelection(new StructuredSelection(UNRELEASED_VERSION));
			} else {
				reportedIn
						.setSelection(new StructuredSelection(workingCopy.getReportedInVersionFilter().getVersions()));
			}
		} else {
			reportedIn.setSelection(new StructuredSelection(ANY_REPORTED_VERSION));
		}

		if (workingCopy.getContentFilter() != null) {
			this.queryString.setText(workingCopy.getContentFilter().getQueryString());
			this.searchComments.setSelection(workingCopy.getContentFilter().isSearchingComments());
			this.searchDescription.setSelection(workingCopy.getContentFilter().isSearchingDescription());
			this.searchEnvironment.setSelection(workingCopy.getContentFilter().isSearchingEnvironment());
			this.searchSummary.setSelection(workingCopy.getContentFilter().isSearchingSummary());
		}

		if (workingCopy.getComponentFilter() != null) {
			if (workingCopy.getComponentFilter().hasNoComponent()) {
				components.setSelection(new StructuredSelection(NO_COMPONENT));
			} else {
				components.setSelection(new StructuredSelection(workingCopy.getComponentFilter().getComponents()));
			}
		} else {
			components.setSelection(new StructuredSelection(ANY_COMPONENT));
		}

		// attributes

		if (workingCopy.getIssueTypeFilter() != null) {
			issueType.setSelection(new StructuredSelection(workingCopy.getIssueTypeFilter().getIsueTypes()));
		} else {
			issueType.setSelection(new StructuredSelection(ANY_ISSUE_TYPE));
		}

		if (workingCopy.getReportedByFilter() != null) {
			UserFilter reportedByFilter = workingCopy.getReportedByFilter();
			if (reportedByFilter instanceof NobodyFilter) {
				reporterType.setSelection(new StructuredSelection(NO_REPORTER));
			} else if (reportedByFilter instanceof CurrentUserFilter) {
				reporterType.setSelection(new StructuredSelection(CURRENT_USER_REPORTER));
			} else if (reportedByFilter instanceof SpecificUserFilter) {
				reporterType.setSelection(new StructuredSelection(SPECIFIC_USER_REPORTER));
				reporter.setText(((SpecificUserFilter) reportedByFilter).getUser());
			} else if (reportedByFilter instanceof UserInGroupFilter) {
				reporterType.setSelection(new StructuredSelection(SPECIFIC_GROUP_REPORTER));
				reporter.setText(((UserInGroupFilter) reportedByFilter).getGroup());
			}
		} else {
			reporterType.setSelection(new StructuredSelection(ANY_REPORTER));
		}

		if (workingCopy.getAssignedToFilter() != null) {
			UserFilter assignedToFilter = workingCopy.getAssignedToFilter();
			if (assignedToFilter instanceof NobodyFilter) {
				assigneeType.setSelection(new StructuredSelection(UNASSIGNED));
			} else if (assignedToFilter instanceof CurrentUserFilter) {
				assigneeType.setSelection(new StructuredSelection(CURRENT_USER_ASSIGNEE));
			} else if (assignedToFilter instanceof SpecificUserFilter) {
				assigneeType.setSelection(new StructuredSelection(SPECIFIC_USER_ASSIGNEE));
				assignee.setText(((SpecificUserFilter) assignedToFilter).getUser());
			} else if (assignedToFilter instanceof UserInGroupFilter) {
				assigneeType.setSelection(new StructuredSelection(SPECIFIC_GROUP_ASSIGNEE));
				assignee.setText(((UserInGroupFilter) assignedToFilter).getGroup());
			}
		} else {
			assigneeType.setSelection(new StructuredSelection(ANY_ASSIGNEE));
		}

		if (workingCopy.getStatusFilter() != null) {
			status.setSelection(new StructuredSelection(workingCopy.getStatusFilter().getStatuses()));
		} else {
			status.setSelection(new StructuredSelection(ANY_STATUS));
		}

		if (workingCopy.getResolutionFilter() != null) {
			Resolution[] resolutions = workingCopy.getResolutionFilter().getResolutions();
			if (resolutions.length == 0) {
				resolution.setSelection(new StructuredSelection(UNRESOLVED));
			} else {
				resolution.setSelection(new StructuredSelection(resolutions));
			}
		} else {
			resolution.setSelection(new StructuredSelection(ANY_RESOLUTION));
		}

		if (workingCopy.getPriorityFilter() != null) {
			priority.setSelection(new StructuredSelection(workingCopy.getPriorityFilter().getPriorities()));
		} else {
			priority.setSelection(new StructuredSelection(ANY_PRIORITY));
		}

		setDateRange(workingCopy.getCreatedDateFilter(), createdStartDatePicker, createdEndDatePicker);
		setDateRange(workingCopy.getUpdatedDateFilter(), updatedStartDatePicker, updatedEndDatePicker);
		setDateRange(workingCopy.getDueDateFilter(), dueStartDatePicker, dueEndDatePicker);
	}

	private void setDateRange(DateFilter dateFilter, DatePicker startDatePicker, DatePicker endDatePicker) {
		if (dateFilter instanceof DateRangeFilter) {
			DateRangeFilter rangeFilter = (DateRangeFilter) dateFilter;
			Calendar c1 = Calendar.getInstance();
			c1.setTime(rangeFilter.getFromDate());
			startDatePicker.setDate(c1);

			Calendar c2 = Calendar.getInstance();
			c2.setTime(rangeFilter.getToDate());
			endDatePicker.setDate(c2);
		}
	}

	void applyChanges() {
		if (namedQuery) {
			workingCopy.setName(this.title.getText());
		}
		if (this.queryString.getText().length() > 0 || this.searchSummary.getSelection()
				|| this.searchDescription.getSelection() || this.searchEnvironment.getSelection()
				|| this.searchComments.getSelection()) {
			workingCopy.setContentFilter(new ContentFilter(this.queryString.getText(), this.searchSummary
					.getSelection(), this.searchDescription.getSelection(), this.searchEnvironment.getSelection(),
					this.searchComments.getSelection()));
		} else {
			workingCopy.setContentFilter(null);
		}

		IStructuredSelection projectSelection = (IStructuredSelection) this.project.getSelection();
		if (!projectSelection.isEmpty() && projectSelection.getFirstElement() instanceof Project) {
			workingCopy.setProjectFilter(new ProjectFilter((Project) projectSelection.getFirstElement()));
		} else {
			workingCopy.setProjectFilter(null);
		}

		IStructuredSelection reportedInSelection = (IStructuredSelection) reportedIn.getSelection();
		if (reportedInSelection.isEmpty()) {
			workingCopy.setReportedInVersionFilter(null);
		} else {
			boolean selectionContainsReleased = false;
			boolean selectionContainsUnreleased = false;
			boolean selectionContainsAll = false;
			boolean selectionContainsNone = false;

			List<Version> selectedVersions = new ArrayList<Version>();

			for (Iterator i = reportedInSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_REPORTED_VERSION.equals(selection)) {
					selectionContainsAll = true;
				} else if (NO_REPORTED_VERSION.equals(selection)) {
					selectionContainsNone = true;
				} else if (RELEASED_VERSION.equals(selection)) {
					selectionContainsReleased = true;
				} else if (UNRELEASED_VERSION.equals(selection)) {
					selectionContainsUnreleased = true;
				} else if (selection instanceof Version) {
					selectedVersions.add((Version) selection);
				}
			}

			if (selectionContainsAll) {
				workingCopy.setReportedInVersionFilter(null);
			} else if (selectionContainsNone) {
				workingCopy.setReportedInVersionFilter(new VersionFilter(new Version[0]));
			} else if (selectionContainsReleased || selectionContainsUnreleased) {
				workingCopy.setReportedInVersionFilter(new VersionFilter(selectionContainsReleased,
						selectionContainsUnreleased));
			} else if (selectedVersions.size() > 0) {
				workingCopy.setReportedInVersionFilter(new VersionFilter(selectedVersions
						.toArray(new Version[selectedVersions.size()])));
			} else {
				workingCopy.setReportedInVersionFilter(null);
			}
		}

		IStructuredSelection fixForSelection = (IStructuredSelection) fixFor.getSelection();
		if (fixForSelection.isEmpty()) {
			workingCopy.setFixForVersionFilter(null);
		} else {
			boolean selectionContainsReleased = false;
			boolean selectionContainsUnreleased = false;
			boolean selectionContainsAll = false;
			boolean selectionContainsNone = false;

			List<Version> selectedVersions = new ArrayList<Version>();

			for (Iterator i = fixForSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_FIX_VERSION.equals(selection)) {
					selectionContainsAll = true;
				} else if (NO_FIX_VERSION.equals(selection)) {
					selectionContainsNone = true;
				} else if (RELEASED_VERSION.equals(selection)) {
					selectionContainsReleased = true;
				} else if (UNRELEASED_VERSION.equals(selection)) {
					selectionContainsUnreleased = true;
				} else if (selection instanceof Version) {
					selectedVersions.add((Version) selection);
				}
			}

			if (selectionContainsAll) {
				workingCopy.setFixForVersionFilter(null);
			} else if (selectionContainsNone) {
				workingCopy.setFixForVersionFilter(new VersionFilter(new Version[0]));
			} else if (selectionContainsReleased || selectionContainsUnreleased) {
				workingCopy.setFixForVersionFilter(new VersionFilter(selectionContainsReleased,
						selectionContainsUnreleased));
			} else if (selectedVersions.size() > 0) {
				workingCopy.setFixForVersionFilter(new VersionFilter(selectedVersions
						.toArray(new Version[selectedVersions.size()])));
			} else {
				workingCopy.setFixForVersionFilter(null);
			}
		}

		IStructuredSelection componentsSelection = (IStructuredSelection) components.getSelection();
		if (componentsSelection.isEmpty()) {
			workingCopy.setComponentFilter(null);
		} else {

			boolean selectionContainsAll = false;
			boolean selectionContainsNone = false;
			List<Component> selectedComponents = new ArrayList<Component>();

			for (Iterator i = componentsSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_COMPONENT.equals(selection)) {
					selectionContainsAll = true;
				} else if (NO_COMPONENT.equals(selection)) {
					selectionContainsNone = true;
				} else if (selection instanceof Component) {
					selectedComponents.add((Component) selection);
				}
			}

			if (selectionContainsAll) {
				workingCopy.setComponentFilter(null);
			} else if (selectedComponents.size() > 0) {
				workingCopy.setComponentFilter(new ComponentFilter(selectedComponents
						.toArray(new Component[selectedComponents.size()])));
			} else if (selectionContainsNone) {
				workingCopy.setComponentFilter(new ComponentFilter(new Component[0]));
			} else {
				workingCopy.setComponentFilter(null);
			}
		}

		// attributes

		// TODO support standard and subtask issue types
		IStructuredSelection issueTypeSelection = (IStructuredSelection) issueType.getSelection();
		if (issueTypeSelection.isEmpty()) {
			workingCopy.setIssueTypeFilter(null);
		} else {
			boolean isAnyIssueTypeSelected = false;

			List<IssueType> selectedIssueTypes = new ArrayList<IssueType>();

			for (Iterator i = issueTypeSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_ISSUE_TYPE.equals(selection)) {
					isAnyIssueTypeSelected = true;
				} else if (selection instanceof IssueType) {
					selectedIssueTypes.add((IssueType) selection);
				}
			}

			if (isAnyIssueTypeSelected) {
				workingCopy.setIssueTypeFilter(null);
			} else {
				workingCopy.setIssueTypeFilter(new IssueTypeFilter(selectedIssueTypes
						.toArray(new IssueType[selectedIssueTypes.size()])));
			}
		}

		IStructuredSelection reporterSelection = (IStructuredSelection) reporterType.getSelection();
		if (reporterSelection.isEmpty()) {
			workingCopy.setReportedByFilter(null);
		} else {
			if (ANY_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(null);
			} else if (NO_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(new NobodyFilter());
			} else if (CURRENT_USER_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(new CurrentUserFilter());
			} else if (SPECIFIC_GROUP_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(new UserInGroupFilter(reporter.getText()));
			} else if (SPECIFIC_USER_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(new SpecificUserFilter(reporter.getText()));
			} else {
				workingCopy.setReportedByFilter(null);
			}
		}

		IStructuredSelection assigneeSelection = (IStructuredSelection) assigneeType.getSelection();
		if (assigneeSelection.isEmpty()) {
			workingCopy.setAssignedToFilter(null);
		} else {
			if (ANY_REPORTER.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(null);
			} else if (UNASSIGNED.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(new NobodyFilter());
			} else if (CURRENT_USER_REPORTER.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(new CurrentUserFilter());
			} else if (SPECIFIC_GROUP_REPORTER.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(new UserInGroupFilter(assignee.getText()));
			} else if (SPECIFIC_USER_REPORTER.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(new SpecificUserFilter(assignee.getText()));
			} else {
				workingCopy.setAssignedToFilter(null);
			}
		}

		IStructuredSelection statusSelection = (IStructuredSelection) status.getSelection();
		if (statusSelection.isEmpty()) {
			workingCopy.setStatusFilter(null);
		} else {
			boolean isAnyStatusSelected = false;

			List<Status> selectedStatuses = new ArrayList<Status>();

			for (Iterator i = statusSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_STATUS.equals(selection)) {
					isAnyStatusSelected = true;
				} else if (selection instanceof Status) {
					selectedStatuses.add((Status) selection);
				}
			}

			if (isAnyStatusSelected) {
				workingCopy.setStatusFilter(null);
			} else {
				workingCopy.setStatusFilter(new StatusFilter(selectedStatuses.toArray(new Status[selectedStatuses
						.size()])));
			}
		}

		IStructuredSelection resolutionSelection = (IStructuredSelection) resolution.getSelection();
		if (resolutionSelection.isEmpty()) {
			workingCopy.setResolutionFilter(null);
		} else {
			boolean isAnyResolutionSelected = false;

			List<Resolution> selectedResolutions = new ArrayList<Resolution>();

			for (Iterator i = resolutionSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_RESOLUTION.equals(selection)) {
					isAnyResolutionSelected = true;
				} else if (selection instanceof Resolution) {
					selectedResolutions.add((Resolution) selection);
				}
			}

			if (isAnyResolutionSelected) {
				workingCopy.setResolutionFilter(null);
			} else {
				workingCopy.setResolutionFilter(new ResolutionFilter(selectedResolutions
						.toArray(new Resolution[selectedResolutions.size()])));
			}
		}

		IStructuredSelection prioritySelection = (IStructuredSelection) priority.getSelection();
		if (prioritySelection.isEmpty()) {
			workingCopy.setPriorityFilter(null);
		} else {
			boolean isAnyPrioritiesSelected = false;

			List<Priority> selectedPriorities = new ArrayList<Priority>();

			for (Iterator i = prioritySelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_PRIORITY.equals(selection)) {
					isAnyPrioritiesSelected = true;
				} else if (selection instanceof Priority) {
					selectedPriorities.add((Priority) selection);
				}
			}

			if (isAnyPrioritiesSelected) {
				workingCopy.setPriorityFilter(null);
			} else {
				workingCopy.setPriorityFilter(new PriorityFilter(selectedPriorities
						.toArray(new Priority[selectedPriorities.size()])));
			}
		}

		workingCopy.setDueDateFilter(getRangeFilter(dueStartDatePicker, dueEndDatePicker));

		workingCopy.setCreatedDateFilter(getRangeFilter(createdStartDatePicker, createdEndDatePicker));

		workingCopy.setUpdatedDateFilter(getRangeFilter(updatedStartDatePicker, updatedEndDatePicker));
	}

	private DateRangeFilter getRangeFilter(DatePicker startDatePicker, DatePicker endDatePicker) {
		Calendar startDate = startDatePicker.getDate();
		Calendar endDate = endDatePicker.getDate();
		if (startDate != null && endDate != null) {
			return new DateRangeFilter(startDate.getTime(), endDate.getTime());
		}
		return null;
	}

	final static class ComponentLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if (element instanceof Placeholder) {
				return ((Placeholder) element).getText();
			}
			return ((Component) element).getName();
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

	}

	final static class VersionLabelProvider implements ILabelProvider, IColorProvider {

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if (element instanceof Placeholder) {
				return ((Placeholder) element).getText();
			}
			return ((Version) element).getName();
		}

		public void addListener(ILabelProviderListener listener) {

		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Color getForeground(Object element) {
			return null;
		}

		public Color getBackground(Object element) {
			if (element instanceof Placeholder) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			}
			return null;
		}

	}

	private static final class Placeholder {
		private final String text;

		public Placeholder(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

	public AbstractRepositoryQuery getQuery() {
		this.applyChanges();
		if (isNew) {
			server.addLocalFilter(workingCopy);
		}

		String url = repository.getUrl();
		return new JiraCustomQuery(url, workingCopy, TasksUiPlugin.getTaskListManager().getTaskList(), TasksUiPlugin
				.getRepositoryManager().getRepository(JiraUiPlugin.REPOSITORY_KIND, url));
	}
}