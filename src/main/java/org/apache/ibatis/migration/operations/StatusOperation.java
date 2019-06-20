/**
 *    Copyright 2010-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.util.*;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;
import org.apache.ibatis.migration.utils.Util;

public final class StatusOperation extends DatabaseOperation {

  private int applied;
  private int pending;
  private int missing;

  private List<Change> changes;

  public StatusOperation operate(ConnectionProvider connectionProvider, MigrationLoader migrationsLoader,
      DatabaseOperationOption option, PrintStream printStream) {
    if (option == null) {
      option = new DatabaseOperationOption();
    }
    println(printStream, "ID             Applied At          Description");
    println(printStream, Util.horizontalLine("", 80));
    changes = new ArrayList<Change>();
    List<Change> migrations = migrationsLoader.getMigrations();
    if (changelogExists(connectionProvider, option)) {
      List<Change> changelog = getChangelog(connectionProvider, option);

      Set<Change> changelogAndMigrations = new HashSet<Change>();
      changelogAndMigrations.addAll(changelog);
      changelogAndMigrations.addAll(migrations);

      for (Change change : changelogAndMigrations) {
        changes.add(change);
        if (!migrations.contains(change)) {
          change.setMissing(true);
          missing++;
        } else if (change.isApplied()) {
          applied++;
        } else {
          pending++;
        }
      }
    } else {
      changes.addAll(migrations);
      pending = migrations.size();
    }
    Collections.sort(changes);
    for (Change change : changes) {
      println(printStream, change.toString());
    }
    println(printStream);
    return this;
  }

  public int getAppliedCount() {
    return applied;
  }

  public int getPendingCount() {
    return pending;
  }

  public int getMissingCount() {
    return missing;
  }

  public List<Change> getCurrentStatus() {
    return changes;
  }
}
