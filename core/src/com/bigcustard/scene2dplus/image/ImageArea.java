package com.bigcustard.scene2dplus.image;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import java.util.HashMap;
import java.util.Map;

public class ImageArea extends ScrollPane {
    public static final float WIDTH = 250;
    private TextButton importButton;
    private Map<ImagePlus, ImageControls> imageControlMap = new HashMap<>();
    private ImageAreaModel model;
    private Skin skin;
    private Table table;

    public ImageArea(ImageAreaModel model, Skin skin) {
        super(new Table(), skin);
        this.skin = skin;
        this.model = model;
        createImportButton(skin);
        layoutControls();
        ImageAreaController controller = new ImageAreaController(this, model);
        controller.init();
    }

    private void createImportButton(Skin skin) {
        importButton = new TextButton("Add from clipboard", skin);
    }

    public void onImageAdded(ImagePlus gameImage) {
        createImageControls(gameImage);
        layoutControls();
    }

    public TextButton importButton() {
        return importButton;
    }

    public ImageControls getImageControls(ImagePlus image) {
        if (!imageControlMap.containsKey(image)) {
            createImageControls(image);
        }
        return imageControlMap.get(image);
    }

    private void createImageControls(ImagePlus image) {
        ImageControls imageControls = new ImageControls(image, skin);
        imageControlMap.put(image, imageControls);
    }

    public void showFailure() {
        importButton.setText("Dodgy image!");
        importButton.addAction(
                Actions.sequence(
                        Actions.repeat(10,
                                Actions.sequence(Actions.moveBy(-3, 0, 0.02f, Interpolation.sineOut),
                                        Actions.moveBy(6, 0, 0.04f, Interpolation.sine),
                                        Actions.moveBy(-3, 0, 0.02f, Interpolation.sineIn))),
                        Actions.run(() -> importButton.setText("Add from clipboard"))));
    }

    private void layoutControls() {
        table = (Table) getWidget();
        table.clearChildren();
        addHeader(table);
        addImportButton(table);

        for (ImagePlus gameImage : model.getImages()) {
            addImageControls(table, gameImage);
        }
    }

    private void addHeader(Table table) {
        table.top();
        table.row();
        table.add(new Label("Game images", skin)).padTop(20).padBottom(20);
    }

    private void addImportButton(Table table) {
        table.row();
        table.add(importButton).width(WIDTH);
    }

    private void addImageControls(Table table, ImagePlus gameImage) {
        ImageControls imageControls = getImageControls(gameImage);

        table.row();
//        table.add(imageControls.getDeleteButton());
//        table.row();
        Image image = gameImage.asImage();
        table.add(image).width(WIDTH).height(image.getHeight() * WIDTH / image.getWidth()).padTop(20);
        table.row();
        table.add(imageControls.getNameField()).width(WIDTH);
        table.row();
        table.add(createSizeArea(imageControls));

    }

    private Table createSizeArea(ImageControls imageControls) {
        Table table = new Table();
        table.add(imageControls.getWidthField()).width(WIDTH * 0.4f);
        table.add(new Label(" x ", skin)).width(WIDTH * 0.2f);
        table.add(imageControls.getHeightField()).width(WIDTH * 0.4f);
        return table;
    }
}
