package sumdu.edu.ua.core.service;

import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

import java.time.Duration;
import java.time.Instant;

public class CommentService {
    private final CommentRepositoryPort repo;

    public CommentService(CommentRepositoryPort repo) {
        this.repo = repo;
    }

    /**
     * Deletes a comment only if it was created not more than 24 hours ago.
     *
     * @throws IllegalStateException if the comment was not found or is older than 24 hours
     */
    public void delete(long bookId, long commentId) {
        Comment comment = repo.findById(bookId, commentId);
        if (comment == null) {
            throw new IllegalStateException("The comment was not found");
        }

        Instant createdAt = comment.getCreatedAt();
        if (createdAt == null) {
            throw new IllegalStateException("The time of creation of the comment is unknown");
        }

        if (Duration.between(createdAt, Instant.now()).toHours() > 24) {
            throw new IllegalStateException("You can't delete a comment that is older than 24 hours");
        }

        repo.delete(bookId, commentId);
    }
}

