package org.example.virtuber.stock.service;

import lombok.RequiredArgsConstructor;
import org.example.virtuber.stock.dto.StockResponse;
import org.example.virtuber.stock.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;

  public List<StockResponse> getStocks() {
    return stockRepository.findAll()
        .stream()
        .map(StockResponse::from)
        .toList();
  }
}
