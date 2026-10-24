package com.cafe.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor
public class OrderItem {
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @ManyToOne(fetch = FetchType.LAZY,optional = false)
      @JoinColumn(name = "order_id",nullable = false)
      private Order order;

      @ManyToOne(fetch = FetchType.LAZY,optional = false)
      @JoinColumn(name = "menu_item_id",nullable = false)
      private MenuItem menuItem;

      @Column(nullable = false)
      private Integer quantity;

      @Column(nullable = false)
      private BigDecimal priceAtOrder;
}
