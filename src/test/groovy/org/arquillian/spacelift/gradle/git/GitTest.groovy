package org.arquillian.spacelift.gradle.git

import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.task.os.CommandTool
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class GitTest {

    private static final String testRepository = "ssh://git@github.com/smiklosovic/test.git"

    @Test
    void initAndBranchGitTest() {

        File repositoryInitDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString())

        File repositoryCloneDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString())

        Spacelift.task(repositoryInitDir, GitInitTask).then(GitCloneTask).destination(repositoryCloneDir).execute().await()

        // We need to configure the user, otherwise it git will be failing with exit code 128
        Spacelift.task(CommandTool)
                .workingDirectory(repositoryCloneDir)
                .programName('git')
                .parameters('config', 'user.email', 'spacelift@arquillian.org').execute().await()

        Spacelift.task(CommandTool)
                .workingDirectory(repositoryCloneDir)
                .programName('git')
                .parameters('config', 'user.name', 'Arquillian Spacelift')
                .execute().await()

        File dummyFile1 = new File(repositoryCloneDir, "dummyFile")
        File dummyFile2 = new File(repositoryCloneDir, "dummyFile2")

        dummyFile1.createNewFile()
        dummyFile2.createNewFile()

        // this will not be added
        File outsideOfRepository = new File(repositoryCloneDir.getParentFile(), UUID.randomUUID().toString())
        outsideOfRepository.createNewFile()

        File outsideOfRepositoryRelative = new File(repositoryCloneDir, "../" + UUID.randomUUID().toString())
        outsideOfRepositoryRelative.createNewFile()

        Spacelift.task(repositoryCloneDir, GitAddTask).add([dummyFile1, dummyFile2, outsideOfRepository])
                .then(GitCommitTask).message("added some files")
                .then(GitPushTask).branch("master")
                .then(GitBranchTask.class).branch("dummyBranch")
                .then(GitCheckoutTask.class).checkout("dummyBranch")
                .execute().await()


        File dummyFile3 = new File(repositoryCloneDir, "dummyFile3")
        File dummyFile4 = new File(repositoryCloneDir, "dummyFile4")

        dummyFile3.createNewFile()
        dummyFile4.createNewFile()

        Spacelift.task(repositoryCloneDir, GitAddTask).add([dummyFile3, dummyFile4, outsideOfRepositoryRelative])
                .then(GitRemoveTask).force().remove(dummyFile3).remove([outsideOfRepository, outsideOfRepositoryRelative])
                .then(GitCommitTask).message("deleted dummyFile3 and added dummyFile4")
                .then(GitTagTask).tag("1.0.0")
                .then(GitPushTask).tags().branch("dummyBranch")
                .execute().await()

        File repository2CloneDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString())

        Spacelift.task(repositoryInitDir.canonicalPath, GitCloneTask).destination(repository2CloneDir)
                .then(GitFetchTask)
                .then(GitCheckoutTask).checkout("dummyBranch")
                .execute().await()

        assertFalse(dummyFile3.exists())
        assertTrue(dummyFile4.exists())
    }

    /**
     * Ignored since it needs specific repository to clone from and push to and this would require
     * private SSH key
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void testGit() throws IOException {

        File repositoryCloneDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString())

        File gitSshFile = Spacelift.task(GitSshFileTask).execute().await()

        Spacelift.task(testRepository, GitCloneTask).gitSsh(gitSshFile).destination(repositoryCloneDir)
                .then(GitFetchTask)
                .then(GitBranchTask).branch("dummyBranch")
                .then(GitCheckoutTask).checkout("dummyBranch")
                .execute().await()

        File file1 = new File(repositoryCloneDir, UUID.randomUUID().toString())
        File file2 = new File(repositoryCloneDir, UUID.randomUUID().toString())

        file1.createNewFile()
        file2.createNewFile()

        Spacelift.task(repositoryCloneDir, GitAddTask).add([file1, file2])
                .then(GitCommitTask).message("added some files")
                .then(GitPushTask).remote("origin").branch("dummyBranch")
                .then(GitPushTask).remote("origin").branch(":dummyBranch") // delete that branch
                .execute().await()
    }
}