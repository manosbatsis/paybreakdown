package paybreakdown.model;

import java.math.BigDecimal;
import java.util.Currency;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *  Each item has worker id (String), rate name (String), total work time (Duration), total pay (Currency).
 */
@Data
@Builder
public class BreakDownItem {

	@NonNull
	private String workerId;

	@NonNull
	private String rateName;

	@NonNull
	private long workMinutes;

	@NonNull
	private BigDecimal pay;

	@NonNull
	private Currency currency;

	@Override
	public String toString() {
		long hours = Math.floorDiv(workMinutes, 60);
		String time = new StringBuffer().append(hours).append(':').append(workMinutes - (hours * 60)).toString();
		return new ToStringBuilder(this)
				.append("workerId", workerId)
				.append("rateName", rateName)
				.append("time", time)
				.append("currency", currency)
				.append("pay", pay).toString();
	}
}
