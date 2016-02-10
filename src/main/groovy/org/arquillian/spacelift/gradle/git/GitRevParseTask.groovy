package org.arquillian.spacelift.gradle.git

import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.execution.ExecutionException
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.task.Task
import org.arquillian.spacelift.task.os.CommandTool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Finds SHA1 of a given revision. Returns {@code null} if no such revision was found.
 * @author kpiwko
 *
 */
class GitRevParseTask extends Task<File, String> {

    private Logger logger = LoggerFactory.getLogger(GitRevParseTask)

    private String rev

    /**
     * Revision to search for. Can be commit sha1, 'HEAD', 'master', in short
     * any format supported by git rev-parse tool
     * @param rev
     * @return
     */
    GitRevParseTask rev(String rev) {
        this.rev = rev
        this
    }

    @Override
    protected String process(File repositoryDir) throws Exception {

        CommandBuilder command = new CommandBuilder("git")
                .parameter("rev-parse")
                .parameter(rev)

        logger.info(command.toString())

        try {
            List<String> output = Spacelift.task(CommandTool)
                    .workingDirectory(repositoryDir)
                    .shouldExitWith(0, 128)
                    .command(command)
                    .execute()
                    .await()
                    .output()

            if (output && output.size() == 1) {
                return output.get(0);
            }
            // no such sha was found
            null
        }
        catch (ExecutionException ex) {
            throw new ExecutionException(ex, "Unable to get rev sha of '{0}' at '{1}'. Command '{2}.",
                    rev, repositoryDir.getCanonicalPath(), command.toString())
        }
    }
}
