package com.bigcustard.glide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bigcustard.glide.code.Game;
import com.bigcustard.glide.code.GameStore;
import com.bigcustard.glide.code.command.ExitCommand;
import com.bigcustard.glide.code.command.RunCommand;
import com.bigcustard.glide.code.language.Language;
import com.bigcustard.glide.help.Help;
import com.bigcustard.glide.help.HelpTopic;
import com.bigcustard.scene2dplus.button.ButtonBar;
import com.bigcustard.scene2dplus.button.TextButtonPlus;
import com.bigcustard.scene2dplus.command.CommandHistory;
import com.bigcustard.scene2dplus.command.RedoCommand;
import com.bigcustard.scene2dplus.command.UndoCommand;
import com.bigcustard.scene2dplus.dialog.ErrorDialog;
import com.bigcustard.scene2dplus.image.ImageEditor;
import com.bigcustard.scene2dplus.image.ImageModel;
import com.bigcustard.scene2dplus.image.ImageUtils;
import com.bigcustard.scene2dplus.resource.Resource;
import com.bigcustard.scene2dplus.resource.ResourceArea;
import com.bigcustard.scene2dplus.resource.ResourceSet;
import com.bigcustard.scene2dplus.sound.SoundEditor;
import com.bigcustard.scene2dplus.sound.SoundModel;
import com.bigcustard.scene2dplus.sound.SoundUtils;
import com.bigcustard.scene2dplus.tab.TabControl;
import com.bigcustard.scene2dplus.textarea.ScrollableTextArea;
import com.bigcustard.scene2dplus.textarea.TextAreaModel;
import com.bigcustard.scene2dplus.textarea.command.CopyCommand;
import com.bigcustard.scene2dplus.textarea.command.PasteCommand;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CodingScreen extends ScreenAdapter {
    private Skin skin;
    private Stage stage;
    private TextAreaModel model;
    private TextAreaModel exampleModel;
    private ScrollableTextArea exampleArea;
    private ScrollableTextArea textArea;
    private TabControl resourceTabControl;
    private Game game;
    private GameStore gameStore;
    private Help help;
    private Consumer<Screen> setScreen;
    private ScreenFactory screenFactory;
    private ScheduledFuture<?> gameSavingProcess;
    private ButtonBar buttonBar;
    private ScheduledExecutorService executorService;
    private Cell<ScrollableTextArea> exampleCell;
    private Table layoutTable;

    public CodingScreen(Game game, GameStore gameStore, Help help, Viewport viewport, Consumer<Screen> setScreen, ScreenFactory screenFactory, Skin skin) {
        this.game = game;
        this.gameStore = gameStore;
        this.help = help;
        this.setScreen = setScreen;
        this.screenFactory = screenFactory;
        this.stage = new Stage(viewport);
        this.skin = skin;

        Table layoutTable = layoutScreen();

        stage.addActor(layoutTable);
        stage.setKeyboardFocus(textArea.textArea());
        Gdx.input.setInputProcessor(stage);
    }

    private Table layoutScreen() {
        createTextArea(game);
        createExampleArea(game.language());
        Label errorLabel = createErrorLabel(game);
        createResourceArea();
        buttonBar = createButtonBar();

        layoutTable = new Table();
        layoutTable.background(skin.getDrawable("solarizedNew"));
        layoutTable.row();
        layoutTable.add(createTextAreaTable(errorLabel)).expand().fill();
        layoutTable.add(resourceTabControl).width(280).expandY().fillY();
        layoutTable.row();
        layoutTable.add(buttonBar).colspan(2).expandX().fillX();
        layoutTable.setFillParent(true);
        layoutTable.pack();

        return layoutTable;
    }

    private ButtonBar createButtonBar() {
        ButtonBar buttonBar = new ButtonBar(skin);
        buttonBar.addSpacer(1);
        buttonBar.addTextButton("Past <", () -> new UndoCommand(game.commandHistory()));
        buttonBar.addImage("tardis2");
        buttonBar.addTextButton("> Future", () -> new RedoCommand(game.commandHistory()));
        buttonBar.addSpacer(16);
        buttonBar.addTextButton("Copy", () -> new CopyCommand(model));
        buttonBar.addImage("copy");
        buttonBar.addTextButton("Paste", () -> new PasteCommand(model));
        buttonBar.addSpacer(16);
        buttonBar.addImageButton(" Run", "run-button", () -> new RunCommand(game, gameStore, this::showRunScreen));
        buttonBar.addSpacer(16);
        buttonBar.addImageButton(" Exit", "exit-button", () -> new ExitCommand(game, gameStore, this::saveGameChoice, this::getGameName, this::errorReporter, this::exitToMainMenu));

        executorService = Executors.newSingleThreadScheduledExecutor();
        gameSavingProcess = executorService.scheduleAtFixedRate(() -> {
            buttonBar.refreshEnabledStatuses();
            gameStore.save(game);
        }, 0, 2, TimeUnit.SECONDS);

        return buttonBar;
    }

    private void exitToMainMenu() {
        gameSavingProcess.cancel(true);
        setScreen.accept(screenFactory.createWelcomeScreen());
        dispose();
    }

    private void showRunScreen(Game game) {
        RuntimeFacade.INSTANCE.run(game, () -> {
            setScreen.accept(this);
            Gdx.input.setInputProcessor(stage);
        });
    }

    private void createResourceArea() {
        ResourceArea<ImageModel> imageArea = createImageArea();
        ResourceArea<SoundModel> soundArea = createSoundArea();
        Actor helpArea = createHelpArea();
        resourceTabControl = new TabControl();
        resourceTabControl.addTab(imageArea, new TextButtonPlus("Images  ", skin, "tab"));
        resourceTabControl.addTab(soundArea, new TextButtonPlus("Sounds  ", skin, "tab"));
        resourceTabControl.addTab(helpArea, new TextButtonPlus("Help  ", skin, "tab"));
        resourceTabControl.background(skin.getDrawable("solarizedBackground"));
        resourceTabControl.init();
    }

    private Actor createHelpArea() {
        List<HelpTopic> helpTopics = help.topics();
        List<Actor> helpLinks = helpTopics.stream().map(this::createHelpLink).collect(Collectors.toList());

        Table table = new Table();
        table.background(skin.getDrawable("solarizedNew"));
        table.clearChildren();
        table.top();
        table.padTop(20);
        for (Actor link : helpLinks) {
            table.add(link);
            table.row();
        }
        table.pack();

        return new ScrollPane(table, skin);
    }

    private Actor createHelpLink(HelpTopic helpTopic) {
        TextButtonPlus button = new TextButtonPlus(helpTopic.getText(), skin, "link");
        button.onClick(() -> {
            exampleCell.height(200);
            exampleModel.setText(helpTopic.getExampleCode());
            exampleArea.invalidateHierarchy();
        });
        return button;
    }

    private ResourceArea<ImageModel> createImageArea() {
        List<ImageModel> imageModels = game.imageGroup().images();
        List<Resource<ImageModel>> editors = imageModels
                .stream()
                .map(this::createImageEditor)
                .collect(Collectors.toList());
        ResourceSet<ImageModel> resourceSet = new ResourceSet<>(editors, game.commandHistory());
        resourceSet.resources().watchChange((image) -> {
            List<ImageModel> models = resourceSet.stream().map(Resource::model).collect(Collectors.toList());
            game.imageGroup().images(models);
        });
        return new ResourceArea<>(skin, resourceSet, game.commandHistory(), (stream, url) ->
                createImageEditor(ImageUtils.importImage(stream, url, game.imageGroup().folder())));
    }

    private ImageEditor createImageEditor(ImageModel model) {
        return new ImageEditor(model, skin, game.commandHistory());
    }

    private ResourceArea<SoundModel> createSoundArea() {
        List<SoundModel> soundModels = game.soundGroup().sounds();
        List<Resource<SoundModel>> editors = soundModels
                .stream()
                .map(this::createSoundEditor)
                .collect(Collectors.toList());
        ResourceSet<SoundModel> resourceSet = new ResourceSet<>(editors, game.commandHistory());
        resourceSet.resources().watchAdd((sound) -> {
            List<SoundModel> models = resourceSet.stream().map(Resource::model).collect(Collectors.toList());
            game.soundGroup().sounds(models);
        });
        return new ResourceArea<>(skin, resourceSet, game.commandHistory(), (stream, url) ->
                createSoundEditor(SoundUtils.importSound(stream, url, game.soundGroup().folder())));
    }

    private SoundEditor createSoundEditor(SoundModel model) {
        return new SoundEditor(model, skin, game.commandHistory());
    }

    private Table createTextAreaTable(Label errorLabel) {
        Table textAreaTable = new Table();
        exampleCell = textAreaTable.add(exampleArea);
        exampleCell.fill().height(0);
        textAreaTable.row();
        textAreaTable.add(textArea).fill().expand();
        textAreaTable.row();
        textAreaTable.add(errorLabel).fillX();
        return textAreaTable;
    }

    private void createTextArea(Game game) {
        model = new TextAreaModel(game.code(), game.language().codeColorCoder(game::runtimeError));
        model.preInsertVetoer(game.language()::vetoPreInsert);
        model.addChangeListener((m) -> game.code(model.text()));
        textArea = new ScrollableTextArea(model, skin, game.commandHistory(), "code");
    }

    private void createExampleArea(Language language) {
        exampleModel = new TextAreaModel("hjhhkhjfr", language.codeColorCoder(() -> null));
        exampleArea = new ScrollableTextArea(exampleModel, skin, new CommandHistory(), "example");
    }

    private Label createErrorLabel(Game game) {
        Label errorLabel = new Label("", skin, "error");
        errorLabel.setVisible(false);
        errorLabel.setWrap(true);
        game.registerChangeListener((g) -> {
            Gdx.app.postRunnable(() -> {
                Pair<Integer, String> error = game.runtimeError();
                errorLabel.setVisible(error != null);
                errorLabel.setText(error == null ? null : error.getRight());
            });
        });
        return errorLabel;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(delta, 1 / 60f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    private void errorReporter(Exception e, Runnable onClosed) {
        ErrorDialog errorDialog = new ErrorDialog(e, onClosed, skin);
        errorDialog.show(stage);
    }

    private ListenableFuture<String> getGameName() {
        NameGameDialog nameGameDialog = new NameGameDialog(game, skin);
        nameGameDialog.show(stage);
        stage.setKeyboardFocus(nameGameDialog.getNameTextField());
        return nameGameDialog.getFutureGameName();
    }

    private ListenableFuture<Boolean> saveGameChoice() {
        gameSavingProcess.cancel(true);
        SaveChoiceDialog saveGameDialog = new SaveChoiceDialog(skin);
        saveGameDialog.show(stage);
        return saveGameDialog.getFutureSaveChoice();
    }

    @Override
    public void dispose() {
        super.dispose();
        stage.dispose();
        model.dispose();
        resourceTabControl.dispose();
        game.dispose();
        buttonBar.dispose();
        executorService.shutdown();
    }
}
