package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImpl implements TicketService {

    private static final int MAXIMUM_TICKET_COUNT = 20;
    private static final Map<Type, Integer> TICKET_PRICES = Map.of(
            ADULT, 20,
            CHILD, 10,
            INFANT, 0);

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(final TicketPaymentService ticketPaymentService, final SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(final Long accountId, final TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateAccount(accountId);

        final var ticketCounts = Stream.of(ticketTypeRequests)
                .collect(groupingByConcurrent(TicketTypeRequest::getTicketType, summingInt(TicketTypeRequest::getNoOfTickets)));

        validate(ticketCounts);

        ticketPaymentService.makePayment(accountId, getTotalCost(ticketCounts));
        seatReservationService.reserveSeat(accountId, getNumberOfSeatsRequired(ticketCounts));
    }

    private int getTicketCount(final Map<Type, Integer> ticketCounts, final Predicate<Type> filter) {
        return ticketCounts.keySet().stream()
                .filter(filter)
                .mapToInt(ticketCounts::get)
                .sum();
    }

    private int getTicketCountFor(final Map<Type, Integer> ticketCounts, final Type type) {
        return getTicketCount(ticketCounts, t -> t == type);
    }

    private int getTotalTicketCount(final Map<Type, Integer> ticketCounts) {
        return getTicketCount(ticketCounts, x -> true);
    }

    private int getNumberOfSeatsRequired(final Map<Type, Integer> ticketCounts) {
        return getTicketCount(ticketCounts, this::isSeatRequired);
    }

    private boolean isSeatRequired(final Type type) {
        return type != INFANT;
    }

    private int getTotalCost(final Map<Type, Integer> ticketCounts) {
        return ticketCounts.keySet().stream()
                .mapToInt(type -> (getPriceFor(type) * ticketCounts.get(type))).sum();
    }

    private int getPriceFor(final Type type) throws InvalidPurchaseException {
        if (!TICKET_PRICES.containsKey(type)) {
            throw new InvalidPurchaseException();
        }

        return TICKET_PRICES.get(type);
    }

    private void validateAccount(final Long accountId) {
        if (accountId == null || accountId < 1) {
            throw new InvalidPurchaseException();
        }
    }

    private void validate(final Map<Type, Integer> ticketCounts) {
        mustNotExceedMaximumTicketCount(ticketCounts);
        atLeastOneAdultRequired(ticketCounts);
    }

    private void mustNotExceedMaximumTicketCount(final Map<Type, Integer> ticketCounts) {
        if (getTotalTicketCount(ticketCounts) > MAXIMUM_TICKET_COUNT) {
            throw new InvalidPurchaseException();
        }
    }

    private void atLeastOneAdultRequired(final Map<Type, Integer> ticketCounts) {
        if (getTicketCountFor(ticketCounts, ADULT) < 1) {
            throw new InvalidPurchaseException();
        }
    }
}
