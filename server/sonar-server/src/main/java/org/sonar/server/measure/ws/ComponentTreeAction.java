/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.measure.ws;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import org.sonar.api.i18n.I18n;
import org.sonar.api.resources.ResourceTypes;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.server.ws.WebService.Param;
import org.sonar.api.utils.Paging;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.metric.MetricDto;
import org.sonar.server.user.UserSession;
import org.sonarqube.ws.WsMeasures;
import org.sonarqube.ws.WsMeasures.ComponentTreeWsResponse;
import org.sonarqube.ws.client.measure.ComponentTreeWsRequest;

import static java.lang.String.format;
import static org.sonar.core.util.Uuids.UUID_EXAMPLE_02;
import static org.sonar.server.measure.ws.ComponentDtoToWsComponent.componentDtoToWsComponent;
import static org.sonar.server.measure.ws.MetricDtoToWsMetric.metricDtoToWsMetric;
import static org.sonar.server.ws.WsParameterBuilder.QualifierParameterContext.newQualifierParameterContext;
import static org.sonar.server.ws.WsParameterBuilder.createQualifiersParameter;
import static org.sonar.server.ws.WsUtils.checkRequest;
import static org.sonar.server.ws.WsUtils.writeProtobuf;
import static org.sonarqube.ws.client.measure.MeasuresWsParameters.ACTION_COMPONENT_TREE;
import static org.sonarqube.ws.client.measure.MeasuresWsParameters.PARAM_ADDITIONAL_FIELDS;
import static org.sonarqube.ws.client.measure.MeasuresWsParameters.PARAM_BASE_COMPONENT_ID;
import static org.sonarqube.ws.client.measure.MeasuresWsParameters.PARAM_BASE_COMPONENT_KEY;
import static org.sonarqube.ws.client.measure.MeasuresWsParameters.PARAM_METRIC_KEYS;
import static org.sonarqube.ws.client.measure.MeasuresWsParameters.PARAM_METRIC_SORT;
import static org.sonarqube.ws.client.measure.MeasuresWsParameters.PARAM_QUALIFIERS;
import static org.sonarqube.ws.client.measure.MeasuresWsParameters.PARAM_STRATEGY;

public class ComponentTreeAction implements MeasuresWsAction {
  private static final int MAX_SIZE = 500;
  static final String ALL_STRATEGY = "all";
  static final String CHILDREN_STRATEGY = "children";
  static final String LEAVES_STRATEGY = "leaves";
  static final Set<String> STRATEGIES = ImmutableSortedSet.of(ALL_STRATEGY, CHILDREN_STRATEGY, LEAVES_STRATEGY);
  static final String NAME_SORT = "name";
  static final String PATH_SORT = "path";
  static final String QUALIFIER_SORT = "qualifier";
  static final String METRIC_SORT = "metric";
  static final Set<String> SORTS = ImmutableSortedSet.of(NAME_SORT, PATH_SORT, QUALIFIER_SORT, METRIC_SORT);
  static final String ADDITIONAL_METRICS = "metrics";
  static final String ADDITIONAL_PERIODS = "periods";
  static final Set<String> ADDITIONAL_FIELDS = ImmutableSortedSet.of(ADDITIONAL_METRICS, ADDITIONAL_PERIODS);

  private final ComponentTreeDataLoader dataLoader;
  private final UserSession userSession;
  private final I18n i18n;
  private final ResourceTypes resourceTypes;

  public ComponentTreeAction(ComponentTreeDataLoader dataLoader, UserSession userSession, I18n i18n,
    ResourceTypes resourceTypes) {
    this.dataLoader = dataLoader;
    this.userSession = userSession;
    this.i18n = i18n;
    this.resourceTypes = resourceTypes;
  }

