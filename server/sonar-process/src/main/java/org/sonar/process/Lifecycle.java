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

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.sonar.process.Lifecycle.State.HARD_STOPPING;
import static org.sonar.process.Lifecycle.State.INIT;
import static org.sonar.process.Lifecycle.State.RESTARTING;
import static org.sonar.process.Lifecycle.State.STARTED;
import static org.sonar.process.Lifecycle.State.STARTING;
import static org.sonar.process.Lifecycle.State.STOPPED;
import static org.sonar.process.Lifecycle.State.STOPPING;

public class Lifecycle {
  private static final Logger LOG = LoggerFactory.getLogger(Lifecycle.class);

  public enum State {
    INIT, STARTING, STARTED, RESTARTING, STOPPING, HARD_STOPPING, STOPPED
  }

  private static final Map<State, Set<State>> TRANSITIONS = buildTransitions();

  private static Map<State, Set<State>> buildTransitions() {
    Map<State, Set<State>> res = new EnumMap<>(State.class);
    res.put(INIT, toSet(STARTING));
    res.put(STARTING, toSet(STARTED, STOPPING, HARD_STOPPING));
    res.put(STARTED, toSet(RESTARTING, STOPPING, HARD_STOPPING));
    res.put(RESTARTING, toSet(STARTING, HARD_STOPPING));
    res.put(STOPPING, toSet(STOPPED));
    res.put(HARD_STOPPING, toSet(STOPPED));
    res.put(STOPPED, toSet());
    return res;
  }

  private static Set<State> toSet(State... states) {
    if (states.length == 0) {
      return Collections.emptySet();
    }
    if (states.length == 1) {
      return Collections.singleton(states[0]);
    }
    Set<State> res = new HashSet<>(states.length);
    Collections.addAll(res, states);
    return res;
  }

  private State state = INIT;

  public State getState() {
    return state;
  }

  public synchronized boolean tryToMoveTo(State to) {
    boolean res = false;
    State currentState = state;
    if (TRANSITIONS.get(currentState).contains(to)) {
      this.state = to;
      res = true;
    }
    LOG.trace("tryToMoveTo from {} to {} => {}", currentState, to, res);
    return res;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Lifecycle lifecycle = (Lifecycle) o;
    return state == lifecycle.state;
  }

  @Override
  public int hashCode() {
    return state.hashCode();
  }
}
