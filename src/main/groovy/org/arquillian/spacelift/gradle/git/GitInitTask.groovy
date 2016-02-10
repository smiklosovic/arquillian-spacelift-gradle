package org.arquillian.spacelift.gradle.git

import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.execution.ExecutionException
import org.arquillian.spacelift.process.Command
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.task.Task
import org.arquillian.spacelift.task.os.CommandTool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Initializes repository. Initialized repository is bare.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class GitInitTask extends Task<File, String> {

    private Logger logger = LoggerFactory.getLogger(GitInitTask)

    @Override
    protected String process(File repositoryDir) throws Exception {

        Command command = new CommandBuilder("git").parameters("init", "--bare", repositoryDir.getAbsolutePath()).build()

        logger.info(command.toString())

        try {
            Spacelift.task(CommandTool).command(command).execute().await()
        } catch (ExecutionException ex) {
            throw new ExecutionException(ex, "Unable to initialize repository at {0}", repositoryDir.getAbsolutePath())
        }

        repositoryDir.canonicalPath
    }
}
