package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImpl implements TicketService {

    static final Map<Type, Integer> TICKET_PRICES = Map.of(
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

        final var ticketCounts = Stream.of(ticketTypeRequests)
                .collect(groupingByConcurrent(TicketTypeRequest::getTicketType, summingInt(TicketTypeRequest::getNoOfTickets)));

        ticketPaymentService.makePayment(accountId, getTotalCost(ticketCounts));
        seatReservationService.reserveSeat(accountId, getNumberOfSeats(ticketCounts));
    }

    private int getNumberOfSeats(final Map<Type, Integer> ticketCounts) {
        return ticketCounts.keySet().stream()
                .filter(this::isSeatRequired)
                .mapToInt(ticketCounts::get)
                .sum();
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
}
