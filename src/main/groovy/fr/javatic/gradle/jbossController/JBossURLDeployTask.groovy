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
        String deploymentName = getDeployName() ?: deployNameFromPath()

        ModelControllerClient client = getClient();
        try {
            ServerDeploymentManager serverDeploymentManager = ServerDeploymentManager.Factory.create(client)

            DeploymentPlan plan = null;
            if (isRedeploy(client, deploymentName)) {
                logger.info("Redeploy '${url}' as '${deploymentName}'" as String)
                DeploymentPlanBuilder builder = serverDeploymentManager.newDeploymentPlan().replace(deploymentName, getUrl()).redeploy(deploymentName)
                plan = builder.build()
            } else {
                logger.info("Deploy '${url}' as '${deploymentName}'" as String)
                DeploymentPlanBuilder builder = serverDeploymentManager.newDeploymentPlan().add(getUrl()).deploy(deploymentName)
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

    private String deployNameFromPath() {
        getUrl().path.substring(getUrl().path.lastIndexOf("/") + 1)
    }

    private ModelControllerClient getClient() {
        ModelControllerClient.Factory.create(getHost() as String, getPort(), new SimpleCallbackHandler(getUsername(),
                getPassword()))
    }

    private static boolean isRedeploy(ModelControllerClient client, String deploymentName) {
        DeploymentInspector.getDeployments(client).contains(deploymentName)
    }
}
