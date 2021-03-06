package com.bigcustard.glide.code.language;

import com.bigcustard.glide.code.CodeColorCoder;
import com.bigcustard.glide.code.Game;
import com.bigcustard.glide.language.Keywords;
import com.bigcustard.glide.language.Syntax;
import com.bigcustard.scene2dplus.textarea.ColorCoder;
import com.bigcustard.scene2dplus.textarea.TextAreaModel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public abstract class Language {
    public static Language Ruby = new Ruby();
    public static Language Groovy = new Groovy();
    public static Language Javascript = new Javascript();
    public static Language Python = new Python();

    private final Syntax syntax;
    private String scriptEngine;
    private String buttonStyle;
    private String template;

    Language(Keywords keywords, String scriptEngine, String buttonStyle, String template) {
        this.scriptEngine = scriptEngine;
        this.buttonStyle = buttonStyle;
        this.template = template + "\n\n";
        this.syntax = new Syntax(keywords, this::errorChecker);
    }

    public abstract Pair<Integer, String> errorChecker(String code);

    public Pair<Integer, String> locateError(Throwable throwable) {
        return null;
    }

    public boolean isValid(String code) {
        return syntax.isValid(code);
    }

    public ColorCoder codeColorCoder(Supplier<Pair<Integer, String>> errorSupplier) {
        return new CodeColorCoder(errorSupplier, syntax);
    }

    public String scriptEngine() {
        return scriptEngine;
    }

    public String buttonStyle() {
        return buttonStyle;
    }

    public Syntax syntax() {
        return syntax;
    }

    public String template() {
        return template;
    }

    public String vetoPreInsert(String characters, TextAreaModel textAreaModel) {
        return characters;
    }

    @Override
    public String toString() {
        return scriptEngine;
    }

    public static Language from(String scriptEngine) {
        if (scriptEngine.equals(Javascript.scriptEngine())) {
            return Javascript;
        } else if (scriptEngine.equals(Groovy.scriptEngine())) {
            return Groovy;
        } else if (scriptEngine.equals(Ruby.scriptEngine())) {
            return Ruby;
        } else if (scriptEngine.equals(Python.scriptEngine())) {
            return Python;
        }
        throw new IllegalArgumentException("Unknown language " + scriptEngine);
    }
}
