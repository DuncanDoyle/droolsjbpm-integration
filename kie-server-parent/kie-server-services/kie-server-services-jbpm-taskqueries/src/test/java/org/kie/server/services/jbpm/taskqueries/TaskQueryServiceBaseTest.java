package org.kie.server.services.jbpm.taskqueries;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.junit.Test;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.taskqueries.util.TaskQueriesStrategy;
import org.mockito.Mockito;

public class TaskQueryServiceBaseTest {

	
	
	@Test
	public void testGetHumanTasksWithFilters() {
		QueryService queryServiceMock = Mockito.mock(QueryService.class);
		KieServerRegistry contextMock = Mockito.mock(KieServerRegistry.class);
		KieServerConfig configMock = Mockito.mock(KieServerConfig.class);
		when(contextMock.getConfig()).thenReturn(configMock);
		TaskQueriesStrategy taskQueriesStrategyMock = Mockito.mock(TaskQueriesStrategy.class); 
		
		TaskQueryServiceBase base = new TaskQueryServiceBase(queryServiceMock, contextMock, taskQueriesStrategyMock);
		
		Integer page = new Integer(0);
		Integer pageSize = new Integer(10);
		String payload = getPayload();
		String marshallingType = "JAXB";
		
		Collection<UserTaskInstanceWithVarsDesc> userTaskInstances = getUserTaskInstances();
		
		when(queryServiceMock.query(any(), any(), any(), any(QueryParam.class))).thenReturn(userTaskInstances);
		
		TaskInstanceList taskInstances = base.getHumanTasksWithFilters(page, pageSize, payload, marshallingType);
		
		//TODO: Implement the same logic with JUnit AssertThat
		assertEquals(1, taskInstances.getItems().size());
		
		TaskInstance ti1 = taskInstances.getItems().stream().findFirst().get();
		
		assertEquals("ddoyle", ti1.getActualOwner());
		assertEquals("RESERVED", ti1.getStatus());
		assertEquals("mswiderski", ti1.getCreatedBy());
	}
	
	private String getPayload() {
		StringBuilder payloadBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"); 
		payloadBuilder.append("<task-query-filter-spec>");
		payloadBuilder.append("<order-asc>false</order-asc>");
		payloadBuilder.append("<query-params>");
		payloadBuilder.append("<cond-column>DEPLOYMENTID</cond-column>");
		payloadBuilder.append("<cond-operator>EQUALS_TO</cond-operator>");
		payloadBuilder.append("<cond-values xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">simple-project</cond-values>");
		payloadBuilder.append("</query-params>");
		payloadBuilder.append("</task-query-filter-spec>");
		return payloadBuilder.toString();
	}
	
	private Collection<UserTaskInstanceWithVarsDesc> getUserTaskInstances() {
		List<UserTaskInstanceWithVarsDesc> tasks = new ArrayList<>();
		
		UserTaskInstanceWithVarsDesc task1 = new org.jbpm.kie.services.impl.model.UserTaskInstanceWithVarsDesc(new Long(1L), "RESERVED", new Date(), "test-task", "Test task", new Integer(1), "ddoyle", "mswiderski", "test-deployment", "test-process", new Long(1L), new Date(), new Date());
		
		tasks.add(task1);
		
		return tasks;
	}
	
}
