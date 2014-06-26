package fr.javatic.gradle.jbossController

import org.gradle.api.tasks.TaskAction

class JBossAddDatasource extends JBossControllerTask {
    String dsName
    String dsUser
    String dsPassword
    String dsDriverName
    String dsJdbcurl

    JBossAddDatasource() {
    }

    def prepare() {
        if (dsName == null ||
                dsUser == null ||
                dsPassword == null ||
                dsDriverName == null ||
                dsJdbcurl == null) {
            throw new IllegalArgumentException("""Missing task properties, the following must be set: dsName, dsUser, dsPassword, dsDriverName, dsJdbcurl""")
        }

        jboss {
            // AuthDS
            address(["subsystem"  : "datasources",
                     "data-source": "${dsName}"]) {
                operation("add", [
                        "enabled"                     : true,
                        "jndi-name"                   : "java:/jdbc/${dsName}",
                        "driver-name"                 : dsDriverName,
                        "validate-on-match"           : false,
                        "background-validation"       : false,
                        "background-validation-millis": 500L,
                        "user-name"                   : dsUser,
                        "password"                    : dsPassword,
                ])
            }

            address(["subsystem"            : "datasources",
                     "data-source"          : "${dsName}",
                     "datasource-properties": "URL"]) {
                operation("add", ["value": dsJdbcurl])
            }
        }
    }

    @TaskAction
    def run() {
        prepare()
        super.run();
    }
}
