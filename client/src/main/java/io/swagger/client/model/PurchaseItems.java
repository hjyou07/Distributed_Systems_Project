/*
 * GianTigle Supermarkets
 * This is a simple supermarket example interface for CS6650 at Northeastern University 
 *
 * OpenAPI spec version: 1.0.0
 * Contact: i.gorton@northeastern.edu
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
/**
 * PurchaseItems
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T21:37:24.598Z[GMT]")
public class PurchaseItems {
  @SerializedName("ItemID")
  private String itemID = null;

  @SerializedName("numberOfItems:")
  private Integer numberOfItems = null;

  public PurchaseItems itemID(String itemID) {
    this.itemID = itemID;
    return this;
  }

   /**
   * Get itemID
   * @return itemID
  **/
  @Schema(description = "")
  public String getItemID() {
    return itemID;
  }

  public void setItemID(String itemID) {
    this.itemID = itemID;
  }

  public PurchaseItems numberOfItems(Integer numberOfItems) {
    this.numberOfItems = numberOfItems;
    return this;
  }

   /**
   * Get numberOfItems
   * @return numberOfItems
  **/
  @Schema(description = "")
  public Integer getNumberOfItems() {
    return numberOfItems;
  }

  public void setNumberOfItems(Integer numberOfItems) {
    this.numberOfItems = numberOfItems;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PurchaseItems purchaseItems = (PurchaseItems) o;
    return Objects.equals(this.itemID, purchaseItems.itemID) &&
        Objects.equals(this.numberOfItems, purchaseItems.numberOfItems);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemID, numberOfItems);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PurchaseItems {\n");
    
    sb.append("    itemID: ").append(toIndentedString(itemID)).append("\n");
    sb.append("    numberOfItems: ").append(toIndentedString(numberOfItems)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
