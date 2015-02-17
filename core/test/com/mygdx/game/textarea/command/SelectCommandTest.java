package com.mygdx.game.textarea.command;

import com.mygdx.game.XY;
import com.mygdx.game.textarea.TextAreaModel;
import com.mygdx.game.textarea.XYAssert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectCommandTest {
    private TextAreaModel model;
    private SelectCommand command;

    @Before
    public void before() {
        model = new TextAreaModel("hello\nthere", null);
        command = new SelectCommand(model, new XY<Integer>(3, 0), new XY<Integer>(2, 1));
    }

    @Test
    public void execute() {
        command.execute();
        assertThat(model.caret().isAreaSelected()).isTrue();
        XYAssert.assertThat(model.caret().selection().getLeft()).at(3, 0);
        XYAssert.assertThat(model.caret().selection().getRight()).at(2, 1);
    }
}