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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.as.controller.client.helpers.Operations
import org.jboss.dmr.ModelNode

import static org.jboss.as.controller.client.helpers.ClientConstants.*

class JBossControllerTask extends DefaultTask {
    String host
    int port

    String username
    String password

    private JBossDelegate delegate

    JBossControllerTask() {
        delegate = new JBossDelegate()
    }

    def jboss(Closure closure) {
        closure.delegate = this.delegate
        closure()
    }

    @TaskAction
    def run() {
        ModelControllerClient client = ModelControllerClient.Factory.create(host as String, port, new SimpleCallbackHandler(this.username, this.password));
        try {
            ModelNode steps = new ModelNode()
            this.delegate.toModelNode().each { node ->
                steps.add(node)
            }

            ModelNode mainOperation = Operations.createCompositeOperation()
            mainOperation.get(STEPS).set(steps)
            logger.debug("Operation: ${mainOperation}")

            ModelNode result = client.execute(mainOperation)
            logger.debug("Result: ${result}")
            if (!result.get(OUTCOME).asString().equals(SUCCESS)) {
                throw new TaskExecutionException(this, new JBossControllerException(result))
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
