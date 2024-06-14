package com.sparta.oneandzerobest.comment;

import com.sparta.oneandzerobest.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
public class CommentTest {

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void setModifiedAt(){


    }

}
