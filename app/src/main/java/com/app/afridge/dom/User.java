package com.app.afridge.dom;

/**
 * User POJO
 * <p/>
 * Created by drakuwa on 3/5/15.
 */
@SuppressWarnings("UnusedDeclaration")
public class User {

  private String id;
  private String fullName;
  private String imageUrl;
  private String email;

  public String getFullName() {

    return fullName;
  }

  public void setFullName(String fullName) {

    this.fullName = fullName;
  }

  public String getImageUrl() {

    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {

    this.imageUrl = imageUrl;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }

  public String getEmail() {

    return email;
  }

  public void setEmail(String email) {

    this.email = email;
  }

  @Override
  public String toString() {

    String details = "";
    details += "id: " + getId() + "\n";
    details += "fullName: " + getFullName() + "\n";
    details += "imageUrl: " + getImageUrl() + "\n";
    details += "email: " + getEmail() + "\n";
    return details;
  }
}
