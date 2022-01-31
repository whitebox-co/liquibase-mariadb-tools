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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import liquibase.exception.RollbackImpossibleException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.statement.core.CommentStatement;

public class MariaDbToolsAddPrimaryKeyChangeTest extends AbstractMariaDbToolsChangeTest<MariaDbToolsAddPrimaryKeyChange> {

    public MariaDbToolsAddPrimaryKeyChangeTest() {
        super(MariaDbToolsAddPrimaryKeyChange.class);
    }

    @Override
    protected void setupChange(MariaDbToolsAddPrimaryKeyChange change) {
        change.setTableName("person");
        change.setColumnNames("id, name");
        change.setConstraintName("pk_id_name");

        alterText = "ADD PRIMARY KEY (id, name)";
    }

    @Test
    public void testWithoutPercona() {
        MariaDbToolsSchemaChangeStatement.available = false;
        SqlStatement[] statements = generateStatements();
        Assertions.assertEquals(1, statements.length);
        Assertions.assertEquals(AddPrimaryKeyStatement.class, statements[0].getClass());
    }

    @Test
    public void testWithoutPerconaRollback() throws RollbackImpossibleException {
        MariaDbToolsSchemaChangeStatement.available = false;
        Assertions.assertThrows(RollbackImpossibleException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                generateRollbackStatements();
            }
        });
    }

    @Test
    public void testWithoutPerconaAndFail() {
        System.setProperty(Configuration.FAIL_IF_NO_MARIADB_TOOLS, "true");
        MariaDbToolsSchemaChangeStatement.available = false;

        Assertions.assertThrows(RuntimeException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                generateStatements();
            }
        });
    }

    @Test
    public void testReal() {
        assertPerconaChange(alterText);
    }

    @Test
    public void testRealRollback() throws RollbackImpossibleException {
        Assertions.assertThrows(RollbackImpossibleException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                generateRollbackStatements();
            }
        });
    }

    @Test
    public void testUpdateSQL() {
        enableLogging();

        SqlStatement[] statements = generateStatements();
        Assertions.assertEquals(3, statements.length);
        Assertions.assertEquals(CommentStatement.class, statements[0].getClass());
        Assertions.assertEquals("mariadb-schema-change "
                + "--alter-foreign-keys-method=auto "
                + "--nocheck-unique-key-change "
                + "--recursion-method=none "
                + "--alter=\"" + alterText + "\" "
                + "--password=*** --execute h=localhost,P=3306,u=user,D=testdb,t=person",
                ((CommentStatement)statements[0]).getText());
        Assertions.assertEquals(CommentStatement.class, statements[1].getClass());
        Assertions.assertEquals(AddPrimaryKeyStatement.class, statements[2].getClass());
    }

    @Test
    public void testRollbackSQL() throws RollbackImpossibleException {
        enableLogging();

        Assertions.assertThrows(RollbackImpossibleException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                generateRollbackStatements();
            }
        });
    }

    @Test
    public void testUpdateSQLNoAlterSqlDryMode() {
        enableLogging();
        System.setProperty(Configuration.NO_ALTER_SQL_DRY_MODE, "true");

        SqlStatement[] statements = generateStatements();
        Assertions.assertEquals(1, statements.length);
        Assertions.assertEquals(CommentStatement.class, statements[0].getClass());
        Assertions.assertEquals("mariadb-schema-change "
                + "--alter-foreign-keys-method=auto "
                + "--nocheck-unique-key-change "
                + "--recursion-method=none "
                + "--alter=\"" + alterText + "\" "
                + "--password=*** --execute h=localhost,P=3306,u=user,D=testdb,t=person",
                ((CommentStatement)statements[0]).getText());
    }

    @Test
    public void testRollbackSQLNoAlterSqlDryMode() throws RollbackImpossibleException {
        enableLogging();
        System.setProperty(Configuration.NO_ALTER_SQL_DRY_MODE, "true");

        Assertions.assertThrows(RollbackImpossibleException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                generateRollbackStatements();
            }
        });
    }

    @Test
    public void testSkipChange() {
        System.setProperty(Configuration.SKIP_CHANGES, "addPrimaryKey");
        SqlStatement[] statements = generateStatements();
        Assertions.assertEquals(1, statements.length);
        Assertions.assertEquals(AddPrimaryKeyStatement.class, statements[0].getClass());
    }
}
