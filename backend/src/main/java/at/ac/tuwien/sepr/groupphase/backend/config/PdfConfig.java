package at.ac.tuwien.sepr.groupphase.backend.config;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration class for setting up PDF-related beans in the application context.
 *
 * <p>This class provides a bean for creating instances of {@link PDDocument}, which can be used for PDF generation and manipulation.</p>
 */
@Configuration
public class PdfConfig {

    /**
     * Creates and returns a new instance of {@link PDDocument}.
     *
     * <p>This method is annotated with {@link Bean}, allowing the created {@link PDDocument} instance to be injected into other components of the application.</p>
     *
     * @return a new {@link PDDocument} instance
     */
    @Bean
    public PDDocument pdfDocument() {
        return new PDDocument();
    }
}
