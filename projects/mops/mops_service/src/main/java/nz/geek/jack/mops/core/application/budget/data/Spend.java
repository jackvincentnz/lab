package nz.geek.jack.mops.core.application.budget.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Spend(LocalDate spendDay, BigDecimal amount) {}
