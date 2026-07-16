package ru.practicum.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.entity.Event;

public interface EventSpecification {
    Specification<Event> toSpecification();
}
