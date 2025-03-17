package edu.harvard.Data;

public class Data {
  // Internal data types (stored in database)
  public static class Account {
    public String id;
    public String username;
    public String password_hash;
    public String client_bcrypt_prefix;
    public long timestamp;
  }

  public static class Message {
    public String id;
    public String sender_id;
    public String recipient_id;
    public String message;
    public boolean read;
    public long timestamp;
  }
}
