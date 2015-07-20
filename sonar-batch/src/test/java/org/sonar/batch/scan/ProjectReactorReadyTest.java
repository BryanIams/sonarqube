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
package org.sonar.batch.scan;

import org.junit.Test;
import org.sonar.api.batch.bootstrap.ProjectBuilder;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.batch.repository.ProjectScmRepositoryLoader;

import static org.mockito.Mockito.mock;

public class ProjectReactorReadyTest {
  @Test
  public void should_do_nothing() {
    // it's only a barrier
    ProjectReactorReady barrier = new ProjectReactorReady(mock(ProjectExclusions.class), mock(ProjectReactor.class),
      new ProjectBuilder[] {mock(ProjectBuilder.class)}, mock(ProjectReactorValidator.class), mock(ProjectScmRepositoryLoader.class));
    barrier.start();
  }

  @Test
  public void project_builders_should_be_optional() {
    ProjectReactorReady barrier = new ProjectReactorReady(mock(ProjectExclusions.class), mock(ProjectReactor.class), mock(ProjectReactorValidator.class),
      mock(ProjectScmRepositoryLoader.class));
    barrier.start();
  }
}