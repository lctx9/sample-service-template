package com.example.sample.api;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.web.ApiResponse;
import com.example.common.web.PageResponse;
import com.example.sample.api.dto.CreateItemRequest;
import com.example.sample.api.dto.ItemResponse;
import com.example.sample.application.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Items", description = "Sample CRUD API")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an item")
    public ApiResponse<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
        return ApiResponse.ok(ItemResponse.from(itemService.create(request.name(), request.description())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an item by id")
    public ApiResponse<ItemResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(ItemResponse.from(itemService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List items with paging and sorting")
    public ApiResponse<PageResponse<ItemResponse>> list(
            @ParameterObject @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(itemService.list(pageable), ItemResponse::from));
    }
}
