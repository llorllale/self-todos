/**
 * Copyright (c) 2020, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.todos;

import com.jcabi.ssh.Shell;
import com.selfxdsd.api.Project;
import org.cactoos.io.DeadInput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

/**
 * Representation of pdd puzzles from processing a SSH command.
 * @author criske
 * @version $Id$
 * @since 0.0.1
 * @todo #7:30min Write unit test for SshPuzzles and unit DocumentPuzzles
 *  for cases when there are invalid documents.
 */
public final class SshPuzzles implements Puzzles<Project> {

    /**
     * Next puzzles for processing.
     */
    private final Puzzles<String> next;

    /**
     * SSH.
     */
    private final Shell ssh;

    /**
     * Ctor.
     * @param ssh SSH.
     * @param next Next puzzles for processing.
     */
    public SshPuzzles(final Shell ssh,
                      final Puzzles<String> next) {
        this.ssh = ssh;
        this.next = next;
    }

    @Override
    public void process(final Project project)
        throws PuzzlesProcessingException {
        try {
            final String id = UUID.randomUUID().toString().replace("-", "");
            this.exec(
                String.format(
                    Files.readString(
                        Path.of("src/main/resources/cloneRepoAndPdd.sh")
                    ),
                    id, id, project.provider(), project.repoFullName()
                )
            );
            final String puzzles = this.exec(
                "cd self-todos-tmp-" + id + "/repo"
                + " && cat ./puzzles.xml");
            this.next.process(puzzles);
        } catch (final IOException exception) {
            throw new PuzzlesProcessingException(exception);
        }
    }

    /**
     * Just exec.
     * @param cmd Command
     * @return Stdout
     * @throws IOException If fails
     */
    public String exec(final String cmd) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int exit = this.ssh.exec(cmd, new DeadInput().stream(), baos, baos);
        System.out.println("EXIT CODE >>>> " + exit);
        return baos.toString(StandardCharsets.UTF_8.toString());
    }

    @Override
    public Iterator<Puzzle> iterator() {
        return next.iterator();
    }
}
