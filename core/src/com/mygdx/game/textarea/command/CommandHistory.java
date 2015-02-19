package com.mygdx.game.textarea.command;

import java.util.LinkedList;

public class CommandHistory {
    private LinkedList<Command> executedCommands = new LinkedList<Command>();
    private int lastCommandIndex = -1;

    public void redo() {
        if (lastCommandIndex < executedCommands.size() - 1) {
            Command nextCommand = executedCommands.get(lastCommandIndex + 1);
            lastCommandIndex++;
            nextCommand.execute();
        }
    }

    public void undo() {
        if (lastCommandIndex >= 0) {
            Command lastCommand = executedCommands.get(lastCommandIndex);
            lastCommandIndex--;
            lastCommand.undo();
        }
    }

    public void execute(Command command) {
        if (command != null) {
            clearRedoChain();
            executedCommands.add(command);
            lastCommandIndex++;
            command.execute();
        }
    }

    private void clearRedoChain() {
        for (int i = lastCommandIndex + 1; i < executedCommands.size(); i++) {
            executedCommands.removeLast();
        }
    }
}
