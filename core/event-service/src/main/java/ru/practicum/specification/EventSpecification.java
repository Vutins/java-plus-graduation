package ru.practicum.specification;

import org.springframework.data.jpa.domain.Specification;

public interface EventSpecification {
    Specification<Event> toSpecification();
}
