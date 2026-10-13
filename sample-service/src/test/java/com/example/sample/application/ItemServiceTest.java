package com.example.sample.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.common.error.BusinessException;
import com.example.sample.domain.Item;
import com.example.sample.domain.ItemCreatedEvent;
import com.example.sample.domain.ItemErrorCode;
import com.example.sample.infrastructure.persistence.ItemRepository;

/**
 * Unit test sample: test business logic only, mock everything else.
 * Name tests as: method_condition_expectedResult.
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ItemService itemService;

    @Test
    void create_newName_savesItemAndPublishesEvent() {
        when(itemRepository.existsByNameIgnoreCase("Book")).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item item = itemService.create("  Book  ", "A book");

        assertThat(item.getName()).isEqualTo("Book");
        ArgumentCaptor<ItemCreatedEvent> event = ArgumentCaptor.forClass(ItemCreatedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().name()).isEqualTo("Book");
    }

    @Test
    void create_duplicatedName_throwsAndDoesNotSave() {
        when(itemRepository.existsByNameIgnoreCase("Book")).thenReturn(true);

        assertThatThrownBy(() -> itemService.create("Book", null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ItemErrorCode.ITEM_NAME_DUPLICATED);
        verify(itemRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getById_unknownId_throwsNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ItemErrorCode.ITEM_NOT_FOUND);
    }
}
