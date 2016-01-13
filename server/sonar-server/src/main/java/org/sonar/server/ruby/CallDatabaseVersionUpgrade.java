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
package org.sonar.server.ruby;

/**
 * Interface which must be top-level public class to be used by the Ruby engine but that hides name of the Ruby method
 * in the Ruby script from the rest of the platform (only {@link RubyDatabaseMigration} is known to the platform).
 */
public interface CallDatabaseVersionUpgrade {

  /**
   * Java method that calls the upgrade_and_start method defined in the {@code call_databaseversion_upgrade.rb} script.
   */
  void callUpgrade();
}
