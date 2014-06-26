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
import org.jboss.dmr.ModelNode

import javax.security.auth.callback.*
import javax.security.sasl.RealmCallback

class JBossControllerTask extends DefaultTask {
    String host
    int port

    String username
    String password

    JBossDelegate delegate

    JBossControllerTask() {
        delegate = new JBossDelegate()
    }

    def jboss(Closure closure) {
        closure.delegate = this.delegate
        closure()
    }

    @TaskAction
    def run() {
        ModelControllerClient client = ModelControllerClient.Factory.create(host, port, new SimpleCallbackHandler(this.username, this.password));
        try {
            ModelNode steps = new ModelNode()
            this.delegate.toModelNode().each { node ->
                steps.add(node)
            }

            ModelNode mainOperation = new ModelNode()
            mainOperation.get("operation").set("composite")
            mainOperation.get("steps").set(steps)
            logger.debug("Operation: ${mainOperation}")

            ModelNode result = client.execute(mainOperation)
            logger.debug("Result: ${result}")
            if (!result.get("outcome").asString().equals("success")) {
                throw new TaskExecutionException(this, new JBossControllerException(result))
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private static class SimpleCallbackHandler implements CallbackHandler {
        private String username
        private String password

        SimpleCallbackHandler(String username, String password) {
            this.username = username
            this.password = password
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback current : callbacks) {
                if (current instanceof NameCallback) {
                    NameCallback ncb = (NameCallback) current;
                    ncb.setName(username);
                } else if (current instanceof PasswordCallback) {
                    PasswordCallback pcb = (PasswordCallback) current;
                    pcb.setPassword(password.toCharArray());
                } else if (current instanceof RealmCallback) {
                    RealmCallback rcb = (RealmCallback) current;
                    rcb.setText(rcb.getDefaultText());
                } else {
                    throw new UnsupportedCallbackException(current);
                }
            }
        }
    }
}
