
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    
    String url = buildUri(symbol, from, to);

    String jsonStringResponse = restTemplate.getForObject(url,String.class);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    TiingoCandle[] data = objectMapper.readValue(jsonStringResponse, TiingoCandle[].class);

    if(data != null) return Arrays.asList(data);
    else return Arrays.asList(new TiingoCandle[0]);
  }
  
  private String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    //  String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
    //       + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    return  "https://api.tiingo.com/tiingo/daily/"+ symbol +"/prices?startDate="+ 
    startDate +"&endDate="+ endDate +"&token=" + getToken();
  } 

  private static String getToken() {
    return "9ce683cf44f906f9fe805c7d9df6d31f345e729b";
  }
  
}
