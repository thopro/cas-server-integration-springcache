package no.get.cas.ticket.registry;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;


/**
 * TicketRegistry to support Spring Cache Abstraction.
 */

public final class SpringCacheTicketRegistry extends AbstractDistributedTicketRegistry {

    private String cacheName;

    private CacheManager cacheManager;

    @Override
    protected void updateTicket(Ticket ticket) {
        cacheManager.getCache(cacheName).put(ticket.getId(), ticket);
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
        Cache cache = cacheManager.getCache(cacheName);
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
        Cache.ValueWrapper valueWrapper = cacheManager.getCache(cacheName).get(ticketId);
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
            cacheManager.getCache(cacheName).evict(ticketId);
            return true;
        }
    }

    /**
     * Unsupported in Spring Cache Abstraction.
     *
     * Retrieve all tickets from the registry.
     *
     * @return collection of tickets currently stored in the registry. Tickets
     *         might or might not be valid i.e. expired.
     */
    @Override
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("Spring Cache does not support retrieval of entire cached content. If you used a registry cleaner, please adjust TTL of your cache instead");
    }

    public void setCacheManager(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setCacheName(final String cacheName) {
        this.cacheName = cacheName;
    }
}
