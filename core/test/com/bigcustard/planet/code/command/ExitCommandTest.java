package com.bigcustard.planet.code.command;

import com.bigcustard.planet.code.Game;
import com.bigcustard.util.FutureSupplier;
import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;

public class ExitCommandTest {
    private ExitCommand command;
    @Mock private Game game;
    @Mock private Runnable exitProcess;
    @Mock private BiConsumer<Exception, Runnable> mockErrorReporter;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void executeWithUnnamedGameThenSave() {
        FutureSupplier<String> nameSupplier = () -> Futures.immediateFuture("name") ;
        FutureSupplier<Boolean> saveChoiceSupplier = () -> Futures.immediateFuture(true);
        when(game.isNamed()).thenReturn(false);
        command = new ExitCommand(game, saveChoiceSupplier, nameSupplier, mockErrorReporter,exitProcess);
        command.execute();
        verify(game).setName("name");
    }

    @Test
    public void executeWithUnnamedGameThenDelete() {
        FutureSupplier<String> mockSupplier = mock(FutureSupplier.class);
        FutureSupplier<Boolean> saveChoiceSupplier = () -> Futures.immediateFuture(false);
        when(game.isNamed()).thenReturn(false);
        command = new ExitCommand(game, saveChoiceSupplier, mockSupplier, mockErrorReporter, exitProcess);
        command.execute();
        verifyZeroInteractions(mockSupplier);
        verify(game).delete();
    }

    @Test
    public void executeWithNamedGameSavesAutomatically() {
        FutureSupplier<String> mockSupplier = mock(FutureSupplier.class);
        when(game.isNamed()).thenReturn(true);
        command = new ExitCommand(game, null, mockSupplier, mockErrorReporter, exitProcess);
        command.execute();
        verifyZeroInteractions(mockSupplier);
    }
}