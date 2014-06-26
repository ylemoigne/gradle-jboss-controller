package fr.javatic.gradle.jbossController

import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.as.controller.client.helpers.Operations
import org.jboss.dmr.ModelNode

import static org.jboss.as.controller.client.helpers.ClientConstants.*

class DeploymentInspector {
    public static List<String> getDeployments(ModelControllerClient client) throws IOException {
        final ModelNode op = Operations.createOperation(READ_CHILDREN_NAMES_OPERATION);
        op.get(CHILD_TYPE).set(DEPLOYMENT);

        final ModelNode listDeploymentsResult = client.execute(op);
        final List<String> result = new ArrayList<String>();
        if (Operations.isSuccessfulOutcome(listDeploymentsResult)) {
            final List<ModelNode> deployments = readResult(listDeploymentsResult).asList();
            for (ModelNode n : deployments) {
                result.add(n.asString());
            }
        } else {
            throw new IllegalStateException(getFailureDescriptionAsString(listDeploymentsResult));
        }

        Collections.sort(result);
        return result;

    }

    public static String getFailureDescriptionAsString(final ModelNode result) {
        if (Operations.isSuccessfulOutcome(result)) {
            return "";
        }

        if (result.hasDefined(FAILURE_DESCRIPTION)) {
            if (result.hasDefined(OP)) {
                return String.format("Operation '%s' at address '%s' failed: %s", result.get(OP), result.get(OP_ADDR), result
                        .get(FAILURE_DESCRIPTION));
            } else {
                return String.format("Operation failed: %s", result.get(FAILURE_DESCRIPTION));
            }
        }
        return String.format("An unexpected response was found checking the deployment. Result: %s", result);
    }
}
