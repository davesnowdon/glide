package com.bigcustard.planet.code.command;

import com.bigcustard.planet.code.CodeRunner;
import com.bigcustard.planet.code.Game;
import com.bigcustard.scene2dplus.textarea.TextAreaModel;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class RunCommandTest {
    private RunCommand command;
    private TextAreaModel model;
    private SettableFuture<String> futureName;
    @Mock private Game game;

    @Before
    public void before() {
        initMocks(this);
        futureName = SettableFuture.create();
        Supplier<ListenableFuture<String>> nameSupplier = () -> futureName;
        model = new TextAreaModel("code", null);
        command = new RunCommand(model, game, nameSupplier);
    }

    @Test
    public void cannotExecuteWhenGameInvalid() {
        when(game.isValid()).thenReturn(false);
        assertThat(command.canExecute()).isFalse();
    }

    @Test
    public void canExecuteWhenGameValid() {
        when(game.isValid()).thenReturn(true);
        assertThat(command.canExecute()).isTrue();
    }

    @Test
    public void forcesNamingUnnamedGames() {
        when(game.isNamed()).thenReturn(false);
        command.execute();
        futureName.set("name");
        verify(game).setName("name");
        verify(game).run();

    }

    @Test
    public void dontHaveToNameNamedGames() {
        when(game.isNamed()).thenReturn(true);
        command.execute();
        verify(game, never()).setName(anyString());
        verify(game).run();

    }
}