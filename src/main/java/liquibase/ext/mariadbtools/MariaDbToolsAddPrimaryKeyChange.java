package liquibase.ext.mariadbtools;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.change.Change;

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

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtil;

@DatabaseChange(name = MariaDbToolsAddPrimaryKeyChange.NAME, description = "Adds creates a primary key out of an existing column or set of columns.",
    priority = MariaDbToolsAddPrimaryKeyChange.PRIORITY, appliesTo = "column")
public class MariaDbToolsAddPrimaryKeyChange extends AddPrimaryKeyChange implements MariaDbToolsChange {
    public static final String NAME = "addPrimaryKey";
    public static final int PRIORITY = ChangeMetaData.PRIORITY_DEFAULT + 50;

    /**
     * Generates the statements required for the add primary key change.
     * In case of a MySQL database, mariadb tools will be used.
     * In case of generating the SQL statements for review (updateSQL) the command
     * will be added as a comment.
     * @param database the database
     * @return the list of statements
     * @see MariaDbToolsSchemaChangeStatement
     */
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return MariaDbChangeUtil.generateStatements(this,
                database,
                super.generateStatements(database));
    }

    @Override
    public String generateAlterStatement(Database database) {
        StringBuilder alter = new StringBuilder();

        // in case there is a primary key already, we need to drop it first
        // this should be done with one single pt-osc call
        if (MariaDbToolsConstraintsService.getInstance().hasPrimaryKey(database, this)) {
            alter.append("DROP PRIMARY KEY, ");
        }

        alter.append("ADD PRIMARY KEY (");
        List<String> columns = StringUtil.splitAndTrim(getColumnNames(), ",");
        if (columns == null) columns = Collections.emptyList();
        alter.append(database.escapeColumnNameList(StringUtil.join(columns, ", ")));
        alter.append(')');

        return alter.toString();
    }

    @Override
    public String getTargetTableName() {
        return getTableName();
    }

    @Override
    public String getTargetDatabaseName() {
        return getCatalogName();
    }

    @Override
    protected Change[] createInverses() {
        return null;
    }

    //CPD-OFF - common MariaDbToolsChange implementation
    private Boolean useMariaDbTools;

    private String mariaDbToolsOptions;

    @Override
    public String getChangeName() {
        return NAME;
    }

    @Override
    @DatabaseChangeProperty(requiredForDatabase = {})
    public Boolean getUseMariaDbTools() {
        return useMariaDbTools;
    }

    @Override
    public void setUseMariaDbTools(Boolean useMariaDbTools) {
        this.useMariaDbTools = useMariaDbTools;
    }

    @Override
    @DatabaseChangeProperty(requiredForDatabase = {})
    public String getMariaDbToolsOptions() {
        return mariaDbToolsOptions;
    }

    @Override
    public void setMariaDbToolsOptions(String mariaDbToolsOptions) {
        this.mariaDbToolsOptions = mariaDbToolsOptions;
    }

    @Override
    public Set<String> getSerializableFields() {
        Set<String> fields = new HashSet<>(super.getSerializableFields());
        fields.remove("useMariaDbTools");
        fields.remove("mariaDbToolsOptions");
        return Collections.unmodifiableSet(fields);
    }
    //CPD-ON
}
