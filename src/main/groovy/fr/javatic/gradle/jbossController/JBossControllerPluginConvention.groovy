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

class JBossControllerPluginConvention {
    final Class JBossController = fr.javatic.gradle.jbossController.JBossControllerTask
    final Class JBossAddDatasource = fr.javatic.gradle.jbossController.JBossAddDatasource
    final Class JBossRemoveDatasource = fr.javatic.gradle.jbossController.JBossRemoveDatasource
    final Class JBossRemoveXADatasource = fr.javatic.gradle.jbossController.JBossRemoveXADatasource
    final Class JBossURLDeployTask = fr.javatic.gradle.jbossController.JBossURLDeployTask
    final Class JBossPrintDeploymentTask = fr.javatic.gradle.jbossController.JBossPrintDeploymentTask
}
