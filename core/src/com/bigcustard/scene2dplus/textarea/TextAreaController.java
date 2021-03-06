package com.bigcustard.scene2dplus.textarea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bigcustard.scene2dplus.XY;
import com.bigcustard.scene2dplus.command.Command;
import com.bigcustard.scene2dplus.command.CommandHistory;
import com.bigcustard.scene2dplus.textarea.command.*;
import com.google.common.annotations.VisibleForTesting;

import java.awt.event.KeyEvent;

public class TextAreaController extends ClickListener {
    private TextAreaModel model;
    private ScrollableTextArea view;
    private CommandHistory commandHistory;
    private XY touchDownLocation;
    private boolean dragging;
    private Character lastCharacterTyped;

    public TextAreaController(TextAreaModel model, ScrollableTextArea view, CommandHistory commandHistory) {
        this.model = model;
        this.view = view;
        this.commandHistory = commandHistory;
    }

    @Override
    public boolean keyDown(InputEvent event, int keycode) {
        if (isRedo(keycode)) {
            commandHistory.redo();
            return true;
        } else if (isUndo(keycode)) {
            commandHistory.undo();
            return true;
        } else {
            Command command = getKeyDownCommand(keycode);
            return commandHistory.execute(command);
        }
    }

    @Override
    public boolean keyUp(InputEvent event, int keycode) {
        lastCharacterTyped = null;
        return false;
    }

    @Override
    public boolean keyTyped(InputEvent event, char character) {
        Command command = getKeyTypedCommand(character, event.getKeyCode());
        boolean processed = commandHistory.execute(command);
        view.onModelChange(model);
        return processed;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (isOver(event.getListenerActor(), x, y)) {
            XY caretLocation = view.worldPositionToCaretLocation(new XY((int) x, (int) y));
            if (dragging) {
                dragging = false;
                commandHistory.execute(new SelectCommand(model, touchDownLocation, caretLocation));
            } else {
                commandHistory.execute(new MoveToCommand(model, caretLocation));
            }
        }
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (isOver(event.getListenerActor(), x, y)) {
            model.caret().clearSelection();
            this.touchDownLocation = view.worldPositionToCaretLocation(new XY((int) x, (int) y));
            Stage stage = view.getStage();
            if (stage != null) stage.setKeyboardFocus(view);
        }
        return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        if (isOver(event.getListenerActor(), x, y)) {
            dragging = true;
            XY dragLocation = view.worldPositionToCaretLocation(new XY((int) x, (int) y));
            new SelectCommand(model, touchDownLocation, dragLocation).execute();
        }
    }

    private boolean isCopy(int keycode) {
        return isControlDown() && keycode == Input.Keys.C;
    }

    private boolean isCut(int keycode) {
        return isControlDown() && keycode == Input.Keys.X;
    }

    private boolean isPaste(int keycode) {
        return isControlDown() && keycode == Input.Keys.V;
    }

    private boolean isUndo(int keycode) {
        return isControlDown() && keycode == Input.Keys.Z && !isShiftDown();
    }

    private boolean isRedo(int keycode) {
        return isControlDown() && keycode == Input.Keys.Z && isShiftDown();
    }

    private Command getKeyDownCommand(int keycode) {
        if (isCopy(keycode)) {
            return new CopyCommand(model);
        } else if (isCut(keycode)) {
            return new CutCommand(model);
        } else if (isPaste(keycode)) {
            return new PasteCommand(model);
        } else if (keycode == Input.Keys.UP) {
            return new MoveUpCommand(model);
        } else if (keycode == Input.Keys.DOWN) {
            return new MoveDownCommand(model);
        } else if (keycode == Input.Keys.RIGHT) {
            return new MoveRightCommand(model);
        } else if (keycode == Input.Keys.LEFT) {
            return new MoveLeftCommand(model);
        }
        return null;
    }

    private Command getKeyTypedCommand(char character, int keycode) {
        if (Key.Delete.is(character)) {
            return new DeleteCommand(model);
        } else if (Key.Return.is(character)) {
            return new ReturnCommand(model);
        } else if (Key.Tab.is(character)) {
            return new TabCommand(model);
        } else if (isPrintableChar(character, keycode)) {
            if (lastCharacterTyped == null || lastCharacterTyped != character) {
                lastCharacterTyped = character;
                return new TypeCommand(model, Character.toString(character));
            }
        }
        return null;
    }

    private boolean isPrintableChar(char character, int keyCode) {
        return character >= 32 && character < 127;
    }

    @VisibleForTesting
    protected boolean isControlDown() {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    @VisibleForTesting
    protected boolean isShiftDown() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }
}