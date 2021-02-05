package io.swagger.client.controller;

import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.PurchaseApi;

import java.io.File;
import java.util.*;

public class PurchaseApiExample {

  public static void main(String[] args) {

    // Create an ApiClient instance that connects to my server
    ApiClient apiClient = new ApiClient();
    // might have to change the ip address if you have restarted the instance
    String ec2Path = "http://35.173.161.70:8080/a1_war";
    String localPath = "http://localhost:8080/a1_war_exploded";
    apiClient.setBasePath(localPath);
    // pass in an ApiClient instance that connects to my server
    PurchaseApi apiInstance = new PurchaseApi(apiClient);

    Purchase body = new Purchase(); // Purchase | items purchased
    // create an item to put in Purchase
    PurchaseItems item = new PurchaseItems();
    item.setItemID("item001");
    item.setNumberOfItems(1);
    // add some PurchaseItems by calling .addItem()
    body.addItem(item);

    Integer storeID = 56; // Integer | ID of the store the purchase takes place at
    Integer custID = 56; // Integer | customer ID making purchase
    String date = "20210101"; // String | date of purchase

    try {
      apiInstance.newPurchase(body, storeID, custID, date);
      // what's going on in this newPurchase()?
      ApiResponse response = apiInstance.newPurchaseWithHttpInfo(body, storeID, custID, date);
      System.out.println(response.getStatusCode());
      System.out.println("new Purchase instance built successful");
    } catch (ApiException e) {
      System.err.println("Exception when calling PurchaseApi#newPurchase");
      e.printStackTrace();
    }
  }
}