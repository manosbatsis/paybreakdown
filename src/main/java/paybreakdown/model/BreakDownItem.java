package paybreakdown.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import lombok.Data;
import lombok.NonNull;

/**
 *  Each item has worker id (String), rate name (String), total work time (Duration), total pay (Currency).
 */
@Data
public class BreakDownItem {

	@NonNull
	private String workerId;

	@NonNull
	private String name;

	@NonNull
	private int totalWorkTimeMinutes;

	@NonNull
	private BigDecimal totalPay;

	@NonNull
	private Currency currency;
}
