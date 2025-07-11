package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.Month;

public record LineItemSpendMonthlyTotal(Month month, BigDecimal total) {}
