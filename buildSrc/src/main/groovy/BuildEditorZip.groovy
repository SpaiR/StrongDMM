import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

class BuildEditorZip extends DefaultTask {
    String description = 'Creates release-ready editor archives for both OS.'

    @TaskAction
    void run() {
        def winRunDir = unzipRuntime('win-run')
        def linuxRunDir = unzipRuntime('linux-run')

        def jarFile = project.buildDir.toPath().resolve('libs/strongdmm.jar')

        copy(jarFile, linuxRunDir.resolve('lib'))
        copy(jarFile, winRunDir.resolve('app'))

        modifyWinCfg(winRunDir)
    }

    private Path unzipRuntime(String zipName) {
        def runZip = project.rootDir.toPath().resolve("buildSrc/resources/${zipName}.zip")
        def runDir = project.buildDir.toPath().resolve("tmp/runtime/$zipName")
        copy(project.zipTree(runZip), runDir)
        return runDir.resolve('strongdmm')
    }

    private void copy(from, to) {
        project.copy {
            it.from from
            it.into to
        }
    }

    private void modifyWinCfg(Path winRunDir) {
        def cfg = winRunDir.resolve('app/StrongDMM.cfg').toFile()
        cfg.write(cfg.text.replace('!version!', project.version.toString()))
    }
}
