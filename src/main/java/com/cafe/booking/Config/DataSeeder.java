package com.cafe.booking.Config;

import com.cafe.booking.entity.CafeTable;
import com.cafe.booking.entity.Enum.Category;
import com.cafe.booking.entity.MenuItem;
import com.cafe.booking.repository.TableRepository;
import com.cafe.booking.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner{

    private final MenuItemRepository menuRepo;
    private final TableRepository tableRepo;

    @Override
    public void run(String... args) {
       seedMenu();
       seedTableMenu();
    }

    private void seedMenu(){
        if(menuRepo.count() > 0) return;

        menuRepo.saveAll(List.of(
                item("Cappuccino", "120.00", Category.COFFEE),
                item("Latte", "140.00", Category.COFFEE),
                item("Masala Chai", "60.00", Category.TEA),
                item("Green Tea", "80.00", Category.TEA),
                item("Veg Sandwich", "150.00", Category.SNACK),
                item("Brownie", "110.00", Category.DESERT)));
    }

    private void seedTableMenu(){
        if(tableRepo.count() > 0) return;

        for(int i = 1; i <= 5; i++){
            CafeTable T = new CafeTable();
            T.setTableNumber(i);
            T.setCapacity(i <= 3 ? 2 : 4);
            tableRepo.save(T);
        }
    }

        private MenuItem item(String name, String price, Category c){
            MenuItem m = new MenuItem();
            m.setName(name);
            m.setPrice(new BigDecimal(price));
            m.setCategory(c);
            return m;
        }

}
