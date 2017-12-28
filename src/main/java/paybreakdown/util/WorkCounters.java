package paybreakdown.util;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.threeten.extra.Interval;
import paybreakdown.model.BreakDownItem;
import paybreakdown.model.PayRate;
import paybreakdown.model.Shift;

/**
 * Helper that allows using {@PayRate}s and {@link Shift}s to maintain
 * a collection of {@link WorkCounter} instances in-memory.
 *
 * This implementation is not thread-safe.
 */
@Slf4j
public class WorkCounters {

	public static final String EUROPE_LONDON = "Europe/London";

	private static final BigDecimal MINUTES_IN_HOUR = BigDecimal.valueOf(60);

	private final List<PayRate> sortedPayRates;
	private final PayRate defaultPayRate;
	private final Map<String, WorkCounter> workCountersPerWorker;

	/**
	 * Create a new instance based on the given pay-rates
	 * @param payRates
	 */
	public WorkCounters(@NonNull List<PayRate> payRates, List<Shift> shifts){

		// Sort pay-rates, put any default (null start time) rate last
		payRates.sort(comparing(PayRate::getStart, nullsLast(naturalOrder())));
		// Initialize default rate
		PayRate lastRate = payRates.get(payRates.size() -1);
		if(Objects.isNull(lastRate.getStart())){
			payRates.remove(payRates.size() -1);
			defaultPayRate = lastRate;
		}
		else {
			defaultPayRate = null;
		}
		// Initialize other rates
		this.sortedPayRates = payRates;
		// Initialize work counters
		this.workCountersPerWorker = new HashMap<>();
		// Process shifts
		this.addShifts(shifts);
	}

	/**
	 * Add the input shifts
	 * @param shifts
	 */
	protected void addShifts(@NonNull Iterable<Shift> shifts){
		for (Shift shift : shifts){
			addShift(shift);
		}
	}
	/**
	 * Add the information of the given shift
	 * TODO: Handle payrates and shifts that break out of a 24h/calendar day
	 * @param shift
	 */
	protected void addShift(@NonNull Shift shift){
		// Get the counter for the target worker
		WorkCounter workerWorkCounter = this.getWorkCounter(shift.getWorkerId());

		// Create an Interval for the shift
		Instant shiftStart = ZonedDateTime.of(shift.getStart(), ZoneId.of(EUROPE_LONDON)).toInstant();
		Instant shiftEnd = ZonedDateTime.of(shift.getEnd(), ZoneId.of(EUROPE_LONDON)).toInstant();
		Interval shiftInterval = Interval.of(shiftStart, shiftEnd);
		// Will end up with the default rate minutes
		long shiftMinutesOnDefaultRate = shiftInterval.toDuration().toMinutes();

		// Add the shift time per rate
		for(PayRate payRate : sortedPayRates){
			// Create an Interval for the rate
			Instant rateStart = ZonedDateTime.of(shift.getStart().toLocalDate(), payRate.getStart(), ZoneId.of(EUROPE_LONDON)).toInstant();
			Instant rateEnd = ZonedDateTime.of(shift.getEnd().toLocalDate(), payRate.getEnd(), ZoneId.of(EUROPE_LONDON)).toInstant();
			Interval rateInterval = Interval.of(rateStart, rateEnd);

			// Get the intersection, if any
			if(rateInterval.overlaps(shiftInterval)){
				Interval intersection = rateInterval.intersection(shiftInterval);
				// Add minutes to the work counter's rate bucket
				long rateMinutes = intersection.toDuration().toMinutes();
				workerWorkCounter.addMinutes(payRate.getName(), rateMinutes);
				// Remove rate minutes from shift's default rate minutes
				shiftMinutesOnDefaultRate -= rateMinutes;
			}
		}

		// Add the remaining time as default rate

		log.info("addShift, defaultPayRate: {}, shiftMinutesOnDefaultRate: {}", this.defaultPayRate, shiftMinutesOnDefaultRate);
		workerWorkCounter.addMinutes(this.defaultPayRate.getName(), shiftMinutesOnDefaultRate);
	}

