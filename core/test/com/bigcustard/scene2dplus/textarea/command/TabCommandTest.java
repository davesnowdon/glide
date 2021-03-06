package com.bigcustard.scene2dplus.textarea.command;

import com.bigcustard.scene2dplus.textarea.TextAreaModel;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TabCommandTest {
    private TextAreaModel model;
    private TabCommand command;

    @Before
    public void before() {
        model = new TextAreaModel("", null);
    }

    @Test
    public void executeInColumnZeroOnEmptyLine() {
        command = new TabCommand(model);
        command.execute();
        assertThat(model.text()).isEqualTo("    ");
    }

    @Test
    public void executeInColumnTwoOnEmptyLine() {
        model.insert("ab");
        command = new TabCommand(model);
        command.execute();
        assertThat(model.text()).isEqualTo("ab  ");
    }

    @Test
    public void executeInColumnZeroOnNonEmptyLine() {
        model.setText("abcd");
        command = new TabCommand(model);
        command.execute();
        assertThat(model.text()).isEqualTo("    abcd");
    }

    @Test
    public void executeInColumnTwoOnNonEmptyLine() {
        model.setText("abcd");
        model.caret().moveRight(2);
        command = new TabCommand(model);
        command.execute();
        assertThat(model.text()).isEqualTo("ab  cd");
    }

    @Test
    public void undoInColumnZeroOnEmptyLine() {
        command = new TabCommand(model);
        command.execute();
        command.undo();
        assertThat(model.text()).isEqualTo("");
    }

    @Test
    public void undoInColumnTwoOnEmptyLine() {
        model.insert("ab");
        command = new TabCommand(model);
        command.execute();
        command.undo();
        assertThat(model.text()).isEqualTo("ab");
    }

    @Test
    public void undoInColumnZeroOnNonEmptyLine() {
        model.setText("abcd");
        command = new TabCommand(model);
        command.execute();
        command.undo();
        assertThat(model.text()).isEqualTo("abcd");
    }

    @Test
    public void undoInColumnTwoOnNonEmptyLine() {
        model.setText("abcd");
        model.caret().moveRight(2);
        command = new TabCommand(model);
        command.execute();
        command.undo();
        assertThat(model.text()).isEqualTo("abcd");
    }
}
