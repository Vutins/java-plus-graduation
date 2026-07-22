package ru.practicum.explore_with_me.event.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explore_with_me.event.entity.Event;

public interface EventSpecification {
    Specification<Event> toSpecification();
}
