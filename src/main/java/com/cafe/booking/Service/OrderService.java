package com.cafe.booking.Service;

import com.cafe.booking.dto.OrderItemRequest;
import com.cafe.booking.dto.OrderRequest;
import com.cafe.booking.dto.OrderResponse;
import com.cafe.booking.entity.Enum.OrderStatus;
import com.cafe.booking.entity.MenuItem;
import com.cafe.booking.entity.Order;
import com.cafe.booking.entity.OrderItem;
import com.cafe.booking.entity.UserEntity;
import com.cafe.booking.repository.MenuItemRepository;
import com.cafe.booking.repository.OrderRepository;
import com.cafe.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private OrderRepository orderRepo;
    private MenuItemRepository menuRepo;
    private UserRepository userRepo;

    @Transactional
    public OrderResponse place(String username, OrderRequest req) {
        UserEntity user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        Order order = new Order();
        order.setUser(user);


        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest line : req.items()) {
            MenuItem menuItem = menuRepo.findById(line.menuitemid())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "menu item not found" + line.menuitemid()));

            if (!menuItem.isAvailable()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "menu item not available" + menuItem.getName());
            }
            OrderItem item = new OrderItem();
            item.setMenu(menuItem);
            item.setQuantity(line.quantity());
            item.setPriceAtOrder(menuItem.getPrice());
            order.addItem(item);

            total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
            order.setTotalAmount(total);

            return OrderResponse.from(orderRepo.save(order));

    }

        @Transactional(readOnly = true)
                public List<OrderResponse> mine(String username){
            UserEntity user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"username not found"));

            return orderRepo.findByUserIdOrderByCreatedAtDesc(user.getId())
                    .stream().map(OrderResponse::from).toList();
    }


        @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus next){
        Order order = orderRepo.findDetailedById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found"));
        if(!order.getStatus().canMoveTo(next)){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Cannot move order from" + order.getStatus()
            + "to" + next);
        }
        order.setStatus(next);
        return OrderResponse.from(order);
        }
}
