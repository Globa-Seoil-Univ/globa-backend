package org.y2k2.globa.util;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ValidValues {

    public static Set<String> validSnsKinds = Set.of("1001","1002","1003","1004");
    public static Set<String> validNotificationTypes = Set.of("a","n","s","r");
}
