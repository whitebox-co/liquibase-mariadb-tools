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

import liquibase.change.AddColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.exception.RollbackImpossibleException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.CommentStatement;
import liquibase.statement.core.DropColumnStatement;

public class MariaDbToolsAddColumnChangeTest extends AbstractMariaDbToolsChangeTest<MariaDbToolsAddColumnChange> {

    public MariaDbToolsAddColumnChangeTest() {
        super(MariaDbToolsAddColumnChange.class);
    }

    @Override
    protected void setupChange(MariaDbToolsAddColumnChange change) {
        AddColumnConfig column = new AddColumnConfig();
        column.setName("new_column");
        column.setType("INT");
        change.addColumn(column);
        change.setTableName("person");
        change.setCatalogName("testdb");

        alterText = "ADD COLUMN new_column INT NULL";
    }

    @Test
    public void testWithoutPercona() {
        MariaDbToolsSchemaChangeStatement.available = false;
        SqlStatement[] statements = generateStatements();
        Assertions.assertEquals(1, statements.length);
        Assertions.assertEquals(AddColumnStatement.class, statements[0].getClass());
    }

    @Test
    public void testWithoutPerconaRollback() throws RollbackImpossibleException {
        MariaDbToolsSchemaChangeStatement.available = false;
        SqlStatement[] statements = generateRollbackStatements();
        Assertions.assertEquals(1, statements.length);
        Assertions.assertEquals(DropColumnStatement.class, statements[0].getClass());
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
    public void testWithoutPerdatabaseconaRollbackAndFail() throws RollbackImpossibleException {
        System.setProperty(Configuration.FAIL_IF_NO_MARIADB_TOOLS, "true");
        MariaDbToolsSchemaChangeStatement.available = false;

        Assertions.assertThrows(RuntimeException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                generateRollbackStatements();
            }
        });
    }

    @Test
    public void testReal() {
        assertPerconaChange(alterText);
    }

    @Test
    public void testRealRollback() throws RollbackImpossibleException {
        assertPerconaRollbackChange("DROP COLUMN new_column");
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
                + "--alter=\"ADD COLUMN new_column INT NULL\" "
                + "--password=*** --execute "
                + "h=localhost,P=3306,u=user,D=testdb,t=person",
                ((CommentStatement)statements[0]).getText());
        Assertions.assertEquals(CommentStatement.class, statements[1].getClass());
        Assertions.assertEquals(AddColumnStatement.class, statements[2].getClass());
    }

    @Test
    public void testRollbackSQL() throws RollbackImpossibleException {
        enableLogging();

        SqlStatement[] statements = generateRollbackStatements();
        Assertions.assertEquals(3, statements.length);
        Assertions.assertEquals(CommentStatement.class, statements[0].getClass());
        Assertions.assertEquals("mariadb-schema-change "
                + "--alter-foreign-keys-method=auto "
                + "--nocheck-unique-key-change "
                + "--recursion-method=none "
                + "--alter=\"DROP COLUMN new_column\" "
                + "--password=*** --execute "
                + "h=localhost,P=3306,u=user,D=testdb,t=person",
                ((CommentStatement)statements[0]).getText());
        Assertions.assertEquals(CommentStatement.class, statements[1].getClass());
        Assertions.assertEquals(DropColumnStatement.class, statements[2].getClass());
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
                + "--alter=\"ADD COLUMN new_column INT NULL\" "
                + "--password=*** --execute "
                + "h=localhost,P=3306,u=user,D=testdb,t=person",
                ((CommentStatement)statements[0]).getText());
    }

    @Test
    public void testRollbackSQLNoAlterSqlDryMode() throws RollbackImpossibleException {
        enableLogging();
        System.setProperty(Configuration.NO_ALTER_SQL_DRY_MODE, "true");

        SqlStatement[] statements = generateRollbackStatements();
        Assertions.assertEquals(1, statements.length);
        Assertions.assertEquals(CommentStatement.class, statements[0].getClass());
        Assertions.assertEquals("mariadb-schema-change "
                + "--alter-foreign-keys-method=auto "
                + "--nocheck-unique-key-change "
                + "--recursion-method=none "
                + "--alter=\"DROP COLUMN new_column\" "
                + "--password=*** --execute "
                + "h=localhost,P=3306,u=user,D=testdb,t=person",
                ((CommentStatement)statements[0]).getText());
    }

    @Test
    public void testGenerateAlterMultipleColumns() {
        AddColumnConfig column = new AddColumnConfig();
        column.setName("email");
        column.setType("varchar(255)");
        getChange().addColumn(column);

        Assertions.assertEquals("ADD COLUMN new_column INT NULL, ADD COLUMN email VARCHAR(255) NULL",
                getChange().generateAlterStatement(getDatabase()));
    }

    @Test
    public void testConvertColumnToSql() {
        MariaDbToolsAddColumnChange c = getChange();
        Database database = getDatabase();
        Assertions.assertEquals("ADD COLUMN new_column INT NULL", c.convertColumnToSql(c.getColumns().get(0), database));

        AddColumnConfig column = new AddColumnConfig();
        column.setName("email");
        column.setType("varchar(255)");
        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(false);
        column.setConstraints(constraints);
        Assertions.assertEquals("ADD COLUMN email VARCHAR(255) NOT NULL", c.convertColumnToSql(column, database));

        column.setDefaultValue("no-email@example.org");
        Assertions.assertEquals("ADD COLUMN email VARCHAR(255) NOT NULL DEFAULT 'no-email@example.org'",
                c.convertColumnToSql(column, database));

        column.setRemarks("that is the email");
        Assertions.assertEquals(
                "ADD COLUMN email VARCHAR(255) NOT NULL DEFAULT 'no-email@example.org' COMMENT 'that is the email'",
                c.convertColumnToSql(column, database));
    }

    @Test
    public void testSkipAddColumnChange() {
        System.setProperty(Configuration.SKIP_CHANGES, "addColumn");
        SqlStatement[] statements = generateStatements();
        Assertions.assertEquals(1, statements.length);
        Assertions.assertEquals(AddColumnStatement.class, statements[0].getClass());
    }

    @Test
    public void testConvertColumnToSqlWithAfter() {
        MariaDbToolsAddColumnChange c = getChange();
        Database database = getDatabase();

        AddColumnConfig column = new AddColumnConfig();
        column.setName("new_column");
        column.setType("INT");
        column.setAfterColumn("other_column");

        Assertions.assertEquals("ADD COLUMN new_column INT NULL AFTER other_column",
                c.convertColumnToSql(column, database));

        column.setAfterColumn("other column");
        Assertions.assertEquals("ADD COLUMN new_column INT NULL AFTER `other column`",
                c.convertColumnToSql(column, database));

        column.setAfterColumn("");
        Assertions.assertEquals("ADD COLUMN new_column INT NULL",
                c.convertColumnToSql(column, database));
    }

    @Test
    public void testConvertColumnToSqlWithConstraints() {
        MariaDbToolsAddColumnChange c = getChange();
        Database database = getDatabase();

        AddColumnConfig column = new AddColumnConfig();
        column.setName("testColumn");
        column.setType("BIGINT(20)");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(true);
        constraints.setForeignKeyName("fk_test_column");
        constraints.setReferences("test_parent(id)");
        column.setConstraints(constraints);

        Assertions.assertEquals("ADD COLUMN testColumn BIGINT NULL, "
                + "ADD CONSTRAINT fk_test_column FOREIGN KEY (testColumn) REFERENCES test_parent(id)",
                c.convertColumnToSql(column, database));

        constraints = new ConstraintsConfig();
        constraints.setNullable(true);
        constraints.setReferences("test_parent(id)");
        column.setConstraints(constraints);

        Assertions.assertEquals(
                "ADD COLUMN testColumn BIGINT NULL, ADD FOREIGN KEY (testColumn) REFERENCES test_parent(id)",
                c.convertColumnToSql(column, database));

        constraints = new ConstraintsConfig();
        constraints.setNullable(true).setUnique("true").setUniqueConstraintName("unique_test_column");
        column.setConstraints(constraints);

        Assertions.assertEquals(
                "ADD COLUMN testColumn BIGINT NULL, ADD CONSTRAINT unique_test_column UNIQUE (testColumn)",
                c.convertColumnToSql(column, database));

        constraints = new ConstraintsConfig();
        constraints.setNullable(true).setUnique("true");
        column.setConstraints(constraints);

        Assertions.assertEquals(
                "ADD COLUMN testColumn BIGINT NULL, ADD UNIQUE (testColumn)",
                c.convertColumnToSql(column, database));

        constraints = new ConstraintsConfig();
        constraints.setNullable(true);
        constraints.setForeignKeyName("fk_test_column").setReferences("test_parent(id)");
        constraints.setUnique(true).setUniqueConstraintName("unique_test_column");
        column.setConstraints(constraints);

        Assertions.assertEquals("ADD COLUMN testColumn BIGINT NULL, "
                + "ADD CONSTRAINT fk_test_column FOREIGN KEY (testColumn) REFERENCES test_parent(id), "
                + "ADD CONSTRAINT unique_test_column UNIQUE (testColumn)",
                c.convertColumnToSql(column, database));

        constraints = new ConstraintsConfig();
        constraints.setNullable(true);
        constraints.setForeignKeyName("fk_test_column").setReferencedTableName("test_parent");
        constraints.setReferencedColumnNames("id");
        column.setConstraints(constraints);

        Assertions.assertEquals("ADD COLUMN testColumn BIGINT NULL, "
                + "ADD CONSTRAINT fk_test_column FOREIGN KEY (testColumn) REFERENCES test_parent(id)",
                c.convertColumnToSql(column, database));
    }

    @Test
    public void testConvertColumnToSqlWithConstraintSelfReferencingTable() {
        MariaDbToolsAddColumnChange c = getChange();
        Database database = getDatabase();

        AddColumnConfig column = new AddColumnConfig();
        column.setName("testColumn");
        column.setType("BIGINT(20)");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(true);
        constraints.setForeignKeyName("fk_test_column");
        constraints.setReferences("person(id)");
        column.setConstraints(constraints);

        Assertions.assertEquals("ADD COLUMN testColumn BIGINT NULL, "
                + "ADD CONSTRAINT fk_test_column FOREIGN KEY (testColumn) REFERENCES _person_new(id)",
                c.convertColumnToSql(column, database));
    }
}
