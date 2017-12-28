package paybreakdown;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import paybreakdown.model.BreakDownItem;
import paybreakdown.model.PayRate;
import paybreakdown.model.Shift;
import paybreakdown.util.WorkCounters;

/**
 * Generates a pay breakdown for a list of shifts.
 */
@Slf4j
public class App {

	/**
	 * Get the breakdown items for the given shifts and pay rates
	 * @param shifts
	 * @param payRates
	 * @return
	 */
	public static List<BreakDownItem> breakdown(List<Shift> shifts, List<PayRate> payRates) {

		// Create work counters for the given rates and shifts
		WorkCounters workCounters = new WorkCounters(payRates, shifts);
		// Breakdown
		List<BreakDownItem> breakDownItems = workCounters.breakdown();

		return breakDownItems;
	}
}
