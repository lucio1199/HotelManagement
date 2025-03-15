package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class DefaultUiConfigInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUiConfigInitializer.class);
    private final UiConfigRepository uiConfigRepository;

    public DefaultUiConfigInitializer(UiConfigRepository uiConfigRepository) {
        this.uiConfigRepository = uiConfigRepository;
    }

    @Override
    public void run(String... args) {
    }
}
