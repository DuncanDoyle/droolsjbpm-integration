/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.jbpm.queries;

import static javaslang.API.*;
import static org.kie.server.services.jbpm.ConvertUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryMapperRegistry;
import org.jbpm.services.api.query.QueryResultMapper;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.TaskSummary;
import org.kie.server.jbpm.queries.api.model.definition.BaseQueryFilterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javaslang.Tuple;
import javaslang.Tuple2;

/**
 * Template which provides functionality to do a query via {@link QueryService}.
 * <p>
 * Users of the template need to provide a {@link QueryCallback} and a {@link RequestCallback}, which provide the context in which to
 * execute the query.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class QueryServiceTemplate {

	private static final Logger logger = LoggerFactory.getLogger(QueryServiceTemplate.class);

	private QueryService queryService;

	public QueryServiceTemplate(QueryService queryService) {
		this.queryService = queryService;
	}

	public <T> T getWithFilters(Integer page, Integer pageSize, QueryCallback queryCallback, RequestCallback reqCallback) {

		QueryParam[] params = new QueryParam[0];
		Map<String, String> columnMapping = null;
		QueryContext queryContext = buildQueryContext(page, pageSize);

		BaseQueryFilterSpec filterSpec = reqCallback.getQueryFilterSpec();
		if (filterSpec != null) {
			queryContext.setOrderBy(filterSpec.getOrderBy());
			queryContext.setAscending(filterSpec.isAscending());

			// build parameters for filtering the query
			if (filterSpec.getParameters() != null) {
				params = new QueryParam[filterSpec.getParameters().length];
				int index = 0;
				for (org.kie.server.api.model.definition.QueryParam param : filterSpec.getParameters()) {
					params[index] = new QueryParam(param.getColumn(), param.getOperator(), param.getValue());
					index++;
				}
			}

			columnMapping = queryCallback.getQueryStrategy().getColumnMapping(params);
		}

		QueryResultMapper<?> resultMapper = QueryMapperRegistry.get().mapperFor(queryCallback.getMapperName(), columnMapping);

		logger.debug("About to perform query '{}' with page {} and page size {}", queryCallback.getQueryName(), page, pageSize);

		Object result = queryService.query(queryCallback.getQueryName(), resultMapper, queryContext, params);

		logger.debug("Result returned from the query {} mapped with {}", result, resultMapper);

		return (T) transform(result, resultMapper);
	}

	protected Object transform(Object result, QueryResultMapper resultMapper) {
		Tuple2<Object, Class<?>> resultTuple = Tuple.of(result, resultMapper.getType());

		return Match(resultTuple).of(
				Case(t -> ProcessInstanceWithVarsDesc.class.isAssignableFrom(t._2),
						t -> convertToProcessInstanceWithVarsList((Collection<ProcessInstanceWithVarsDesc>) t._1)),
				Case(t -> ProcessInstanceDesc.class.isAssignableFrom(t._2),
						t -> convertToProcessInstanceList((Collection<ProcessInstanceDesc>) t._1)),
				Case(t -> UserTaskInstanceWithVarsDesc.class.isAssignableFrom(t._2),
						t -> convertToTaskInstanceWithVarsList((Collection<UserTaskInstanceWithVarsDesc>) t._1)),
				Case(t -> UserTaskInstanceDesc.class.isAssignableFrom(t._2),
						t -> convertToTaskInstanceList((Collection<UserTaskInstanceDesc>) t._1)),
				Case(t -> TaskSummary.class.isAssignableFrom(t._2), t -> convertToTaskSummaryList((Collection<TaskSummary>) t._1)),
				Case(t -> List.class.isAssignableFrom(t._2), t -> new ArrayList((Collection) t._1)), Case($(), resultTuple._1));

		/*
		 * Object actualResult = null; if (result instanceof Collection) {
		 * 
		 * if (ProcessInstanceWithVarsDesc.class.isAssignableFrom(resultMapper.getType())) {
		 * 
		 * logger.debug("Converting collection of ProcessInstanceWithVarsDesc to ProcessInstanceList"); actualResult =
		 * convertToProcessInstanceWithVarsList((Collection<ProcessInstanceWithVarsDesc>) result); } else if
		 * (ProcessInstanceDesc.class.isAssignableFrom(resultMapper.getType())) {
		 * 
		 * logger.debug("Converting collection of ProcessInstanceDesc to ProcessInstanceList"); actualResult =
		 * convertToProcessInstanceList((Collection<ProcessInstanceDesc>) result); } else if
		 * (UserTaskInstanceWithVarsDesc.class.isAssignableFrom(resultMapper.getType())) {
		 * 
		 * logger.debug("Converting collection of UserTaskInstanceWithVarsDesc to TaskInstanceList"); actualResult =
		 * convertToTaskInstanceWithVarsList((Collection<UserTaskInstanceWithVarsDesc>) result); } else if
		 * (UserTaskInstanceDesc.class.isAssignableFrom(resultMapper.getType())) {
		 * 
		 * logger.debug("Converting collection of UserTaskInstanceDesc to TaskInstanceList"); actualResult =
		 * convertToTaskInstanceList((Collection<UserTaskInstanceDesc>) result); } else if
		 * (TaskSummary.class.isAssignableFrom(resultMapper.getType())) {
		 * 
		 * logger.debug("Converting collection of TaskSummary to TaskSummaryList"); actualResult =
		 * convertToTaskSummaryList((Collection<TaskSummary>) result); } else if (List.class.isAssignableFrom(resultMapper.getType())) {
		 * 
		 * logger.debug("Converting collection of List to ArrayList"); actualResult = new ArrayList((Collection) result); } else {
		 * 
		 * logger.debug("Convert not supported for custom type {}", resultMapper.getType()); actualResult = result; }
		 * 
		 * logger.debug("Actual result after converting is {}", actualResult); } else {
		 * logger.debug("Result is not a collection - {}, skipping any conversion", result); actualResult = result; } return actualResult;
		 */

	}

}
