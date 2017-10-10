/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smallfe.webparser.entity;

import java.util.Map;

/**
 * Web entity interface
 * @author mkucukdemir
 */
public interface WebParseable {
  
  public Map<String,String> getProductDetails();
  
}
