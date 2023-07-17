package com.crio.warmup.stock.dto;

import java.util.Comparator;

public class CompareByAnnualizedReturnsDescending implements Comparator<AnnualizedReturn> {

    @Override
    public int compare(AnnualizedReturn arg0, AnnualizedReturn arg1) {
      return Double.compare(arg1.getAnnualizedReturn(), arg0.getAnnualizedReturn());
    }
    
  }