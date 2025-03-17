package edu.harvard.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;

public class Configuration {
  public String replicaID;
  public String hostname;
  public String clientPort;
  public String replicaPort;
  public String databaseFile;
  public String introductionHostname;
  public String introductionPort;

  public Configuration(String filename) throws IOException {
    JSONObject obj = new JSONObject(new String(Files.readAllBytes(Path.of(filename))));
    this.replicaID = obj.getString("replicaID");
    this.hostname = obj.getString("hostname");
    this.clientPort = obj.getString("clientPort");
    this.replicaPort = obj.getString("replicaPort");
    this.databaseFile = obj.getString("databaseFile");
    if (obj.has("introductionPoint")) {
      System.out.println("hey");
      this.introductionHostname = obj.getJSONObject("introductionPoint").getString("hostname");
      this.introductionPort = obj.getJSONObject("introductionPoint").getString("port");
    }
  }
}
