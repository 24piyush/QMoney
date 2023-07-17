package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDate;
import java.util.List;

public interface StockQuotesService {

  //CHECKSTYLE:OFF
  List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException
  ;
  //CHECKSTYLE:ON

}
