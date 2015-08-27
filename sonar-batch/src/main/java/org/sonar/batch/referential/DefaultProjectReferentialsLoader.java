/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.batch.referential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.batch.bootstrap.AnalysisMode;
import org.sonar.batch.bootstrap.ServerClient;
import org.sonar.batch.bootstrap.TaskProperties;
import org.sonar.batch.protocol.input.ProjectReferentials;
import org.sonar.batch.rule.ModuleQProfiles;

public class DefaultProjectReferentialsLoader implements ProjectReferentialsLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultProjectReferentialsLoader.class);

  private static final String BATCH_PROJECT_URL = "/batch/project";
  private static final int TIMEOUT_MS = 60000;

  private final ServerClient serverClient;
  private final AnalysisMode analysisMode;

  public DefaultProjectReferentialsLoader(ServerClient serverClient, AnalysisMode analysisMode) {
    this.serverClient = serverClient;
    this.analysisMode = analysisMode;
  }

  @Override
  public ProjectReferentials load(ProjectReactor reactor, TaskProperties taskProperties) {
    String url = BATCH_PROJECT_URL + "?key=" + reactor.getRoot().getKeyWithBranch();
    if (taskProperties.properties().containsKey(ModuleQProfiles.SONAR_PROFILE_PROP)) {
      LOG.warn("Ability to set quality profile from command line using '" + ModuleQProfiles.SONAR_PROFILE_PROP
        + "' is deprecated and will be dropped in a future SonarQube version. Please configure quality profile used by your project on SonarQube server.");
      url += "&profile=" + ServerClient.encodeForUrl(taskProperties.properties().get(ModuleQProfiles.SONAR_PROFILE_PROP));
    }
    url += "&preview=" + analysisMode.isPreview();
    LOG.debug(String.format("Request %s [timeout=%d ms]", url, TIMEOUT_MS));
    return ProjectReferentials.fromJson(serverClient.request(url, true, TIMEOUT_MS));
  }
}
