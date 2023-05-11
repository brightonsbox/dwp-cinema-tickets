package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceImplTest {

    private static final long VALID_ACCOUNT_ID = 123;

    final TicketService service = new TicketServiceImpl();

    @Test
    void purchaseTickets() {
        service.purchaseTickets(VALID_ACCOUNT_ID);
    }
}
