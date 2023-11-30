package co.com.escuelaing.persistence.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import co.com.escuelaing.persistence.GitException;
import co.com.escuelaing.persistence.TemplateRepository;

@Repository
public class GitRepository implements TemplateRepository {

    private Git git;
    private static final String PATH = "/tmp/git";
    private Logger LOGGER = LogManager.getLogger();

    @Value("${project.template-repository}")
    private String repositoryURL;

    @Value("${project.repository-user}")
    private String username;

    @Value("${REPOSITORY_PASSWORD}")
    private String password;

    @Override
    public void saveTemplate(String template, String name) throws GitException {
        File file = new File(PATH);
        CredentialsProvider.setDefault(new UsernamePasswordCredentialsProvider(username, password));

        if (this.git == null) {
            try {
                File current_git = new File(PATH + "/.git");
                if (!current_git.exists()) {
                    this.git = Git.cloneRepository()
                            .setURI(repositoryURL)
                            .setDirectory(file)
                            .call();
                } else {
                    org.eclipse.jgit.lib.Repository repo = new RepositoryBuilder().setGitDir(new File(PATH + "/.git"))
                            .build();
                    this.git = new Git(repo);
                }
            } catch (GitAPIException e) {
                LOGGER.error(e);
                throw new GitException(e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.error(e);
                throw new GitException(e.getMessage(), e);
            }
        }

        try {
            git.checkout().setName("main").setStartPoint("origin main").setForced(true).call();
            git.pull();
        } catch (GitAPIException e) {
            LOGGER.error(e);
            throw new GitException(e.getMessage(), e);
        }

        this.writeTemplateToRepository(template, name);
    }

    private void writeTemplateToRepository(String content, String name) throws GitException {
        File file = new File(this.git.getRepository().getWorkTree(), name + ".json");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(content);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
            throw new GitException(e.getMessage(), e);
        }

        try {
            git.add().addFilepattern(name + ".json").call();
        } catch (GitAPIException e) {
            this.LOGGER.error(e);
            throw new GitException(e.getMessage(), e);
        }

        try {
            git.commit().setMessage("Added template " + name).call();
            Iterable<PushResult> result = git.push().call();
            System.out.println(name);
        } catch (GitAPIException e) {
            this.LOGGER.error(e);
            throw new GitException(e.getMessage(), e);
        }

    }

}
