package io.swagger.client.model;

import static org.junit.Assert.*;

import io.swagger.client.ApiClient;
import io.swagger.client.api.PurchaseApi;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;

public class StoreTest {
  Store store;
  CountDownLatch centralPhaseSignal;
  CountDownLatch westPhaseSignal;
  CountDownLatch closeSignal;

  @Before
  public void setUp() throws Exception {
    // create the global counter
    PurchaseCounter purchaseCounter = new PurchaseCounter();
    PurchaseCounter badPurchaseCounter = new PurchaseCounter();
    // create latch to pass it into every store thread
    centralPhaseSignal = new CountDownLatch(1);
    westPhaseSignal = new CountDownLatch(1);
    closeSignal = new CountDownLatch(1);

    store = new Store(1,purchaseCounter,badPurchaseCounter,centralPhaseSignal,westPhaseSignal,closeSignal);
    store.setServerAddress("http://localhost:8080/a1_war_exploded");
  }

  @Test
  public void testCreatePurchaseBody() {
    Purchase body = store.createPurchaseBody();
    System.out.println(body.toString());
  }

  @Test
  public void testRandomCustID() {
    for (int i=0; i<5; i++) {
      System.out.println(store.randomCustID());
    }
  }

  @Test
  public void testRandomItemID() {
    for (int i=0; i<5; i++) {
      System.out.println(store.randomItemID());
    }
  }

  @Test
  public void run() {
    store.run();
    System.out.println("201 response count: " + store.purchaseCounter.getCount());
    System.out.println("400/404 response count: " + store.badPurchaseCounter.getCount());
    System.out.println("central latch should be at 0: " + centralPhaseSignal.getCount());
    System.out.println("west latch should be at 0: " + westPhaseSignal.getCount());
    System.out.println("close latch should be at 0: " + closeSignal.getCount());
  }

  @Test
  public void makePOSTRequest() {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("http://localhost:8080/a1_war_exploded");
    PurchaseApi apiInstance = new PurchaseApi(apiClient);
    Integer custID = store.randomCustID();
    store.makePOSTRequest(apiInstance, store.createPurchaseBody(), custID);
  }
}