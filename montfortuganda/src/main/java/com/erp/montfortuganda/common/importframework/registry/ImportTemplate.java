package com.erp.montfortuganda.common.importframework.registry;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ImportTemplate {
    private final String templateVersion;
    private final List<String> expectedHeaders;
    private final Map<String, List<String>> aliases; // e.g. "Last Name" -> ["Surname", "LName"]
    private final List<String> mandatoryColumns;
    private final List<String> optionalColumns;
    private final Map<String, String> validationHints;
    private final String downloadUrl;
    private final Map<String, String> sampleData;
}
