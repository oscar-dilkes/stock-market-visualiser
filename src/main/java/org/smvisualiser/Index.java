package org.smvisualiser;

public class Index {
  private final String name;
  private final String wikipediaUrl;

  public Index (String name, String wikipediaUrl) {
    this.name = name;
    this.wikipediaUrl = wikipediaUrl;
  }

  public String getWikipediaUrl() {
    return this.wikipediaUrl;
  }

  @Override
  public String toString() {
    return name;
  }
}
