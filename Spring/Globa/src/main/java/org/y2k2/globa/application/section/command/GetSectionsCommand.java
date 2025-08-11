package org.y2k2.globa.application.section.command;

public record GetSectionsCommand(
        Long recordId
) {
    public static GetSectionsCommand of(Long recordId) {
        return new GetSectionsCommand(recordId);
    }
}
