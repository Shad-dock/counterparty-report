package org.example.counterparty.builder;

public interface ReportBuilder {
    ReportBuilder setTitle(String title);
    ReportBuilder addSection(String section);
    ReportBuilder setFooter(String footer);
    String build();
    void reset();
}