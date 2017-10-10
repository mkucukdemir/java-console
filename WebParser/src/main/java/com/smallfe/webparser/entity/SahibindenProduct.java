/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smallfe.webparser.entity;

import java.util.Map;

/**
 * Web entity related with Sahibinden<dot>com
 * @author mkucukdemir
 */
public class SahibindenProduct implements WebParseable {
  
  private static final String SELECTOR_PATTERN = "";
  private static final String PATTERN_FACTOR = "";
  private static final String URL_PREFIX ="";
  private static final String[] ATTRIBUTE_LIST = new String[]{};
  private String urlSuffix;

  public SahibindenProduct() {
  }

  public SahibindenProduct(String urlSuffix) {
    this.urlSuffix = urlSuffix;
  }

  @Override
  public Map<String, String> getProductDetails() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
