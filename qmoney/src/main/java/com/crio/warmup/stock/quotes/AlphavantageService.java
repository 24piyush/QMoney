
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {
  
  private RestTemplate restTemplate;

  public AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

@Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    
    String url = buildUri(symbol);

    String jsonStringRes = restTemplate.getForObject(url, String.class);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    AlphavantageDailyResponse alphavantageDailyResponse = objectMapper.readValue(jsonStringRes, AlphavantageDailyResponse.class);
    
    Map<LocalDate, AlphavantageCandle> candles = alphavantageDailyResponse.getCandles();
    List<Candle> list = new ArrayList<>();
    for (LocalDate date = from; date.isBefore(to.plusDays(1)); date = date.plusDays(1)) {
      if(candles.containsKey(date)) {
        AlphavantageCandle currCandle = candles.get(date);
        currCandle.set(date);
        list.add(currCandle);
      }
    }
    return list;
  }
  
  private String buildUri(String symbol) {
    //https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=IBM&outputsize=full&apikey=demo
    return  "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&"+ symbol + "&outputsize=full&apikey=" + getToken();
  } 

  private static String getToken() {
    return "34OWM401ON4WB4KH";
  }
}

