/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.jira.tests;

import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylar.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylar.internal.jira.ui.JiraQueryHit;
import org.eclipse.mylar.internal.jira.ui.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.ui.JiraRepositoryQuery;
import org.eclipse.mylar.internal.jira.ui.JiraTask;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.internal.tasks.core.RepositoryTaskHandleUtil;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.TaskList;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TaskListManager;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraTaskExternalizationTest extends TestCase {

	private static final String ISSUE_SUMMARY = "Issue Summary";

	private static final String ISSUE_DESCRIPTION = "Issue Description";

	private static final String ISSUE_KEY = "Issue Key";

	private final static String USER = "mylartest";

	private final static String PASSWORD = "mylartest";

	private final static String SERVER_URL = "http://developer.atlassian.com/jira";

	private static final String TEST_LABEL = "TestLabel";

	private static final String TEST_TASK = "TestTask";

	private TaskListManager manager = TasksUiPlugin.getTaskListManager();

	private TaskRepository repository = null;

	private TaskList taskList;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		repository = new TaskRepository(JiraUiPlugin.REPOSITORY_KIND, SERVER_URL);
		repository.setAuthenticationCredentials(USER, PASSWORD);
		TasksUiPlugin.getRepositoryManager().addRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());
		TasksUiPlugin.getTaskListManager().resetTaskList();		
		taskList = TasksUiPlugin.getTaskListManager().getTaskList();
		TasksUiPlugin.getTaskListManager().saveTaskList();
	}

	@Override
	protected void tearDown() throws Exception {
		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraUiPlugin.REPOSITORY_KIND);
		assertNotNull(client);
		// taskList.clearArchive();
		// MylarTaskListPlugin.getTaskListManager().getTaskList().clear();
		TasksUiPlugin.getTaskListManager().resetTaskList();
		TasksUiPlugin.getRepositoryManager().removeRepository(repository, TasksUiPlugin.getDefault().getRepositoriesFilePath());
		super.tearDown();
	}

	public void testNamedFilterNotRenamed() {
		NamedFilter filter = new NamedFilter();
		filter.setName("f-name");
		JiraRepositoryQuery query = new JiraRepositoryQuery(repository.getUrl(), filter,
				TasksUiPlugin.getTaskListManager().getTaskList());
		taskList.addQuery(query);
		query.setDescription("q-name");

		assertEquals(1, taskList.getQueries().size());
		assertEquals("q-name", taskList.getQueries().iterator().next().getSummary());
		
		manager.saveTaskList();
		manager.resetTaskList();
		manager.readExistingOrCreateNewList();
		
		assertEquals(1, taskList.getQueries().size());
		assertEquals("f-name", taskList.getQueries().iterator().next().getSummary());
	}
	
	public void testCustomQueryRename() {
		FilterDefinition filter = new FilterDefinition();
		filter.setName("f-name");
		JiraCustomQuery query = new JiraCustomQuery(repository.getUrl(), filter,
				TasksUiPlugin.getTaskListManager().getTaskList(), repository);
		taskList.addQuery(query);
		query.setDescription("q-name");

		assertEquals(1, taskList.getQueries().size());
		assertEquals("q-name", taskList.getQueries().iterator().next().getSummary());
		
		manager.saveTaskList();
		manager.resetTaskList();
		manager.readExistingOrCreateNewList();
		
		assertEquals(1, taskList.getQueries().size());
		assertEquals("q-name", taskList.getQueries().iterator().next().getSummary());
	}
	
	public void testCompletionSave() {
		JiraTask jiraTask = new JiraTask(SERVER_URL, TEST_TASK, TEST_LABEL, true);
		jiraTask.setCompleted(true);
		manager.getTaskList().moveToRoot(jiraTask);

		manager.saveTaskList();
		manager.resetTaskList();
		manager.readExistingOrCreateNewList();
		ITask task = manager.getTaskList().getTask(SERVER_URL, TEST_TASK);
		assertTrue(task.isCompleted());
	}

	public void testJiraTaskSave() {
		JiraTask jiraTask = new JiraTask(SERVER_URL+"testSave", TEST_TASK, TEST_LABEL, true);
		String testUrl = "http://foo";
		jiraTask.setTaskUrl(testUrl);
		manager.getTaskList().moveToRoot(jiraTask);

		manager.saveTaskList();
		manager.resetTaskList();
		manager.readExistingOrCreateNewList();
		Collection<ITask> taskSet = manager.getTaskList().getAllTasks();

		
		boolean taskFound = false;
		for (ITask currTask : taskSet) {
			if (currTask instanceof JiraTask && ((JiraTask)currTask).getRepositoryUrl().equals(SERVER_URL+"testSave")) {
				taskFound = true;
				// Check that the URL of the Jira task is it's handle
				assertEquals(testUrl, currTask.getTaskUrl());
				break;
			}
		}
		assertTrue("The saved Jira task was not found", taskFound);
	}

	public void testJiraFilterHitSave() {
		NamedFilter namedFilter = new NamedFilter();
		namedFilter.setName("Test Filter");
		namedFilter.setId("123456");
		namedFilter.setDescription("Test Filter Description");
		JiraRepositoryQuery jiraRepositoryQuery = new JiraRepositoryQuery(repository.getUrl(), namedFilter,
				TasksUiPlugin.getTaskListManager().getTaskList());
		String filterUrl = jiraRepositoryQuery.getUrl();

		Issue jiraIssue = new Issue();
		jiraIssue.setKey(ISSUE_KEY);
		jiraIssue.setDescription(ISSUE_DESCRIPTION);
		jiraIssue.setSummary(ISSUE_SUMMARY);
		JiraTask jiraTask = new JiraTask(SERVER_URL, ""+123, ISSUE_DESCRIPTION, true);
		taskList.addTask(jiraTask);
		JiraRepositoryConnector.updateTaskDetails(repository.getUrl(), jiraTask, jiraIssue, true);
		TasksUiPlugin.getTaskListManager().getTaskList().addTask(jiraTask);
		JiraQueryHit jiraHit = new JiraQueryHit(taskList, jiraTask.getSummary(), repository.getUrl(), "123", jiraTask.getKey(), false);
		jiraHit.setCorrespondingTask(jiraTask);
		assertNotNull(taskList.getTask(jiraHit.getHandleIdentifier()));
		jiraRepositoryQuery.addHit(jiraHit);
		TasksUiPlugin.getTaskListManager().getTaskList().addQuery(jiraRepositoryQuery);
		assertNotNull(taskList.getTask(jiraHit.getHandleIdentifier()));

		manager.saveTaskList();
		manager.resetTaskList();
		manager.readExistingOrCreateNewList();

		Set<AbstractRepositoryQuery> queries = manager.getTaskList().getQueries();
		JiraRepositoryQuery savedFilter = null;
		for (AbstractRepositoryQuery query : queries) {
			if (query.getHandleIdentifier().equals(jiraRepositoryQuery.getHandleIdentifier())) {
				savedFilter = (JiraRepositoryQuery) query;
				break;
			}
		}

		assertNotNull(savedFilter);
		assertEquals(savedFilter.getUrl(), filterUrl);

		assertTrue(savedFilter.getHits().size() > 0);

		JiraQueryHit savedHit = (JiraQueryHit) savedFilter.getHits().iterator().next();
		JiraTask jTask = (JiraTask) savedHit.getCorrespondingTask();
		
		assertEquals(jiraIssue.getKey(), jTask.getKey());
		assertEquals(jiraIssue.getSummary(), jTask.getSummary());
		
		String handle = RepositoryTaskHandleUtil.getHandle(jiraHit.getRepositoryUrl(), "123");
		assertEquals(handle, jTask.getHandleIdentifier());
		
		AbstractRepositoryConnector client = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				JiraUiPlugin.REPOSITORY_KIND);  
		assertNotNull(client);
		assertNotNull(taskList.getTask(savedHit.getHandleIdentifier()));
	}
}
