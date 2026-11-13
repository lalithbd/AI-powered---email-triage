package org.galactic.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.galactic.dto.view.StatChip;
import org.galactic.model.Classification;
import org.galactic.util.Category;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ClassificationRepository implements PanacheRepository<Classification> {

    public List<Classification> findClassificationMails(Instant dayAgo) {
        return find("select c from classification c join fetch c.email e where e.receivedAt >= :dayAgo order by e.receivedAt desc",
                Map.of("dayAgo", dayAgo)).list();
    }

    public List<Classification> findClassificationMails(Instant weekAgo, int pageNo, int pageSize) {
        return find("select c from classification c join fetch c.email e where e.receivedAt >= :weekAgo order by e.receivedAt desc",
                Map.of("weekAgo", weekAgo)).page(pageNo, pageSize).list();
    }

    public List<Classification> findClassificationMails(Instant weekAgo, int pageNo, int pageSize, Category category) {
        return find("select c from classification c join fetch c.email e where e.receivedAt >= :weekAgo and " +
                        "c.category = :category order by e.receivedAt desc",
                Map.of("weekAgo", weekAgo, "category", category)).page(pageNo, pageSize).list();
    }

    public List<StatChip> getStats(Instant from) {
        return getEntityManager().createQuery("select new org.galactic.dto.view.StatChip(c.category, count(c)) " +
                        "from classification c join c.email e where e.receivedAt >= :since group by c.category", StatChip.class)
                .setParameter("since", from)
                .getResultList();
    }
}
