package fr.javatic.gradle.jbossController

import org.gradle.api.tasks.TaskAction

class JBossRemoveDatasource extends JBossControllerTask {
    String dsName

    JBossRemoveDatasource() {
    }

    def prepare() {
        if (dsName == null) {
            throw new IllegalArgumentException("""Missing task properties, the following must be set: dsName""")
        }

        jboss {
            // AuthDS
            address(["subsystem"     : "datasources",
                     "data-source": "${dsName}"]) {
                operation("remove")
            }
        }
    }

    @TaskAction
    def run() {
        prepare()
        super.run();
    }
}
