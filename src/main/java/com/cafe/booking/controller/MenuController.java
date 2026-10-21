package com.cafe.booking.controller;

import com.cafe.booking.Service.MenuService;
import com.cafe.booking.dto.MenuItemRequest;
import com.cafe.booking.dto.MenuItemResponse;
import com.cafe.booking.entity.Enum.Category;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService service;

    @GetMapping("/menu")
    public Page<MenuItemResponse> list(@RequestParam(required = false)Category category, Pageable pageable){
        return service.list(category,pageable);
    }

    @PostMapping("/admin/menu")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(@Valid @RequestBody MenuItemRequest r){
        return service.create(r);
    }

    @PutMapping("/admin/menu/{id}")
    public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest r){
        return service.update(id,r);
    }

    @DeleteMapping("/admin/menu/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    //2 times made the mistake for taking menuitemresponse instead of void for delete
    public  void delete(@PathVariable Long id){
          service.delete(id);
    }
}
