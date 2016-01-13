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
package org.sonar.process;

import java.io.File;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ProcessCommands} based on a {@link AllProcessesCommands} of which will request a
 * single {@link ProcessCommands} to use as delegate for the specified processNumber.
 */
public class DefaultProcessCommands implements ProcessCommands {
  private final AllProcessesCommands allProcessesCommands;
  private final ProcessCommands delegate;

  public DefaultProcessCommands(File directory, int processNumber) {
    this(directory, processNumber, true);
  }

  public DefaultProcessCommands(File directory, int processNumber, boolean clean) {
    this.allProcessesCommands = new AllProcessesCommands(directory);
    this.delegate = allProcessesCommands.getProcessCommand(processNumber, clean);
  }

  @Override
  public boolean isReady() {
    return delegate.isReady();
  }

  @Override
  public void setReady() {
    delegate.setReady();
  }

  @Override
  public void ping() {
    delegate.ping();
  }

  @Override
  public long getLastPing() {
    return delegate.getLastPing();
  }

  @Override
  public void askForStop() {
    delegate.askForStop();
  }

  @Override
  public boolean askedForStop() {
    return delegate.askedForStop();
  }

  @Override
  public void askForRestart() {
    delegate.askForRestart();
  }

  @Override
  public boolean askedForRestart() {
    return delegate.askedForRestart();
  }

  @Override
  public void acknowledgeAskForRestart() {
    delegate.acknowledgeAskForRestart();
  }

  @Override
  public void endWatch() {
    try {
      close();
    } catch (Exception e) {
      LoggerFactory.getLogger(getClass()).error("Failed to close DefaultProcessCommands", e);
    }
  }

  @Override
  public void close() throws Exception {
    allProcessesCommands.close();
  }
}
