
package com.crio.warmup.stock.quotes;

import org.springframework.web.client.RestTemplate;

public enum StockQuoteServiceFactory {

  // Note: (Recommended reading)
  // Pros and cons of implementing Singleton via enum.
  // https://softwareengineering.stackexchange.com/q/179386/253205

  INSTANCE;

  public StockQuotesService getService(String provider,  RestTemplate restTemplate) {
    switch (provider.toLowerCase()) {
      case "tiingo" :
        return new TiingoService(restTemplate);
      case "alphavantage" :
        return new AlphavantageService(restTemplate);
      default:
        return new AlphavantageService(restTemplate);
    }
  }
}
