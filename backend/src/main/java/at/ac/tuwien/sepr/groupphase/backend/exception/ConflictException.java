package at.ac.tuwien.sepr.groupphase.backend.exception;

import java.util.List;

/**
 * Exception that signals data conflicts from external sources.
 *
 * <p>
 * This exception is thrown when incoming data conflicts with the current system state,
 * typically due to violations of constraints on relationships, rather than invariants.
 * It contains a list of all conflict checks that failed during validation of the data.
 * </p>
 */
public class ConflictException extends ErrorListException {

    /**
     * Constructs a {@code ConflictException} with a summary message and a list of detailed errors.
     *
     * @param messageSummary a brief description summarizing the conflict
     * @param errors         a list of specific conflict errors that were encountered
     */
    public ConflictException(String messageSummary, List<String> errors) {
        super("Conflicts", messageSummary, errors);
    }
}