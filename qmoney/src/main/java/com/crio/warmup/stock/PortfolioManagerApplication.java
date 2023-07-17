
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.ResponseEntity;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    om.registerModule(new JavaTimeModule());
    PortfolioTrade[] trades = om.readValue(file, PortfolioTrade[].class);

    List<String> list = new ArrayList<>();
    for(PortfolioTrade trade : trades){
      list.add(trade.getSymbol());
    }
    return list;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/raghavkansal19-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
     String functionNameFromTestFileInStackTrace = "mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "29";
    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    String fileName = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);

    File file = resolveFileFromResources(fileName);
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    PortfolioTrade[] trades = om.readValue(file, PortfolioTrade[].class);

    List<TotalReturnsDto> list = new ArrayList<>();
    for(PortfolioTrade trade : trades){
      String symbol = trade.getSymbol();
      TiingoCandle[] response = APICall(trade, endDate);
      if(response != null){
        Double closingPrice = response[response.length -1].getClose();
        list.add(new TotalReturnsDto(symbol,closingPrice));
      }
    }
    Collections.sort(list);
    List<String> symbolsList = new ArrayList<>();
    for(TotalReturnsDto ob : list){
      symbolsList.add(ob.getSymbol());
    }
    return symbolsList;
  }

  public static TiingoCandle[] APICall(PortfolioTrade trade, LocalDate endDate){
    String token = "9ce683cf44f906f9fe805c7d9df6d31f345e729b";
    String url = prepareUrl(trade,endDate,token);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<TiingoCandle[]> response = restTemplate.getForEntity(url,TiingoCandle[].class);
    TiingoCandle[] data = response.getBody();
    return data;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    PortfolioTrade[] trades = om.readValue(file, PortfolioTrade[].class);

    ArrayList<PortfolioTrade> list = new ArrayList<>();
    for(PortfolioTrade trade : trades){
      list.add(trade);
    }
    return list;
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    return  "https://api.tiingo.com/tiingo/daily/"+ trade.getSymbol() +"/prices?startDate="+ 
    trade.getPurchaseDate() +"&endDate="+ endDate +"&token=" + token;
  }
  

  //  ./gradlew test --tests ModuleThreeRefactorTest
  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    if(candles == null) return 0.0;
    return candles.get(0).getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    if(candles == null) return 0.0;
    return candles.get(candles.size() - 1).getClose();
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = prepareUrl(trade, endDate, token);
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<TiingoCandle[]> response = restTemplate.getForEntity(url,TiingoCandle[].class);
    TiingoCandle[] data = response.getBody();
    return Arrays.asList(data);
  }

  public static String getToken() {
    return "9ce683cf44f906f9fe805c7d9df6d31f345e729b";
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
    
    String filename = args[0];
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate endDate = LocalDate.parse(args[1], formatter);
    List<PortfolioTrade> trades = readTradesFromJson(filename);
    List<AnnualizedReturn> list = new ArrayList<>();
    
    for(PortfolioTrade trade : trades) {
      List<Candle> candles = fetchCandles(trade, endDate, getToken());
      double buyPrice = getOpeningPriceOnStartDate(candles);
      double sellPrice = getClosingPriceOnEndDate(candles);
      list.add(calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice));
    }
    Collections.sort(list,new CompareByAnnualizedReturnsDescending());
    return list;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn
  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        LocalDate purchaseDate = trade.getPurchaseDate();
        String symbol = trade.getSymbol();

        Double totalReturn = (sellPrice - buyPrice) / buyPrice;
        Double totalYears = getTotalYears(purchaseDate,endDate);
        Double annualReturns = Math.pow((1+totalReturn), 1/totalYears) - 1;
        return new AnnualizedReturn(symbol, annualReturns, totalReturn);
  }

  private static Double getTotalYears(LocalDate start, LocalDate end) {
    long totalMonths = ChronoUnit.DAYS.between(start, end);
    double totalYears = totalMonths / 365.24;
    return totalYears;
  }
  
  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = getPortfolioListFromJsonString(contents, objectMapper);
       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }

  public static String readFileAsString(String fileName) throws Exception{
    //File file = resolveFileFromResources(fileName);
    return new String(Files.readAllBytes(Paths.get(resolveFileFromResources(fileName).getAbsolutePath())));
  }

  public static PortfolioTrade[] getPortfolioListFromJsonString(String jsonString, ObjectMapper objectMapper) throws Exception {
    PortfolioTrade[] portfolioTrades = objectMapper.readValue(jsonString, PortfolioTrade[].class);
    return portfolioTrades;
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

