package liquibase.ext.mariadbtools;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.MySQLDatabase;

@ExtendWith(RestoreSystemPropertiesExtension.class)
public class MariaDbToolsSchemaChangeStatementTest {
    private Database database;

    @BeforeEach
    public void setup() {
        System.setProperty(Configuration.LIQUIBASE_PASSWORD, "root");

        database = new MySQLDatabase();
        database.setLiquibaseSchemaName("testdb");
        DatabaseConnection conn = new MockDatabaseConnection("jdbc:mysql://user@localhost:3306/testdb",
                "user@localhost");
        database.setConnection(conn);
    }

    @Test
    public void checkPerconaToolkitIsAvailable() {
        MariaDbToolsSchemaChangeStatement.available = null;
        if (MariaDbToolsSchemaChangeStatement.isAvailable()) {
            System.out.println("pt-online-schema-change is available.");
        } else {
            System.out.println("pt-online-schema-change is NOT available.");
        }
        System.out.println("Version is " + MariaDbToolsSchemaChangeStatement.getVersion());
    }

    @Test
    public void testBuildCommand() {
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.empty());
        Assertions.assertEquals(
                "[mariadb-schema-change, --alter-foreign-keys-method=auto, --nocheck-unique-key-change, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }

    @Test
    public void testBuildCommandWithPerconaOptions() {
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.of("--per-change-option"));
        Assertions.assertEquals(
                "[mariadb-schema-change, --per-change-option, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }

    @Test
    public void testBuildCommand2() {
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL, ADD COLUMN email VARCHAR(255) NULL", Optional.empty());
        Assertions.assertEquals(
                "[mariadb-schema-change, --alter-foreign-keys-method=auto, --nocheck-unique-key-change, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, ADD COLUMN email VARCHAR(255) NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }

    @Test
    public void testPrintCommand() {
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.empty());
        Assertions.assertEquals(
                "mariadb-schema-change --alter-foreign-keys-method=auto --nocheck-unique-key-change --recursion-method=none --alter=\"ADD COLUMN new_column INT NULL\" --password=*** --execute h=localhost,P=3306,u=user,D=testdb,t=person",
                statement.printCommand(database));
    }

    @Test
    public void testAdditionalOptions() {
        System.setProperty(Configuration.ADDITIONAL_OPTIONS, "--config /tmp/percona.conf");
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.empty());
        Assertions.assertEquals(
                "[mariadb-schema-change, --config, /tmp/percona.conf, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }

    @Test
    public void testMultipleAdditionalOptions() {
        System.setProperty(Configuration.ADDITIONAL_OPTIONS, "--config /tmp/percona.conf --alter-foreign-keys-method=auto");
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.empty());
        Assertions.assertEquals(
                "[mariadb-schema-change, --config, /tmp/percona.conf, --alter-foreign-keys-method=auto, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }

    @Test
    public void testAdditionalOptionsWithSpaces() {
        System.setProperty(Configuration.ADDITIONAL_OPTIONS, "--config \"/tmp/file with spaces.conf\"");
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.empty());
        Assertions.assertEquals(
                "[mariadb-schema-change, --config, /tmp/file with spaces.conf, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }

    @Test
    public void testAdditionalOptionsWithQuotes() {
        System.setProperty(Configuration.ADDITIONAL_OPTIONS, "--config \"/tmp/percona.conf\"");
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.empty());
        Assertions.assertEquals(
                "[mariadb-schema-change, --config, /tmp/percona.conf, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }

    @Test
    public void testAdditionalOptionsMultipleWithQuotes() {
        System.setProperty(Configuration.ADDITIONAL_OPTIONS, "--critical-load=\"Threads_running=160\" --alter-foreign-keys-method=\"auto\"");
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.empty());
        Assertions.assertEquals(
                "[mariadb-schema-change, --critical-load=Threads_running=160, --alter-foreign-keys-method=auto, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }

    @Test
    public void testAdditionalOptionsMultipleWithQuotesAndSpaces() {
        System.setProperty(Configuration.ADDITIONAL_OPTIONS, "--arg1=\"val1 val2\" --alter-foreign-keys-method=\"auto\"");
        MariaDbToolsSchemaChangeStatement statement = new MariaDbToolsSchemaChangeStatement("testdb", "person",
                "ADD COLUMN new_column INT NULL", Optional.empty());
        Assertions.assertEquals(
                "[mariadb-schema-change, --arg1=val1 val2, --alter-foreign-keys-method=auto, --recursion-method=none, --alter=ADD COLUMN new_column INT NULL, --password=root, --execute, h=localhost,P=3306,u=user,D=testdb,t=person]",
                String.valueOf(statement.buildCommand(database)));
    }
}
