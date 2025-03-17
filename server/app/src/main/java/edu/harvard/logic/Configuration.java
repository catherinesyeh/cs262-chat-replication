package edu.harvard.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;

public class Configuration {
  public String replicaID;
  public String hostname;
  public int clientPort;
  public int replicaPort;
  public String databaseFile;
  public String jwtSecret;
  public String introductionHostname;
  public int introductionPort;

  public Configuration(String filename) throws IOException {
    JSONObject obj = new JSONObject(new String(Files.readAllBytes(Path.of(filename))));
    this.replicaID = obj.getString("replicaID");
    this.hostname = obj.getString("hostname");
    this.clientPort = Integer.parseInt(obj.getString("clientPort"));
    this.replicaPort = Integer.parseInt(obj.getString("replicaPort"));
    this.databaseFile = obj.getString("databaseFile");
    this.jwtSecret = obj.getString("jwtSecret");
    if (obj.has("introductionPoint")) {
      this.introductionHostname = obj.getJSONObject("introductionPoint").getString("hostname");
      this.introductionPort = Integer.parseInt(obj.getJSONObject("introductionPoint").getString("port"));
    }
  }
}
