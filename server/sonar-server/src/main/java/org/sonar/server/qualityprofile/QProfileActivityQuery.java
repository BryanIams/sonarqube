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
package org.sonar.server.qualityprofile;

import org.sonar.server.activity.Activity;
import org.sonar.server.activity.index.ActivityQuery;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * @since 4.4
 */
public class QProfileActivityQuery extends ActivityQuery {

  public QProfileActivityQuery() {
    super();
    setTypes(Arrays.asList(Activity.Type.QPROFILE.name()));
  }

  @CheckForNull
  public String getQprofileKey() {
    return (String)getDataOrFilters().get("profileKey");
  }

  public QProfileActivityQuery setQprofileKey(@Nullable String qprofileKey) {
    if (qprofileKey != null) {
      addDataOrFilter("profileKey", qprofileKey);
    }
    return this;
  }
}
