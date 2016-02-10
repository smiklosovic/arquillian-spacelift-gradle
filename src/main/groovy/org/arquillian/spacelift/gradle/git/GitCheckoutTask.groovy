package org.arquillian.spacelift.gradle.git

import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.execution.ExecutionException
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.task.Task
import org.arquillian.spacelift.task.os.CommandTool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Checkouts some branch. If {@link GitCheckoutTask#checkout(String)} is not called, it is checked out to master.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class GitCheckoutTask extends Task<File, File> {

    private static Logger logger = LoggerFactory.getLogger(GitCheckoutTask)

    String branch = "master"

    boolean force = false

    /**
     *
     * @param branch , branch to check out, it is skipped when it is null object or it is empty string.
     * @return
     */
    GitCheckoutTask checkout(String branch) {
        if (notNullAndNotEmpty(branch)) {
            this.branch = branch
        }
        this
    }

    GitCheckoutTask force() {
        this.force = true
        this
    }

    @Override
    protected File process(File repositoryDir) throws Exception {

        CommandBuilder command = new CommandBuilder("git")
                .parameter("checkout")
                .parameter(branch)

        if (force) {
            command.parameter('--force')
        }

        logger.info(command.toString())

        try {
            Spacelift.task(CommandTool).workingDirectory(repositoryDir).command(command).execute().await()
        } catch (ExecutionException ex) {
            throw new ExecutionException(
                    ex, "Checking out branch '{0}' in repository '{1}' was not successful. Command '{2}'.", branch,
                    repositoryDir.getAbsolutePath(), command.toString())
        }

        repositoryDir
    }

    private boolean notNullAndNotEmpty(String value) {
        value && !value.isEmpty()
    }
}