  @Override
  public void define(WebService.NewController context) {
    WebService.NewAction action = context.createAction(ACTION_COMPONENT_TREE)
      .setDescription(format("Navigate through components based on the chosen strategy with specified measures. The %s or the %s parameter must be provided.<br>" +
        "Requires one of the following permissions:" +
        "<ul>" +
        "<li>'Administer System'</li>" +
        "<li>'Administer' rights on the specified project</li>" +
        "<li>'Browse' on the specified project</li>" +
        "</ul>" +
        "When limiting search with the %s parameter, directories are not returned.",
        PARAM_BASE_COMPONENT_ID, PARAM_BASE_COMPONENT_KEY, Param.TEXT_QUERY))
      .setResponseExample(getClass().getResource("component_tree-example.json"))
      .setSince("5.4")
      .setHandler(this)
      .addPagingParams(100, MAX_SIZE);

    action.createSortParams(SORTS, NAME_SORT, true)
      .setDescription("Comma-separated list of sort fields")
      .setExampleValue(NAME_SORT + ", " + PATH_SORT);

    action.createParam(Param.TEXT_QUERY)
      .setDescription("Limit search to: <ul>" +
        "<li>component names that contain the supplied string</li>" +
        "<li>component keys that are exactly the same as the supplied string</li>" +
        "</ul>")
      .setExampleValue("FILE_NAM");

    action.createParam(PARAM_BASE_COMPONENT_ID)
      .setDescription("Base component id. The search is based on this component. It is not included in the response.")
      .setExampleValue(UUID_EXAMPLE_02);

    action.createParam(PARAM_BASE_COMPONENT_KEY)
      .setDescription("Base component key.The search is based on this component. It is not included in the response.")
      .setExampleValue("org.apache.hbas:hbase");

    action.createParam(PARAM_METRIC_KEYS)
      .setDescription("Metric keys")
      .setRequired(true)
      .setExampleValue("ncloc,complexity,violations");

    action.createParam(PARAM_METRIC_SORT)
      .setDescription(
        format("Metric key to sort by. The '%s' parameter must contain the '%s' value. It must be part of the '%s' parameter", Param.SORT, METRIC_SORT, PARAM_METRIC_KEYS))
      .setExampleValue("ncloc");

    action.createParam(PARAM_ADDITIONAL_FIELDS)
      .setDescription("Comma-separated list of additional fields that can be returned in the response.")
      .setPossibleValues(ADDITIONAL_FIELDS)
      .setExampleValue("periods,metrics");

    createQualifiersParameter(action, newQualifierParameterContext(userSession, i18n, resourceTypes));

    action.createParam(PARAM_STRATEGY)
      .setDescription("Strategy to search for base component descendants:" +
        "<ul>" +
        "<li>children: return the children components of the base component. Grandchildren components are not returned</li>" +
        "<li>all: return all the descendants components of the base component. Grandchildren are returned. Base component is not returned.</li>" +
        "<li>leaves: return all the descendant components (files, in general) which don't have other children. They are the leaves of the component tree.</li>" +
        "</ul>")
      .setPossibleValues(STRATEGIES)
      .setDefaultValue(ALL_STRATEGY);
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    ComponentTreeWsResponse componentTreeWsResponse = doHandle(toComponentTreeWsRequest(request));
    writeProtobuf(componentTreeWsResponse, request, response);
  }

  private ComponentTreeWsResponse doHandle(ComponentTreeWsRequest request) {
    ComponentTreeData data = dataLoader.load(request);
    if (data.getComponents()==null) {
      return emptyResponse(data.getBaseComponent(), request);
    }

    return buildResponse(
      request,
      data,
      Paging.forPageIndex(
        request.getPage())
        .withPageSize(request.getPageSize())
        .andTotal(data.getComponentCount()));
  }

