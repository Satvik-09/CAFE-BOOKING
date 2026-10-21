package com.cafe.booking.Service;

import com.cafe.booking.dto.*;
import com.cafe.booking.entity.*;
import com.cafe.booking.entity.Enum.Category;
import com.cafe.booking.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuItemRepository repo;

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> list(Category category, Pageable pageable){
        Page<MenuItem> page = (category == null)
                ? repo.findAll(pageable)
                : repo.findByCategory(category,pageable);
                return page.map(MenuItemResponse::from);
    }

    @Transactional
    public  MenuItemResponse create(MenuItemRequest r){
        if(repo.existsByName(r.name())) // IF TRUE THEN ROLLBACK OPERATION
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Item already exsists");
        MenuItem m = new MenuItem();
        apply(m,r); //  BEGIN OPERATION
        return MenuItemResponse.from(repo.save(m));//COMMIT OPERATION
    }

    @Transactional
    public MenuItemResponse update(Long id,MenuItemRequest r){
        MenuItem m = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"item not found"));
        apply(m,r);
         return MenuItemResponse.from(m);

    }

    @Transactional
    public void delete(Long id){
        if(!repo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"item not found");
        repo.deleteById(id);
    }

    private void apply(MenuItem m, MenuItemRequest r){
        m.setName(r.name());
        m.setDescription(r.description());
        m.setPrice(r.price());
        m.setCategory(r.category());
        if(r.available() != null) m.setAvailable(r.available());
    }
}
