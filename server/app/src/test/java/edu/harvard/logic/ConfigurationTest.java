package edu.harvard.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {
  @Test
  void loadsExampleConfig() throws Exception {
    Configuration config = new Configuration("../config.example.json");
    assertEquals("1", config.replicaID);
    assertEquals("localhost", config.hostname);
  }
}
