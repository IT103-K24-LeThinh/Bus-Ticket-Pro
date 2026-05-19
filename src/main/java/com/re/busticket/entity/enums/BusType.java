package com.re.busticket.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusType {
    SEATS_29("29 chỗ"),
    SEATS_45("45 chỗ");

    private final String displayName;
}
