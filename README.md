gradle-jboss-controller
================
Basic support for controlling a jboss instance.

Changelog
=========
Fix auto import for JBossAddXADatasource task.

Usage
=====

Basic with Gradle 2.0
-----------

    buildscript {
        repositories {
            jcenter()
            // or mavenCentral()
        }

        dependencies {
            classpath 'fr.javatic.gradle:gradle-jboss-controller:1.2'
        }
    }

    apply plugin: 'fr.javatic.jboss-controller'

Basic with Gradle 2.1
-----------

    plugins {
      id "fr.javatic.jboss-controller" version "1.1"
    }

Basic Task
----------

    task installFooDs(type: JBossAddDatasource) { // You use the type JBossAddXADatasource
        host = jbossHost
        port = portManagementNative

        username = jbossAdminLogin
        password = jbossAdminPassword

        dsName = "foo"
        dsUser = defaultAuthBddUser
        dsPassword = defaultAuthBddPassword
        dsDriver = "mysql"
        dsJdbcUrl = "jdbc:mysql://localhost:3306/foo"
    }

    task removeSampleDatasource(type: JBossRemoveDatasource) { // or JBossRemoveXADatasource
        host = jbossHost
        port = portManagementNative

        username = jbossAdminLogin
        password = jbossAdminPassword

        dsName = "ExampleDS"
    }

    task deployMyWebapp(type: JBossURLDeployTask) {
        host = jbossHost
        port = portManagementNative

        username = jbossAdminLogin
        password = jbossAdminPassword

        url = new URL("file://Users/ylemoigne/MyWebapp.war")
    }

    task printDeployments(type: JBossPrintDeploymentTask) {
        mustRunAfter startInstance

        host = jbossHost
        port = portManagementNative

        username = jbossAdminLogin
        password = jbossAdminPassword
    }

Advanced Task
-------------
This task use the wrap a micro-dsl around jboss management model api. This allow you to alter almost any JBoss configuration parameter.

    task configureJBoss(type: JBossController) {
        host = 'localhost'
        port = 9999

        username = "foo"
        password = "bar"

        jboss {
            address(["subsystem": "deployment-scanner",
                     "scanner"  : "default"]) {
                operation("remove")
            }

            address(["subsystem": "deployment-scanner"]) {
                operation("remove")
            }

            address(["extension": "org.jboss.as.deployment-scanner"]) {
                operation("remove")
            }

            address(["subsystem"      : "security",
                     "security-domain": "SampleRealm",
                     "authentication" : "classic"]) {
                operation("add", ["login-modules": [
                        [
                                "code"          : "Remoting",
                                "flag"          : "optional",
                                "module-options": ["password-stacking": "useFirstPass"]
                        ],
                        [
                                "code"          : "org.jboss.security.auth.spi.DatabaseServerLoginModule",
                                "flag"          : "required",
                                "module-options": [
                                        "dsJndiName"     : "java:/jdbc/myDS",
                                        "principalsQuery": "select PASSWORD as 'Password' from USERS where LOGIN=?",
                                        "rolesQuery"     : "select ROLE as 'Role', 'Roles' as 'RoleGroup' from USERS where LOGIN=?"
                                ]
                        ]
                ]
                ])
            }
        }
    }

