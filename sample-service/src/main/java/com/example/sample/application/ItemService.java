package com.example.sample.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.common.error.BusinessException;
import com.example.sample.domain.Item;
import com.example.sample.domain.ItemCreatedEvent;
import com.example.sample.domain.ItemErrorCode;
import com.example.sample.infrastructure.persistence.ItemRepository;

/**
 * Business logic lives here. Controllers stay thin and only call services.
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ItemService(ItemRepository itemRepository, ApplicationEventPublisher eventPublisher) {
        this.itemRepository = itemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Item create(String name, String description) {
        String trimmedName = name.trim();
        if (itemRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new BusinessException(ItemErrorCode.ITEM_NAME_DUPLICATED);
        }
        Item item = itemRepository.save(new Item(trimmedName, description));
        eventPublisher.publishEvent(new ItemCreatedEvent(item.getId(), item.getName()));
        return item;
    }

    @Transactional(readOnly = true)
    public Item getById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Item> list(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }
}
