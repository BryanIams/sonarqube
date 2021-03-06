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
package org.sonar.db.version.v55;

import org.junit.Before;
import org.junit.Test;
import org.sonar.db.Database;
import org.sonar.db.dialect.PostgreSql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DropRulesDatesAndCharacteristicsTest {

  DropRulesDatesAndCharacteristics underTest;

  Database database;

  @Before
  public void setUp() {
    database = mock(Database.class);
    underTest = new DropRulesDatesAndCharacteristics(database);
  }

  @Test
  public void generate_sql_on_postgresql() {
    when(database.getDialect()).thenReturn(new PostgreSql());
    assertThat(underTest.generateSql()).isEqualTo(
      "ALTER TABLE rules DROP COLUMN created_at, DROP COLUMN updated_at, DROP COLUMN characteristic_id, DROP COLUMN default_characteristic_id"
      );
  }

}
