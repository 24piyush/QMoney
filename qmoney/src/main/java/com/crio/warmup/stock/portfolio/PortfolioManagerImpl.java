
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.client.RestTemplate;
import com.crio.warmup.stock.dto.CompareByAnnualizedReturnsDescending;
import java.util.Collections;
import org.springframework.http.ResponseEntity;



public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }
  
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
        List<AnnualizedReturn> list = new ArrayList<>();
        for(PortfolioTrade trade : portfolioTrades) {
          String symbol = trade.getSymbol();
          LocalDate purchaseDate = trade.getPurchaseDate();
          List<Candle> candles;
          if(stockQuotesService != null){
            try {
              candles = stockQuotesService.getStockQuote(symbol, purchaseDate, endDate);
            } catch (JsonProcessingException e) {
              e.printStackTrace();
              return Collections.emptyList();
            }
          }
          else{
            candles = getStockQuote(symbol, purchaseDate, endDate);
          }
          double buyPrice = getOpeningPriceOnStartDate(candles);
          double sellPrice = getClosingPriceOnEndDate(candles);
          list.add(calculateSingleTradeAnnualizedReturns(endDate, trade, buyPrice, sellPrice));
        }
        Collections.sort(list,new CompareByAnnualizedReturnsDescending());
        return list;
      }

  public static AnnualizedReturn calculateSingleTradeAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        LocalDate purchaseDate = trade.getPurchaseDate();
        String symbol = trade.getSymbol();

        Double totalReturn = (sellPrice - buyPrice) / buyPrice;
        Double totalYears = getTotalYears(purchaseDate,endDate);
        Double annualReturns = Math.pow((1+totalReturn), 1/totalYears) - 1;
        return new AnnualizedReturn(symbol, annualReturns, totalReturn);
  }

  //CHECKSTYLE:OFF

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) {
        String url = buildUri(symbol, from, to);
        ResponseEntity<TiingoCandle[]> response = restTemplate.getForEntity(url,TiingoCandle[].class);
        TiingoCandle[] data = response.getBody();
        return Arrays.asList(data);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
      //  String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
      //       + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
      return  "https://api.tiingo.com/tiingo/daily/"+ symbol +"/prices?startDate="+ 
      startDate +"&endDate="+ endDate +"&token=" + getToken();
  }

  public static String getToken() {
    return "9ce683cf44f906f9fe805c7d9df6d31f345e729b";
  }

  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    if(candles == null) return 0.0;
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    if(candles == null) return 0.0;
    return candles.get(candles.size() - 1).getClose();
  }

  private static Double getTotalYears(LocalDate start, LocalDate end) {
    long totalMonths = ChronoUnit.DAYS.between(start, end);
    double totalYears = totalMonths / 365.24;
    return totalYears;
  }
  
}
