package org.example.counterparty.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты для ReportBuilder")
public class ConcreteReportBuilderTest {
    private ReportBuilder builder;

    @BeforeEach
    void setUp(){
        builder = new ConcreteReportBuilder();
    }

    @Test
    @DisplayName("Должен создать отчёт с заголовком")
    void shouldBuildReportWithTitle() {
        String result = builder
                .createNew()
                .setTitle("ОТЧЕТ ПО КОНТРАГЕНТУ")
                .build();

        assertThat(result).contains("ОТЧЕТ ПО КОНТРАГЕНТУ");
        assertThat(result).contains("-----");
    }

    @Test
    @DisplayName("Должен добавить секции в отчет")
    void shouldAddSections() {
        String result = builder
                .createNew()
                .addSection("Наименование: ООО Агитбригада Ветерок")
                .addSection("ИНН: 1234567890")
                .addSection("ОГРН: 1234567890123")
                .build();

        assertThat(result).contains("Наименование: ООО Агитбригада Ветерок");
        assertThat(result).contains("ИНН: 1234567890");
        assertThat(result).contains("ОГРН: 1234567890123");
    }

    @Test
    @DisplayName("Должен сбрасывать состояние между разными отчетами")
    void shouldResetBetweenReports() {
        builder.createNew().setTitle("Первый отчет").build();

        String result = builder
                .createNew()
                .setTitle("Второй отчет")
                .build();

        assertThat(result).contains("Второй отчет");
        assertThat(result).doesNotContain("Первый отчет");
    }

    @Test
    @DisplayName("Должен создавать полноценный отчет со всеми компонентами")
    void shouldBuildFullReport() {
        String result = builder
                .createNew()
                .setTitle("ОТЧЕТ ПО КОНТРАГЕНТУ")
                .addSection("\n1. ОСНОВНЫЕ СВЕДЕНИЯ:")
                .addSection("   Наименование: ПАО СБЕРБАНК")
                .addSection("   ИНН: 7707083893")
                .addSection("   ОГРН: 1027700132195")
                .addSection("\n2. ЮРИДИЧЕСКИЙ АДРЕС:")
                .addSection("   г Москва, ул Вавилова, д 19")
                .addSection("\n3. СТАТУС ОРГАНИЗАЦИИ:")
                .addSection("   Действующее")
                .setFooter("Дата формирования: 2024-06-08")
                .build();

        assertThat(result).contains("ОТЧЕТ ПО КОНТРАГЕНТУ");
        assertThat(result).contains("ПАО СБЕРБАНК");
        assertThat(result).contains("7707083893");
        assertThat(result).contains("1027700132195");
        assertThat(result).contains("г Москва, ул Вавилова, д 19");
        assertThat(result).contains("Действующее");
        assertThat(result).contains("2024-06-08");
    }

    @Test
    @DisplayName("Должен вернуть пустую строку при создании без вызова методов")
    void shouldReturnEmptyReportWhenNoContent() {
        String result = builder.createNew().build();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Метод reset должен очищать содержимое")
    void resetShouldClearContent() {
        builder.setTitle("Текст").build();

        builder.reset();
        String result = builder.build();

        assertThat(result).isEmpty();
    }
}
