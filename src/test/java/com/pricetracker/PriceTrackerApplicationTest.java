package com.pricetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PriceTrackerApplicationSimpleTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(PriceTrackerApplication.class);

  @Test
  void testConstructor() {
    PriceTrackerApplication app = new PriceTrackerApplication();
    assertThat(app).isNotNull();
  }

  @Test
  void testMainMethodLoadsContext() {
    PriceTrackerApplication.main(new String[]{});
  }
}