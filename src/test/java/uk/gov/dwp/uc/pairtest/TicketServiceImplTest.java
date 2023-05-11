package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private static final long VALID_ACCOUNT_ID = 123;
    private static final TicketTypeRequest ONE_ADULT_TICKET = new TicketTypeRequest(ADULT, 1);
    private static final int COST_FOR_ONE_ADULT = 10;

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
    void purchaseTickets() {
        service.purchaseTickets(VALID_ACCOUNT_ID, ONE_ADULT_TICKET);

        verify(ticketPaymentService).makePayment(VALID_ACCOUNT_ID, COST_FOR_ONE_ADULT);
        verify(seatReservationService).reserveSeat(VALID_ACCOUNT_ID, 1);
    }
}
