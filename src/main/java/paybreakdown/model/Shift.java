package paybreakdown.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Each shift has start (DateTime), end (DateTime) and worker id (String)
 */
@Data
@Builder
public class Shift {
	@NonNull
	private LocalDateTime start;

	@NonNull
	private LocalDateTime end;

	@NonNull
	private String workerId;
}