  private static ComponentTreeWsResponse buildResponse(ComponentTreeWsRequest request, ComponentTreeData data, Paging paging) {
    ComponentTreeWsResponse.Builder response = ComponentTreeWsResponse.newBuilder();
    response.getPagingBuilder()
      .setPageIndex(paging.pageIndex())
      .setPageSize(paging.pageSize())
      .setTotal(paging.total())
      .build();

    response.setBaseComponent(componentDtoToWsComponent(data.getBaseComponent()));

    for (ComponentDto componentDto : data.getComponents()) {
      response.addComponents(componentDtoToWsComponent(
        componentDto,
        data.getMeasuresByComponentUuidAndMetric().row(componentDto.uuid()),
        data.getReferenceComponentUuidsById()));
    }

    if (areMetricsInResponse(request)) {
      WsMeasures.Metrics.Builder metricsBuilder = response.getMetricsBuilder();
      for (MetricDto metricDto : data.getMetrics()) {
        metricsBuilder.addMetrics(metricDtoToWsMetric(metricDto));
      }
    }

    if (arePeriodsInResponse(request)) {
      response.getPeriodsBuilder().addAllPeriods(data.getPeriods());
    }

    return response.build();
  }

  private static boolean areMetricsInResponse(ComponentTreeWsRequest request) {
    return request.getAdditionalFields() != null && request.getAdditionalFields().contains(ADDITIONAL_METRICS);
  }

  private static boolean arePeriodsInResponse(ComponentTreeWsRequest request) {
    return request.getAdditionalFields() != null && request.getAdditionalFields().contains(ADDITIONAL_PERIODS);
  }

  private static ComponentTreeWsResponse emptyResponse(ComponentDto baseComponent, ComponentTreeWsRequest request) {
    ComponentTreeWsResponse.Builder response = ComponentTreeWsResponse.newBuilder();
    response.getPagingBuilder()
      .setPageIndex(request.getPage())
      .setPageSize(request.getPageSize())
      .setTotal(0);
    response.setBaseComponent(componentDtoToWsComponent(baseComponent));
    return response.build();
  }

  private static ComponentTreeWsRequest toComponentTreeWsRequest(Request request) {
    ComponentTreeWsRequest componentTreeWsRequest = new ComponentTreeWsRequest()
      .setBaseComponentId(request.param(PARAM_BASE_COMPONENT_ID))
      .setBaseComponentKey(request.param(PARAM_BASE_COMPONENT_KEY))
      .setMetricKeys(request.mandatoryParamAsStrings(PARAM_METRIC_KEYS))
      .setStrategy(request.mandatoryParam(PARAM_STRATEGY))
      .setQualifiers(request.paramAsStrings(PARAM_QUALIFIERS))
      .setAdditionalFields(request.paramAsStrings(PARAM_ADDITIONAL_FIELDS))
      .setSort(request.paramAsStrings(Param.SORT))
      .setAsc(request.paramAsBoolean(Param.ASCENDING))
      .setMetricSort(request.param(PARAM_METRIC_SORT))
      .setPage(request.mandatoryParamAsInt(Param.PAGE))
      .setPageSize(request.mandatoryParamAsInt(Param.PAGE_SIZE))
      .setQuery(request.param(Param.TEXT_QUERY));
    String metricSortValue = componentTreeWsRequest.getMetricSort();
    checkRequest(!componentTreeWsRequest.getMetricKeys().isEmpty(), "The '%s' parameter must contain at least one metric key", PARAM_METRIC_KEYS);
    checkRequest(metricSortValue == null ^ componentTreeWsRequest.getSort().contains(METRIC_SORT),
      "To sort by a metric, the '%s' parameter must contain '%s' and a metric key must be provided in the '%s' parameter",
      Param.SORT, METRIC_SORT, PARAM_METRIC_SORT);
    checkRequest(metricSortValue == null ^ componentTreeWsRequest.getMetricKeys().contains(metricSortValue),
      "To sort by the '%s' metric, it must be in the list of metric keys in the '%s' parameter", metricSortValue, PARAM_METRIC_KEYS);
    return componentTreeWsRequest;
  }
}
