package com.app.afridge.dom;

/**
 * Helper for GSON ingredients
 * <p/>
 * Created by drakuwa on 1/30/15.
 */
public class IngredientHelper {

  // {"id":"11","naziv":"egg\/s","catid":"13"}
  private String id;
  private String naziv;
  private String catid;

  public IngredientHelper(String id, String naziv, String catid) {

    this.id = id;
    this.naziv = naziv;
    this.catid = catid;
  }


  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }

  public String getNaziv() {

    return naziv;
  }

  public void setNaziv(String naziv) {

    this.naziv = naziv;
  }

  public String getCatid() {

    return catid;
  }

  public void setCatid(String catid) {

    this.catid = catid;
  }

  @Override
  public String toString() {

    String details = "";
    details += "id: " + getId() + "\n";
    details += "naziv: " + getNaziv() + "\n";
    details += "catid: " + getCatid() + "\n";
    return details;
  }
}