	/**
	 * Get the existing punch-card for the given worker, or create a new one if missing
	 * @param workerId
	 * @return
	 */
	protected WorkCounter getWorkCounter(@NonNull String workerId){
		// Get the punch-card, if any
		WorkCounter workCounter = this.workCountersPerWorker.get(workerId);
		// Create a punch-card for the given worker if missing
		if(Objects.isNull(workCounter)){
			workCounter = new WorkCounter(this.sortedPayRates, this.defaultPayRate);
			this.workCountersPerWorker.put(workerId, workCounter);
		}
		// return the punch-card either way
		return workCounter;
	}

	/**
	 * Break down this instance into a list of {@link BreakDownItem}
	 * @return
	 */
	public List<BreakDownItem> breakdown() {
		List<BreakDownItem> breakDownItems = new LinkedList<>();
		// Iterate worker counters
		for(String workerId : this.workCountersPerWorker.keySet()){
			// Get worker counter
			WorkCounter workerCounter = this.workCountersPerWorker.get(workerId);
			// Create a breakdown item per counter rate bucket
			for (PayRate payRate : sortedPayRates){
				// Add item if applicable
				addBreakDownItem(breakDownItems, workerId, workerCounter, payRate);
			}
			// Add default rate if any and applicable
			if(Objects.nonNull(this.defaultPayRate)){
				addBreakDownItem(breakDownItems, workerId, workerCounter, this.defaultPayRate);
			}
		}
		// Return breakdown items
		return breakDownItems;
	}

	/**
	 * Add a breakdown item to the list if applicable, i.e. if work under the specified rate has been performed
	 * @param breakDownItems
	 * @param workerId
	 * @param workerCounter
	 * @param payRate
	 */
	protected void addBreakDownItem(List<BreakDownItem> breakDownItems, String workerId, WorkCounter workerCounter, PayRate payRate) {
		// Get minutes worked under this rate
		long rateMinutes = workerCounter.minutesPerRate.get(payRate.getName());
		if(rateMinutes > 0){
			// Calculate pay
			BigDecimal pay = payRate.getHourlyRate().multiply(BigDecimal.valueOf(rateMinutes).divide(MINUTES_IN_HOUR));
			// Add item
			breakDownItems.add(BreakDownItem.builder()
					.workerId(workerId)
					.rateName(payRate.getName())
					.currency(payRate.getCurrency())
					.pay(pay)
					.workMinutes(rateMinutes).build());
		}
	}

	/**
	 * Used to keep track of hours worked per worker, per rate
	 * @see WorkCounters
	 */
	protected static class WorkCounter {

		/** Bucket of hours per rate */
		final Map<String, Long> minutesPerRate;

		/**
		 * Initialise an instance with the given rate names
		 * @param payRates
		 */
		WorkCounter(@NonNull List<PayRate> payRates, PayRate defaultRate){
			// Create a bucket of hours per pay-rate
			minutesPerRate = payRates.stream().distinct()
					.collect(Collectors.toMap(PayRate::getName, s -> new Long(0)));
			// Add a bucket for the default rate, if any
			if(Objects.nonNull(defaultRate)){
				minutesPerRate.put(defaultRate.getName(), new Long(0));
			}
		}

		/**
		 * Add work time to the corresponding rate
		 */
		public void addMinutes(@NonNull String rateName, @NonNull Long minutesToAdd){
			log.info("addMinutes, rateName: {}, minutesToAdd: {}", rateName, minutesToAdd);
			log.info("addMinutes, minutesPerRate: {}", this.minutesPerRate);
			Long minutes = this.minutesPerRate.get(rateName);
			log.info("addMinutes, minutes: {}", minutes);
			this.minutesPerRate.put(rateName, Long.sum(minutes, minutesToAdd));
		}
	}
}
