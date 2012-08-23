package no.get.integrations.cas.ticket.registry;


import no.get.commons.logging.Logger;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;


/**
 * TicketRegistry to support Spring Cache Abstraction.
 */

public final class SpringCacheTicketRegistry extends AbstractDistributedTicketRegistry {

    private static final String FQN_TICKET = "cas-store";

    private CacheManager cacheManager;

    @Override
    protected void updateTicket(Ticket ticket) {
        cacheManager.getCache(FQN_TICKET).put(ticket.getId(), ticket);
    }

    @Override
    protected boolean needsCallback() {
        return true;
    }

    /**
     * Add a ticket to the registry. Ticket storage is based on the ticket id.
     *
     * @param ticket The ticket we wish to add to the cache.
     */
    @Override
    public void addTicket(Ticket ticket) {
        Cache cache = cacheManager.getCache(FQN_TICKET);
        cache.put(ticket.getId(), ticket);
    }

    /**
     * Retrieve a ticket from the registry.
     *
     * @param ticketId the id of the ticket we wish to retrieve
     * @return the requested ticket.
     */
    @Override
    public Ticket getTicket(String ticketId) {
        Cache.ValueWrapper valueWrapper = cacheManager.getCache(FQN_TICKET).get(ticketId);
        if (valueWrapper == null) {
            return getProxiedTicketInstance(null);
        }

        return getProxiedTicketInstance((Ticket) valueWrapper.get());
    }

    /**
     * Remove a specific ticket from the registry.
     *
     * @param ticketId The id of the ticket to delete.
     * @return true if the ticket was removed and false if the ticket did not
     *         exist.
     */
    @Override
    public boolean deleteTicket(String ticketId) {
        if (getTicket(ticketId) == null) {
            return false;
        } else {
            cacheManager.getCache(FQN_TICKET).evict(ticketId);
            return true;
        }
    }

    /**
     * Retrieve all tickets from the registry.
     *
     * @return collection of tickets currently stored in the registry. Tickets
     *         might or might not be valid i.e. expired.
     */
    @Override
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("Spring cache does not support retrieval of entire cached content");
    }

    public void setCacheManager(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
}
