package org.example.counterparty.builder;

import org.springframework.stereotype.Component;

@Component
public class ConcreteReportBuilder implements ReportBuilder {

    private StringBuilder content;

    public ConcreteReportBuilder() {
        reset();
    }

    @Override
    public void reset() {
        content = new StringBuilder();
    }

    @Override
    public ReportBuilder setTitle(String title) {
        content.append("-".repeat(50)).append("\n");
        content.append(title).append("\n");
        content.append("-".repeat(50)).append("\n");
        return this;
    }

    @Override
    public ReportBuilder addSection(String section) {
        content.append(section).append("\n");
        return this;
    }

    @Override
    public ReportBuilder setFooter(String footer) {
        content.append("-".repeat(50)).append("\n");
        content.append(footer).append("\n");
        return this;
    }

    @Override
    public String build() {
        return content.toString();
    }
}