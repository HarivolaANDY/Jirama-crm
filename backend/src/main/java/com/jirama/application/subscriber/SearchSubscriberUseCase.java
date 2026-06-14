package com.jirama.application.subscriber;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.subscriber.Subscriber;
import com.jirama.domain.subscriber.SubscriberRepository;

import java.util.List;

/**
 * Use case for searching subscribers by various criteria.
 */
@UseCase
public class SearchSubscriberUseCase {

    private final SubscriberRepository subscriberRepository;

    public SearchSubscriberUseCase(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public record SearchQuery(
            String query,
            int page,
            int size
    ) {}

    public record SubscriberSummary(
            String id,
            String subscriberNumber,
            String fullName,
            String email,
            String phoneNumber,
            String status,
            String type
    ) {}

    public List<SubscriberSummary> execute(SearchQuery searchQuery) {
        List<Subscriber> subscribers = subscriberRepository.search(
                searchQuery.query(), searchQuery.page(), searchQuery.size()
        );

        return subscribers.stream()
                .map(s -> new SubscriberSummary(
                        s.getId().toString(),
                        s.getSubscriberNumber(),
                        s.getFullName(),
                        s.getEmail(),
                        s.getPhoneNumber(),
                        s.getStatus().name(),
                        s.getSubscriberType().name()
                ))
                .toList();
    }
}
