package com.crio.warmup.stock.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphavantageCandle implements Candle {

  @JsonProperty("1. open")
  private Double open;

  @JsonProperty("2. high")
  private Double high;

  @JsonProperty("3. low")
  private Double low;

  @JsonProperty("4. close")
  private Double close;

  @JsonIgnore
  private LocalDate date;//private Date date;

  @Override
  public Double getOpen() {
    return open;
  }
  @Override
  public Double getClose() {
    return close;
  }
  @Override
  public Double getHigh() {
    return high;
  }
  @Override
  public Double getLow() {

    return low;
  }
  @Override
  public LocalDate getDate() {
    return date;
  }
  public void set(LocalDate date) {
    this.date = date;
  }
}

