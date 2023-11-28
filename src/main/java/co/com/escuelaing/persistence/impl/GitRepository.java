package co.com.escuelaing.persistence.impl;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerRegistry;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Repository;

import co.com.escuelaing.persistence.TemplateRepository;

@Repository
public class GitRepository implements TemplateRepository{

    private Git git;
    private static final String PATH = "/tmp/git";
    private Logger LOGGER = LogManager.getLogger();

    @Override
    public void saveTemplate(String template, String name) {
        File file = new File(PATH);
        CredentialsProvider.setDefault(new UsernamePasswordCredentialsProvider("alejovasquero", ""));

        if (this.git == null) {
            try {
                File current_git = new File(PATH + "/.git");
                if (!current_git.exists()){
                    this.git = Git.cloneRepository()
                        .setURI("https://github.com/AYGO-INFRAESTRUCTURE-PROJECT/API-TEMPLATE-GENERATOR")
                        .setDirectory(file)
                        .call();
                } else {
                    org.eclipse.jgit.lib.Repository repo = new RepositoryBuilder().setGitDir(new File(PATH + "/.git")).build();
                    this.git = new Git(repo);
                }
            } catch (GitAPIException e) {
                LOGGER.error(e);
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error(e);
                e.printStackTrace();
            }
        }


        try {
            git.checkout().setName("master").setStartPoint("origin/master").call();
            git.pull();
        } catch (GitAPIException e) {
            LOGGER.error(e);
            e.printStackTrace();
        }

        this.writeTemplateToRepository(template, name);
    }


    private void writeTemplateToRepository(String content, String name) {
        File file = new File(this.git.getRepository().getWorkTree(), name + ".json");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(content);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
            e.printStackTrace();
        }

        try {
            git.add().addFilepattern(name + ".json").call();
        } catch (GitAPIException e) {
            this.LOGGER.error(e);
            e.printStackTrace();
        }

        try {
            git.commit().setMessage("Added template " + name).call();
            git.push().call();
        } catch (GitAPIException e) {
            this.LOGGER.error(e);
            e.printStackTrace();
        }

    }
    
}
