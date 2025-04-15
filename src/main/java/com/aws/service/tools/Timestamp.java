package com.aws.service.tools;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Timestamp {

    public String toString() {
        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return now.atOffset(ZoneOffset.UTC).format(formatter);
    }
}
