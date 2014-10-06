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
            final List<ModelNode> deployments = Operations.readResult(listDeploymentsResult).asList();
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
