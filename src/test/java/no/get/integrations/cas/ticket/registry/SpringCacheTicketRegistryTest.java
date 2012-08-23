package no.get.integrations.cas.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/integrations-cas-server-integration-springcache-ModuleTestContext.xml")
public class SpringCacheTicketRegistryTest {


    static {
        System.setProperty("environment", "development");
    }

    @Autowired
    private SpringCacheTicketRegistry springCacheTicketRegistry;

    @Test
    public void updateTicketShouldOverwriteTicketInStorage() {
        Ticket ticket = getTicket();
        springCacheTicketRegistry.addTicket(ticket);
        assertThat(springCacheTicketRegistry.getTicket(ticket.getId()).isExpired()).isFalse();
        TicketGrantingTicket ticket2 = (TicketGrantingTicket) ticket;
        ticket2.expire();
        springCacheTicketRegistry.updateTicket(ticket);
        assertThat(springCacheTicketRegistry.getTicket(ticket.getId()).isExpired()).isTrue();
    }

    @Test
    public void addTicketExistsInCache() {
        Ticket ticket = getTicket();
        springCacheTicketRegistry.addTicket(ticket);
        assertThat(springCacheTicketRegistry.getTicket(ticket.getId())).isEqualTo(ticket);
    }

    @Test
    public void deleteTicketRemovesFromCacheReturnsTrue() {
        Ticket ticket = getTicket();
        springCacheTicketRegistry.addTicket(ticket);
        assertThat(springCacheTicketRegistry.deleteTicket(ticket.getId())).isTrue();
        assertThat(springCacheTicketRegistry.getTicket(ticket.getId())).isNull();
    }

    @Test
    public void deleteTicketOnNonExistingTicketReturnsFalse() {
        String ticketId = "does_not_exist";
        assertThat(springCacheTicketRegistry.deleteTicket(ticketId)).isFalse();
    }

    @Test
    public void getTicketReturnsTicketFromCacheOrNull() {
        Ticket ticket = getTicket();
        springCacheTicketRegistry.addTicket(ticket);
        assertThat(springCacheTicketRegistry.getTicket(ticket.getId())).isEqualTo(ticket);
        assertThat(springCacheTicketRegistry.getTicket("")).isNull();
    }

    private Ticket getTicket() {
        Principal principal = new SimpleWebApplicationServiceImpl("http://www.get.no");
        Authentication authentication = new ImmutableAuthentication(principal);
        return new TicketGrantingTicketImpl("123", authentication, new NeverExpiresExpirationPolicy());
    }
}
