package com.renata.domain.entities;

import com.renata.domain.enums.ReportType;
import java.time.LocalDateTime;
import java.util.UUID;

public class Report {
    private UUID id;
    private ReportType type;
    private LocalDateTime generatedAt;
    private UUID userId;

}
