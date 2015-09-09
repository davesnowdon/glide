package com.bigcustard.glide.code.language;

import com.bigcustard.glide.language.JavascriptKeywords;
import org.apache.commons.lang3.tuple.Pair;

public class Javascript extends Language {
    public static final String TEMPLATE =
              "///////////////////////////////////// \n"
            + "//       Welcome to GLIDE!         // \n"
            + "//  Start writing your game below  // \n"
            + "// Look at Samples for inspiration // \n"
            + "///////////////////////////////////// \n\n";

    public Javascript() {
        super(new JavascriptKeywords(), "js", "javascript-button", TEMPLATE);
    }

    @Override
    public Pair<Integer, String> errorChecker(String code) {
        return null;
    }
}