package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.TicketServiceImpl.TICKET_PRICES;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private static final long VALID_ACCOUNT_ID = 123;

    private static final TicketTypeRequest ONE_ADULT_TICKET = new TicketTypeRequest(ADULT, 1);

    private static final int MULTIPLE_ADULTS = 5;
    private static final TicketTypeRequest MULTIPLE_ADULT_TICKETS = new TicketTypeRequest(ADULT, MULTIPLE_ADULTS);



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
        service.purchaseTickets(VALID_ACCOUNT_ID, ONE_ADULT_TICKET);

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, TICKET_PRICES.get(ADULT));
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, 1);
    }

    @Test
    void purchaseTickets_forMultipleAdults() {
        service.purchaseTickets(VALID_ACCOUNT_ID, MULTIPLE_ADULT_TICKETS);

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, MULTIPLE_ADULTS * TICKET_PRICES.get(ADULT));
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, MULTIPLE_ADULTS);
    }

    @Test
    void purchaseTickets_handlesMultipleRequestsOfTheSameType() {
        service.purchaseTickets(VALID_ACCOUNT_ID, ONE_ADULT_TICKET, MULTIPLE_ADULT_TICKETS);

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, (1 + MULTIPLE_ADULTS) * TICKET_PRICES.get(ADULT));
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, 1 + MULTIPLE_ADULTS);
    }
}
