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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.BaseObjectPool;
import org.apache.commons.pool2.ObjectPool;

public class MockedDbcp2PoolingConnection {

    /**
     * Creates a mocked SQL connection, that looks like a apache commons dbcp pooled connection.
     * @param username the username to use
     * @param password the password to use
     * @return the connection
     * @throws SQLException
     */
    public static Connection create(String username, String password) throws SQLException {
        ObjectPool<Connection> pool = new MockedObjectPool(username, password);
        PoolingDataSource<Connection> ds = new PoolingDataSource<Connection>(pool);
        try {
            return ds.getConnection();
        } finally {
            try {
                ds.close();
            } catch (Exception ignored) {
                // ignored
            }
        }
    }

    private static class MockedObjectPool extends BaseObjectPool<Connection> {
        private final String username;
        private final String password;

        public MockedObjectPool(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public Connection borrowObject() throws Exception {
            Properties info = new Properties();
            info.setProperty("user", username);
            info.setProperty("password", password);
            return new NoOpMySqlConnection("host", 1234, info, "database", "jdbc:mysql://");
        }

        @Override
        public void returnObject(Connection obj) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public void invalidateObject(Connection obj) throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
