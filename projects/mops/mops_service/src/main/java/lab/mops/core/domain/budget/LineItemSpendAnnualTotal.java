package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.Year;

public record LineItemSpendAnnualTotal(Year year, BigDecimal total) {}
