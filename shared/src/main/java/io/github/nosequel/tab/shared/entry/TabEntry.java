package io.github.nosequel.tab.shared.entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class TabEntry {

    private final int x;
    private final int y;
    private final String text;
    private final int ping;

    private String[] skinData = new String[]{"", ""};
}