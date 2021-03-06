package com.bigcustard.glide.code;

import com.badlogic.gdx.files.FileHandle;
import com.bigcustard.glide.code.language.Language;
import com.bigcustard.glide.language.Syntax;
import com.bigcustard.scene2dplus.image.ImageGroup;
import com.bigcustard.scene2dplus.image.ImageModel;
import com.bigcustard.scene2dplus.sound.SoundGroup;
import com.bigcustard.scene2dplus.sound.SoundModel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import javax.script.ScriptException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class GameTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private ImageGroup mockImageModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private SoundGroup mockSoundModel;
    @Mock private ImageModel mockImage;
    @Mock private SoundModel mockSound;
    @Mock private Consumer<Game> mockChangeListener;
    @Mock private Language mockLanguage;
    @Mock private FileHandle mockFolder;
    @Mock private Syntax mockSyntax;
    @Captor private ArgumentCaptor<Consumer<ImageGroup>> changeImageListenerCaptor;
    @Captor private ArgumentCaptor<Consumer<SoundGroup>> changeSoundListenerCaptor;

    @Before
    public void before() {
        initMocks(this);
        when(mockLanguage.scriptEngine()).thenReturn("groovy");
        when(mockLanguage.syntax()).thenReturn(mockSyntax);
        doNothing().when(mockImageModel).watch(changeImageListenerCaptor.capture());
        doNothing().when(mockSoundModel).watch(changeSoundListenerCaptor.capture());
    }

    @Test
    public void isValidIfCodeAndImagesValid() {
        Game game = newGame(mockLanguage);
        when(mockLanguage.isValid("code")).thenReturn(true);
        when(mockImageModel.isValid()).thenReturn(true);
        game.code("code");
        assertThat(game.isValid()).isTrue();
    }

    @Test
    public void isInvalidIfCodeInvalid() {
        Game game = newGame(mockLanguage);
        game.code("code");
        when(mockLanguage.isValid("code")).thenReturn(false);
        when(mockImageModel.isValid()).thenReturn(true);
        assertThat(game.isValid()).isFalse();
    }

    @Test
    public void isInvalidIfImagesInvalid() {
        Game game = newGame(mockLanguage);
        game.code("code");
        when(mockLanguage.isValid("code")).thenReturn(true);
        when(mockImageModel.isValid()).thenReturn(false);
        assertThat(game.isValid()).isFalse();
    }

    @Test
    public void changeImageStoresImageModel() {
        newGame(mockLanguage);
        changeImageListenerCaptor.getValue().accept(mockImageModel);
        verify(mockImageModel, times(1)).save();
    }

    @Test
    public void providesAccessToTheImageModel() {
        assertThat(newGame(mockLanguage).imageGroup()).isSameAs(mockImageModel);
    }

    @Test
    public void notifiesOfCodeChange() {
        Game game = newGame(mockLanguage);
        game.registerChangeListener(mockChangeListener);
        game.code("change");
        verify(mockChangeListener).accept(game);
    }

    @Test
    public void notifiesOfImageChange() {
        Game game = newGame(mockLanguage);
        game.registerChangeListener(mockChangeListener);
        changeImageListenerCaptor.getValue().accept(mockImageModel);
        verify(mockChangeListener).accept(game);
    }

    @Test
    public void notifiesOfSoundChange() {
        Game game = newGame(mockLanguage);
        game.registerChangeListener(mockChangeListener);
        changeSoundListenerCaptor.getValue().accept(mockSoundModel);
        verify(mockChangeListener).accept(game);
    }

    @Test
    public void changeSoundStoresSoundModel() {
        newGame(mockLanguage);
        changeSoundListenerCaptor.getValue().accept(mockSoundModel);
        verify(mockSoundModel, times(1)).save();
    }

    @Test
    public void providesAccessToTheSoundModel() {
        assertThat(newGame(mockLanguage).soundGroup()).isSameAs(mockSoundModel);
    }

    @Test
    public void notifiesOfRuntimeError() {
        Game game = newGame(mockLanguage);
        game.registerChangeListener(mockChangeListener);
        game.runtimeError(new RuntimeException("Bad stuff"));
        verify(mockChangeListener).accept(game);
    }

    @Test
    public void runtimeErrorMessageWhenNone() {
        Game game = newGame(mockLanguage);
        assertThat(game.runtimeError()).isNull();
    }

    @Test
    @Ignore
    public void extractRuntimeErrorMessage() {
        Game game = newGame(mockLanguage);
        game.runtimeError(new RuntimeException(new RuntimeException(new RuntimeException(new ScriptException("Bad stuff")))));
        assertThat(game.runtimeError().getRight()).isEqualTo("Bad stuff");
    }

    @Test
    @Ignore
    public void unexpectedRuntimeErrorMessage() {
        Game game = newGame(mockLanguage);
        game.runtimeError(new RuntimeException("Bad stuff"));
        assertThat(game.runtimeError()).isEqualTo("Bad stuff");
    }

    @Test
    public void itShould_NotBeModifiedInitially() {
        Game game = newGame(mockLanguage);
        assertThat(game.isModified()).isFalse();
    }

    @Test
    public void itShould_BeModifiedWhenTheCodeChangesToSomethingDifferent() {
        Game game = newGame(mockLanguage);
        game.code("new code");
        assertThat(game.isModified()).isTrue();
    }

    @Test
    public void itShould_BeModifiedWhenTheCodeChangesToSomethingDifferentThenTheSameCode() {
        Game game = newGame(mockLanguage);
        game.code("new code");
        game.code("new code");
        assertThat(game.isModified()).isTrue();
    }

    @Test
    public void itShould_NotBeModifiedWhenTheCodeChangesToTheSameCode() {
        Game game = newGame(mockLanguage);
        game.code(game.code());
        assertThat(game.isModified()).isFalse();
    }

    @Test
    public void itShould_BeModifiedWhenImagesChange() {
        Game game = newGame(mockLanguage);
        changeImageListenerCaptor.getValue().accept(mockImageModel);
        assertThat(game.isModified()).isTrue();
    }

    private Game newGame(Language language) {
        return new Game(new Game.Token("name", language, mockFolder), "code", mockImageModel, mockSoundModel);
    }
}
