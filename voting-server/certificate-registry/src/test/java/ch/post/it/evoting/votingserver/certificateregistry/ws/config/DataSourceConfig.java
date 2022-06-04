/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.ws.config;

import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
@DataSourceDefinition(user = "sa", password = "", className = "org.h2.Driver", name = "crDB", databaseName = "crDB", url = "jdbc:h2:./target/h2-test;MODE=ORACLE", properties = {
		"logSql=true" })
public class DataSourceConfig {

}
