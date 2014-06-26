gradle-jboss-controller
================
Basic support for controlling a jboss instance.

Warning
=======
This is my very first prototype, not yet idiomatic groovy source code and supporting only jboss-eap-6.2.0
At this time, you need to checkout and build the plugin by yourself

Usage
=====
    buildscript {
        repositories {
            mavenLocal()
        }

        dependencies {
            classpath 'fr.javatic.gradle:gradle-jboss-controller:0.1-SNAPSHOT'
        }
    }

    apply plugin: 'jboss-controller'

    task configureJBoss(type: JBossController) {
        host = managedConfig.defaultServeurHost
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