package paybreakdown.model;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Each pay rate has name (String), hourly rate (Currency),
 * time of day start (Time) and time of day end (Time).
 */
@Data
@Builder
@AllArgsConstructor
public class PayRate {
	@NonNull
	private String name;

	@NonNull
	private BigDecimal hourlyRate;

	@NonNull
	private Currency currency;

	private LocalTime start;

	private LocalTime end;
}
