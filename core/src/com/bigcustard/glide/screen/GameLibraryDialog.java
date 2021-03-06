package com.bigcustard.glide.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;
import com.bigcustard.glide.code.Game;
import com.bigcustard.glide.code.GameStore;
import com.bigcustard.scene2dplus.Spacer;
import com.bigcustard.scene2dplus.button.ImageButtonPlus;
import com.bigcustard.scene2dplus.button.ImageTextButtonPlus;
import com.bigcustard.scene2dplus.button.TextButtonPlus;
import com.bigcustard.scene2dplus.dialog.ErrorDialog;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;

import java.util.List;
import java.util.function.Consumer;

public class GameLibraryDialog extends Dialog implements Disposable {
    private static int COLUMNS = 2;
    private static List<Game.Token> games;
    private SettableFuture<Game.Token> futureGame = SettableFuture.create();
    private boolean readOnly;

    public static GameLibraryDialog userGames(Skin skin) {
        GameLibraryDialog dialog = new GameLibraryDialog(skin);
        games = new GameStore().allUserGames();
        dialog.layoutControls(skin, false);
        return dialog;
    }

    public static GameLibraryDialog sampleGames(Skin skin) {
        GameLibraryDialog dialog = new GameLibraryDialog(skin);
        games = new GameStore().allSampleGames();
        dialog.layoutControls(skin, true);
        return dialog;
    }

    private GameLibraryDialog(Skin skin) {
        super("", skin);
    }

    public void display(Stage stage, Runnable onCancel, Consumer<Game.Token> handleChoice) {
        show(stage);
        Futures.addCallback(getFutureGame(), new FutureCallback<Game.Token>() {
            public void onSuccess(Game.Token game) {
                if (game != null) {
                    remove();
                    dispose();
                    handleChoice.accept(game);
                } else {
                    onCancel.run();
                }
            }

            public void onFailure(Throwable e) {
                new ErrorDialog(e, onCancel, getSkin()).show(stage);
            }
        });
    }

    public SettableFuture<Game.Token> getFutureGame() {
        return futureGame;
    }

    @Override
    protected void result(Object object) {
        Game.Token selected = (Game.Token) object;
        if (object != null && readOnly) {
            GameStore gameStore = new GameStore();
            selected = gameStore.rename(selected, gameStore.findUniqueName().name());
        }
        futureGame.set(selected);
    }

    private void layoutControls(Skin skin, boolean readOnly) {
        this.readOnly = readOnly;
        getContentTable().clearChildren();
        getButtonTable().clearChildren();
        pad(20);
        text("Choose a game").padBottom(25);
        row();
        layoutGameButtons(skin, readOnly);
        getButtonTable().row();

        TextButton cancelButton = new TextButtonPlus("  Cancel  ", skin);
        setObject(cancelButton, null);
        getButtonTable().add(cancelButton).padTop(20).colspan(COLUMNS * 2);
    }

    private void layoutGameButtons(Skin skin, boolean readOnly) {
        int i = 0;
        for (Game.Token game : games) {
            ImageTextButton button = createButton(skin, game);
            getButtonTable().add(button).fillX().spaceLeft(10).spaceRight(10).padLeft(10).padRight(6).padTop(6);
            setObject(button, game);
            if (!readOnly) getButtonTable().add(createDeleteButton(skin, game)).padTop(2);
            if (++i%COLUMNS == 0) getButtonTable().row();
        }
    }

    private ImageTextButton createButton(Skin skin, Game.Token game) {
        String buttonStyle = game.language().buttonStyle();
        ImageTextButton button = new ImageTextButtonPlus(game.name() + " ", skin, buttonStyle);
        button.clearChildren();
        button.add(new Spacer(4));
        button.add(button.getImage());
        button.add(new Spacer(2));
        button.add(button.getLabel());
        return button;
    }

    private Button createDeleteButton(Skin skin, Game.Token game) {
        ImageButtonPlus button = new ImageButtonPlus(skin, "trash-button");
        button.onClick(() -> deleteGame(skin, game));
        return button;
    }

    private void deleteGame(Skin skin, Game.Token game) {
        new GameStore().delete(game);
        games.remove(game);
        layoutControls(skin, readOnly);
    }

    @Override
    public void dispose() {
        getButtonTable().getChildren().forEach(Actor::clearListeners);
    }
}
