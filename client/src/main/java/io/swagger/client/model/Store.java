package io.swagger.client.model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Store implements Runnable {
  private static final int STORE_HOURS = 9;
  private static final int START_CENTRAL_PHASE = 3;
  private static final int START_WEST_PHASE = 5;

  private Integer storeID;
  private PurchaseCounter purchaseCounter;
  private PurchaseCounter badPurchaseCounter;
  private CountDownLatch centralPhaseSignal;
  private CountDownLatch westPhaseSignal;
  private CountDownLatch closeSignal;

  private int numCust = 1000; // number of customer per store. Defines the range of custIDS
  private int maxItemID = 100000; //
  private int numPurchases = 60; // number of purchases per hour
  private int numPurchaseItems = 5; // number of items for each purchase between 1 ~ 20
  private String date = "20210101"; // string representation of date
  private String serverAddress;


  public Store(Integer storeID, PurchaseCounter purchaseCounter, PurchaseCounter badPurchaseCounter,
      CountDownLatch centralPhaseSignal, CountDownLatch westPhaseSignal, CountDownLatch closeSignal) {
    this.storeID = storeID;
    this.purchaseCounter = purchaseCounter;
    this.badPurchaseCounter = badPurchaseCounter;
    this.centralPhaseSignal = centralPhaseSignal;
    this.westPhaseSignal = westPhaseSignal;
    this.closeSignal = closeSignal;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    System.out.println("serverAddress var: " + serverAddress);
    apiClient.setBasePath(serverAddress);
    // pass in an ApiClient instance that connects to my server
    PurchaseApi apiInstance = new PurchaseApi(apiClient);
    // execute the post method
    // for loop 0-9 (store hr) and 0-60 (purchases per hour) -> simulates time
    for (int i=0; i < STORE_HOURS; i++) {
     for (int j=0; j < numPurchases; j++) {
       Purchase body = createPurchaseBody();
       Integer custID = randomCustID();
       makePOSTRequest(apiInstance, body, custID);
       // at i=3 and at i=5 then count down the latch
       if (i == START_CENTRAL_PHASE) centralPhaseSignal.countDown();
       if (i == START_WEST_PHASE) westPhaseSignal.countDown();
     }
    }
    // when i'm done running count down final latch
    closeSignal.countDown();
  }

  private Purchase createPurchaseBody() {
    Purchase body = new Purchase();
    // generate a specified number of purchase items (default=5)
    for (int i=0; i < numPurchaseItems; i++) {
      PurchaseItems item = new PurchaseItems();
      item.setItemID(randomItemID());
      // for A1, for each purchase item, set amount to 1
      item.setNumberOfItems(1);
      body.addItem(item);
    }
    return body;
  }

  private void makePOSTRequest(PurchaseApi apiInstance, Purchase body, Integer custID) {
    try {
      apiInstance.newPurchase(body, storeID, custID, date);
      ApiResponse response = apiInstance.newPurchaseWithHttpInfo(body, storeID, custID, date);
      if ((response.getStatusCode() == 201)) {
        purchaseCounter.inc();
      } else {
        badPurchaseCounter.inc();
      }
    } catch (ApiException e) {
      System.err.println("Exception when calling PurchaseApi#newPurchase");
      e.printStackTrace();
    }
  }

  private Integer randomCustID() {
    Random rand = new Random();
    int custID = rand.nextInt(numCust) + (storeID * 1000);
    return custID;
  }

  private String randomItemID() {
    Random rand = new Random();
    return String.valueOf(rand.nextInt(maxItemID));
  }

  public void setNumCust(int numCust) {
    this.numCust = numCust;
  }

  public void setMaxItemID(int maxItemID) {
    this.maxItemID = maxItemID;
  }

  public void setNumPurchases(int numPurchases) {
    this.numPurchases = numPurchases;
  }

  public void setNumPurchaseItems(int numPurchaseItems) {
    this.numPurchaseItems = numPurchaseItems;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

}
