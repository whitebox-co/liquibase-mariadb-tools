package liquibase.ext.mariadbtools;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import liquibase.change.core.DropIndexChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(name = MariaDbToolsDropIndexChange.NAME,
    description = "Drops an existing index",
    priority = MariaDbToolsDropIndexChange.PRIORITY, appliesTo = "index")
public class MariaDbToolsDropIndexChange extends DropIndexChange implements MariaDbToolsChange {
    public static final String NAME = "dropIndex";
    public static final int PRIORITY = ChangeMetaData.PRIORITY_DEFAULT + 50;

    @Override
    public SqlStatement[] generateStatements( Database database )
    {
        return MariaDbChangeUtil.generateStatements(this,
                    database,
                    super.generateStatements(database));
    }

    @Override
    public String generateAlterStatement( Database database )
    {
        StringBuilder alter = new StringBuilder();

        alter.append( "DROP ");
        alter.append( "INDEX " );

        if (this.getIndexName() != null) {
            alter.append(database.escapeIndexName(this.getCatalogName(), this.getSchemaName(), this.getIndexName()));
        }

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
