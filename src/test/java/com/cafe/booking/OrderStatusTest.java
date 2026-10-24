package com.cafe.booking;

import com.cafe.booking.entity.Enum.OrderStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderStatusTest {

        @ParameterizedTest(name = "{0} -> {1} allowed={2}")
        @CsvSource({
                "PLACED,PLACED,false",       "PLACED,PREPARING,true",
                "PLACED,SERVED,false",       "PLACED,CANCELLED,true",
                "PREPARING,PLACED,false",    "PREPARING,PREPARING,false",
                "PREPARING,SERVED,true",     "PREPARING,CANCELLED,true",
                "SERVED,PLACED,false",       "SERVED,PREPARING,false",
                "SERVED,SERVED,false",       "SERVED,CANCELLED,false",
                "CANCELLED,PLACED,false",    "CANCELLED,PREPARING,false",
                "CANCELLED,SERVED,false",    "CANCELLED,CANCELLED,false"
        })
        void transitionRules(OrderStatus from, OrderStatus to, boolean allowed) {
            assertEquals(allowed, from.canMoveTo(to));
        }
    }



