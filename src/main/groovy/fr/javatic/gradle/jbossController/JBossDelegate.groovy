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

import org.jboss.dmr.ModelNode

import static org.jboss.as.controller.client.helpers.ClientConstants.OP
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR

class JBossDelegate {
    private List<Address> addresses

    JBossDelegate() {
        this.addresses = new ArrayList<>()
    }

    def List<ModelNode> toModelNode() {
        addresses.collect { it.toModelNode() }.flatten()
    }

    def void address(Map<String, String> address, Closure closure) {
        ModelNode addressNode = new ModelNode()
        address.each { k, v ->
            addressNode.add(k, v)
        }

        Address addressModel = new Address(addressNode)
        closure.delegate = addressModel
        closure()

        addresses.add(addressModel)
    }

    private static class Address {
        private ModelNode addressNode
        private List<Operation> operations

        Address(ModelNode addressNode) {
            this.addressNode = addressNode
            this.operations = new ArrayList<>()
        }

        def void operation(String operation, Map<String, Object> actionParameter) {
            operations.add(new Operation(operation, actionParameter))
        }

        def void operation(String operation) {
            operations.add(new Operation(operation, [:]))
        }

        def List<ModelNode> toModelNode() {
            operations.collect { op ->
                ModelNode operationNode = op.toModelNode()
                operationNode.get(OP_ADDR).set(addressNode)
                return operationNode
            }
        }

        private static class Operation {
            private String name
            private Map<String, Object> parameters

            Operation(String name, Map<String, Object> parameters) {
                this.name = name
                this.parameters = parameters
            }

            def ModelNode toModelNode() {
                ModelNode node = new ModelNode()
                node.get(OP).set(name)
                this.parameters.each { k, v ->
                    if (v instanceof Map ||
                            v instanceof List) {
                        node.get(k).set(buildNode(v))
                    } else {
                        node.get(k).set(v)
                    }
                }
                return node
            }

            private def buildNode(Map<String, Object> map) {
                ModelNode node = new ModelNode()
                map.each { k, v ->
                    if (v instanceof Map ||
                            v instanceof List) {
                        node.get(k).set(buildNode(v))
                    } else {
                        node.get(k).set(v)
                    }
                }
                return node
            }

            private def buildNode(List<Object> list) {
                ModelNode node = new ModelNode()
                list.each { v ->
                    if (v instanceof Map ||
                            v instanceof List) {
                        node.add(buildNode(v))
                    } else {
                        node.add(v)
                    }
                }
                return node
            }

        }
    }
}
