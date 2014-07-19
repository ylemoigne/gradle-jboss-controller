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

class JBossRemoveXADatasource extends JBossControllerTask {
    String dsName

    JBossRemoveXADatasource() {
    }

    def prepare() {
        if (dsName == null) {
            throw new IllegalArgumentException("""Missing task properties, the following must be set: dsName""")
        }

        jboss {
            // AuthDS
            address(["subsystem"     : "datasources",
                     "xa-data-source": "${dsName}"]) {
                operation("remove")
            }
        }
    }

    @TaskAction
    def run() {
        prepare()
        super.run();
    }
}
