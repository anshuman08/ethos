/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales.restaurant.dao;

/**
 *
 * @author premsarojanand
 */
public interface WebServiceEndPointAddress {
    
    String url = "http://apis.ethosmiracle.com/api";
    
    String allDataSend = "/dataSync/create?autoReconnect=true&useSSL=false";
    
    String getPosOrder = "/orders/getposorder?autoReconnect=true&useSSL=false";
    
    String updateOrder = "/orders/update?autoReconnect=true&useSSL=false";
    

    
}
