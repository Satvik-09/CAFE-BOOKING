package com.cafe.booking.entity.Enum;

public enum OrderStatus {
      PLACED,PREPARING,SERVED,CANCELLED;

      public boolean canMoveTo(OrderStatus next){
            return switch (this) {
                  case PLACED    -> next == PREPARING || next == CANCELLED;
                  case PREPARING -> next == SERVED || next == CANCELLED;
                  case SERVED, CANCELLED -> false;
            };
      }
      }

