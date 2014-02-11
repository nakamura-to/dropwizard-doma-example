/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package example;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.seasar.doma.dropwizard.DomaBundle;
import org.seasar.doma.dropwizard.DomaConfig;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.builder.UpdateBuilder;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;

/**
 * @author nakamura-to
 *
 */
public class DepartmentApplication extends Application<DepartmentConfiguration> {

    protected DomaBundle<DepartmentConfiguration> domaBundle = new DomaBundle<DepartmentConfiguration>() {

        @Override
        public DataSourceFactory getDataSourceFactory(
                DepartmentConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }

    };

    @Override
    public void initialize(Bootstrap<DepartmentConfiguration> bootstrap) {
        bootstrap.addBundle(domaBundle);
    }

    @Override
    public void run(DepartmentConfiguration configuration,
            Environment environment) throws Exception {
        DomaConfig config = domaBundle.getConfig();
        initializeDatabase(config);

        environment.jersey().register(
                new DepartmentResource(new DepartmentDaoImpl(config)));
    }

    protected void initializeDatabase(DomaConfig config) {
        LocalTransactionManager tx = config.getLocalTransactionManager();
        tx.required(() -> {
            executeSql(
                    config,
                    "create table department (id integer not null primary key,name varchar(255) not null, version integer not null)");
            executeSql(config,
                    "insert into department values(1,'ACCOUNTING',1)");
            executeSql(config, "insert into department values(2,'RESEARCH',1)");
            executeSql(config, "insert into department values(3,'SALES',1)");
        });
    }

    protected void executeSql(Config config, String sql) {
        UpdateBuilder builder = UpdateBuilder.newInstance(config);
        builder.sql(sql).execute();
    }

    public static void main(String[] args) throws Exception {
        new DepartmentApplication().run(args);
    }
}
