package com.renata.domain.enums;

public enum MarketEventType {
    LISTED, // Item enters the market
    PRICE_UPDATED, // Regular price fluctuation
    PURCHASED, // Item was bought (freeze price)
    RELISTED // Item is available again
}
