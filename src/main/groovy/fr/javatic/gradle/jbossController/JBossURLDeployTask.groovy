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
import org.jboss.as.controller.client.helpers.standalone.*

class JBossURLDeployTask extends DefaultTask {
    String host
    int port

    String username
    String password

    URL url
    String deployName

    JBossURLDeployTask() {
    }

    @TaskAction
    def run() {
        String deploymenName = this.deployName != null ? this.deployName : url.path.substring(url.path.lastIndexOf("/") + 1)

        ModelControllerClient client = ModelControllerClient.Factory.create(host as String, port, new SimpleCallbackHandler(this.username, this.password));
        boolean isRedeploy = DeploymentInspector.getDeployments(client).contains(deploymenName)

        try {
            ServerDeploymentManager serverDeploymentManager = ServerDeploymentManager.Factory.create(client)

            DeploymentPlan plan = null;
            if (isRedeploy) {
                logger.info("Redeploy '${url}' as '${deploymenName}'" as String)
                DeploymentPlanBuilder builder = serverDeploymentManager.newDeploymentPlan().replace(deploymenName, url).redeploy(deploymenName)
                plan = builder.build()
            } else {
                logger.info("Deploy '${url}' as '${deploymenName}'" as String)
                DeploymentPlanBuilder builder = serverDeploymentManager.newDeploymentPlan().add(url).deploy(deploymenName)
                plan = builder.build()
            }

            ServerDeploymentPlanResult planResult = serverDeploymentManager.execute(plan).get()
            for (DeploymentAction action : plan.getDeploymentActions()) {
                final ServerDeploymentActionResult actionResult = planResult.getDeploymentActionResult(action.getId());
                final ServerUpdateActionResult.Result result = actionResult.getResult();
                switch (result) {
                    case ServerUpdateActionResult.Result.FAILED:
                        logger.error("FAILED", actionResult.getDeploymentException())
                        throw new TaskExecutionException(this, actionResult.getDeploymentException());
                    case ServerUpdateActionResult.Result.NOT_EXECUTED:
                        logger.error("NOT_EXECUTED", actionResult.getDeploymentException())
                        throw new TaskExecutionException(this, actionResult.getDeploymentException());
                    case ServerUpdateActionResult.Result.ROLLED_BACK:
                        logger.error("ROLLED_BACK", actionResult.getDeploymentException())
                        throw new TaskExecutionException(this, actionResult.getDeploymentException());
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
