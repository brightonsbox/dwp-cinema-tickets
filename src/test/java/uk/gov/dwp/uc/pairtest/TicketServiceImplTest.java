package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private static final long VALID_ACCOUNT_ID = 123;

    private static final int TICKET_PRICE_ADULT = 20;
    private static final int TICKET_PRICE_CHILD = 10;

    private static final int MAXIMUM_TICKET_AMOUNT = 20;

    private static final TicketTypeRequest SINGLE_ADULT_TICKET = new TicketTypeRequest(ADULT, 1);

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    private TicketService service;

    @BeforeEach
    void setUp() {
        service = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    void purchaseTickets_forASingleAdult() {
        service.purchaseTickets(VALID_ACCOUNT_ID, SINGLE_ADULT_TICKET);

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, TICKET_PRICE_ADULT);
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, 1);
    }

    @Test
    void purchaseTickets_forMultipleAdults() {
        final var multiple = 5;

        service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(ADULT, multiple));

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, multiple * TICKET_PRICE_ADULT);
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, multiple);
    }

    @Test
    void purchaseTickets_handlesMultipleRequestsOfTheSameType() {
        service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(ADULT, 2), new TicketTypeRequest(ADULT, 3));

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, 5 * TICKET_PRICE_ADULT);
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, 5);
    }

    @Test
    void purchaseTickets_forMultipleTypes() {
        service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(ADULT, 1),
                new TicketTypeRequest(CHILD, 2),
                new TicketTypeRequest(INFANT, 3));

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, TICKET_PRICE_ADULT + (2 * TICKET_PRICE_CHILD));
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, 3);
    }

    @Test
    void purchaseTickets_maximumNumberOfTicketsAllowed() {
        service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(ADULT, MAXIMUM_TICKET_AMOUNT));

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, MAXIMUM_TICKET_AMOUNT * TICKET_PRICE_ADULT);
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, MAXIMUM_TICKET_AMOUNT);
    }

    @Test
    void purchaseTickets_maximumNumberOfTicketsAllowedAcrossAnyType() {
        service.purchaseTickets(VALID_ACCOUNT_ID,
                new TicketTypeRequest(ADULT, 1),
                new TicketTypeRequest(CHILD, 4),
                new TicketTypeRequest(INFANT, 15));

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, TICKET_PRICE_ADULT + (4 * TICKET_PRICE_CHILD));
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, 5);
    }

    @Test
    void purchaseTickets_exceedingMaximumTicketCountThrowsException() {
        assertThrowsInvalidPurchaseException(VALID_ACCOUNT_ID, new TicketTypeRequest(ADULT, MAXIMUM_TICKET_AMOUNT + 1));
    }

    @Test
    void purchaseTickets_exceedingMaximumTicketCountAcrossAnyTypeThrowsException() {
        assertThrowsInvalidPurchaseException(VALID_ACCOUNT_ID,
                new TicketTypeRequest(ADULT, 5),
                new TicketTypeRequest(CHILD, 10),
                new TicketTypeRequest(INFANT, 16));
    }

    @Test
    void purchaseTickets_cannotRequestZeroTickets() {
        assertThrowsInvalidPurchaseException(VALID_ACCOUNT_ID);
        assertThrowsInvalidPurchaseException(VALID_ACCOUNT_ID, new TicketTypeRequest(ADULT, 0));
    }

    @Test
    void purchaseTickets_cannotRequestChildTicketWithoutAdult() {
        assertThrowsInvalidPurchaseException(VALID_ACCOUNT_ID, new TicketTypeRequest(CHILD, 1));
    }

    @Test
    void purchaseTickets_cannotRequestInfantTicketWithoutAdult() {
        assertThrowsInvalidPurchaseException(VALID_ACCOUNT_ID, new TicketTypeRequest(INFANT, 1));
    }

    @Test
    void purchaseTickets_cannotRequestInfantAndChildTicketsWithoutAdult() {
        assertThrowsInvalidPurchaseException(VALID_ACCOUNT_ID,
                new TicketTypeRequest(INFANT, 1),
                new TicketTypeRequest(CHILD, 1));
    }

    @Test
    void purchaseTickets_accountNumberGreaterThanZeroValid() {
        service.purchaseTickets(1L, SINGLE_ADULT_TICKET);

        verify(ticketPaymentService).makePayment(1L, TICKET_PRICE_ADULT);
        verify(seatReservationService).reserveSeat(1L, 1);
    }

    @Test
    void purchaseTickets_nullAccountNumberThrowsException() {
        assertThrowsInvalidPurchaseException(null, SINGLE_ADULT_TICKET);
    }

    @Test
    void purchaseTickets_accountNumberLessThanOneThrowsException() {
        assertThrowsInvalidPurchaseException(0L, SINGLE_ADULT_TICKET);
        assertThrowsInvalidPurchaseException(-1L, SINGLE_ADULT_TICKET);
    }

    private void assertThrowsInvalidPurchaseException(final Long accountId, final TicketTypeRequest... ticketTypeRequests) {
        assertThatThrownBy(() -> service.purchaseTickets(accountId, ticketTypeRequests))
                .isInstanceOf(InvalidPurchaseException.class);

    }
}
