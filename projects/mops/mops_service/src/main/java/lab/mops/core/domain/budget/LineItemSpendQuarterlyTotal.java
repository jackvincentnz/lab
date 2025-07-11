package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import lab.mops.core.domain.time.Quarter;

public record LineItemSpendQuarterlyTotal(Quarter quarter, BigDecimal total) {}
