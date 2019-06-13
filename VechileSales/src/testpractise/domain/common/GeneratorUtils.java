package com.dizzion.portal.domain.common;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.RandomStringGenerator;

@UtilityClass
public class GeneratorUtils {
    private static final RandomStringGenerator GENERATOR = new RandomStringGenerator.Builder()
            .withinRange('0', '9')
            .build();
    private static final int DEFAULT_LENGTH = 6;

    public static int generateInt() {
        return Integer.parseInt(GENERATOR.generate(DEFAULT_LENGTH));
    }
}
