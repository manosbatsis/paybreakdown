package paybreakdown;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import paybreakdown.model.BreakDownItem;
import paybreakdown.model.PayRate;
import paybreakdown.model.Shift;

/**
 * Unit test for {@link App}
 */
public class AppTest
		extends TestCase {

	public static final Currency GBP = Currency.getInstance("GBP");

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Breakdown test
	 */
	public void testApp() {
		String str = "1986-04-08 12:30";
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

		List<Shift> shifts = getSampleShifts(dateTimeFormatter);

		List<PayRate> payRates = getSamplePayRates(timeFormatter);

		// TODO: test the results
		List<BreakDownItem> results = App.breakdown(shifts, payRates);
	}

	/**
	 * Rates (name, hourly rate, start, end):
	 * <ul><li>Default, £10.00, null, null</li>
	 * <li>Morning, £15.00, 05:00, 10:00</li>
	 * <li>Evening, £18.00, 16:30, 20:00</li>
	 * <li>Night, £20.00, 20:00, 23:00</li>
	 * </ul>
	 * @param timeFormatter
	 * @return
	 */
	protected List<PayRate> getSamplePayRates(DateTimeFormatter timeFormatter) {
		List<PayRate> payRates = new ArrayList<>(4);
		payRates.add(PayRate.builder().name("Default").currency(GBP).hourlyRate(BigDecimal.valueOf(10.00)).build());

		payRates.add(PayRate.builder().name("Morning")
				.currency(GBP)
				.hourlyRate(BigDecimal.valueOf(15.00))
				.start(LocalTime.parse("05:00", timeFormatter))
				.end(LocalTime.parse("10:00", timeFormatter)).build());

		payRates.add(PayRate.builder().name("Evening")
				.currency(GBP)
				.hourlyRate(BigDecimal.valueOf(18.00))
				.start(LocalTime.parse("16:30", timeFormatter))
				.end(LocalTime.parse("20:00", timeFormatter)).build());

		payRates.add(PayRate.builder().name("Night")
				.currency(GBP)
				.hourlyRate(BigDecimal.valueOf(20.00))
				.start(LocalTime.parse("20:00", timeFormatter))
				.end(LocalTime.parse("23:00", timeFormatter)).build());
		return payRates;
	}

	protected List<Shift> getSampleShifts(DateTimeFormatter dateTimeFormatter) {
		List<Shift> shifts = new ArrayList<>(2);
		shifts.add(Shift.builder().workerId("John")
				.start(LocalDateTime.parse("2017-06-23 09:00", dateTimeFormatter))
				.end(LocalDateTime.parse("2017-06-23 17:00", dateTimeFormatter))
				.build());
		shifts.add(Shift.builder().workerId("John")
				.start(LocalDateTime.parse("2017-06-24 06:00", dateTimeFormatter))
				.end(LocalDateTime.parse("2017-06-24 14:00", dateTimeFormatter))
				.build());
		return shifts;
	}
}
