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
 * Tags repository with {@link #tag(String)}. You can tag particular commit with {@link GitTagTask#commit(String)}.
 * Deletion of tag is done by {@link GitTagTask#delete()}, forcing by {@link GitTagTask#force} flags. You can not use force and delete flags together.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class GitTagTask extends Task<File, File> {

    private Logger logger = LoggerFactory.getLogger(GitTagTask)

    private boolean delete

    private boolean force

    private String commit

    private String tag = "unknown tag"

    GitTagTask delete() {
        delete = true
        this
    }

    GitTagTask force() {
        force = true
        this
    }

    /**
     *
     * @param commit hash of commit you want to tag, null values and empty strings are not taken into consideration.
     * @return this
     */
    GitTagTask commit(String commit) {
        if (notNullAndNotEmpty(commit)) {
            this.commit = commit
        }
        this
    }

    /**
     *
     * @param tag tag itself
     * @return this
     */
    GitTagTask tag(String tag) {
        this.tag = tag
        this
    }

    @Override
    protected File process(File repositoryDir) throws Exception {

        CommandBuilder commandBuilder = new CommandBuilder("git").parameter("tag")

        if (delete) {
            commandBuilder.parameter("-d")
        }

        if (force && !delete) {
            commandBuilder.parameter("-f")
        }

        commandBuilder.parameter(tag)

        if (commit && !delete) {
            commandBuilder.parameter(commit)
        }

        Command command = commandBuilder.build()

        logger.info(command.toString())

        try {
            Spacelift.task(CommandTool).workingDirectory(repositoryDir).command(command).execute().await()
        } catch (ExecutionException ex) {
            throw new ExecutionException(ex, "Could not add tag {0} using command {1}", tag, command.toString())
        }

        repositoryDir
    }

    private boolean notNullAndNotEmpty(String value) {
        value && !value.isEmpty()
    }
}
