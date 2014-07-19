/*
 * Copyright 2014 Yann Le Moigne
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
