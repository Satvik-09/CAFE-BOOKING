package com.cafe.booking.entity;

import com.cafe.booking.entity.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal TotalAmount = BigDecimal.ZERO;

    private LocalDateTime CreatedAt;

    @PrePersist
      void Oncreate(){
        CreatedAt = LocalDateTime.now();
    }

    public void AddItem(OrderItem item){
        items.add(item);
        item.setOrder(this);
    }
}
