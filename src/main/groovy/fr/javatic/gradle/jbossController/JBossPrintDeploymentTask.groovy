package fr.javatic.gradle.jbossController

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.jboss.as.controller.client.ModelControllerClient

class JBossPrintDeploymentTask extends DefaultTask {
    String host
    int port

    String username
    String password

    @TaskAction
    def run() {
        ModelControllerClient client = ModelControllerClient.Factory.create(host as String, port, new SimpleCallbackHandler(this.username, this.password));
        println("###START###")
        String deployments = DeploymentInspector.getDeployments(client).join("\n%%%\n")
        println(deployments)
        println("###END###")
    }
}
