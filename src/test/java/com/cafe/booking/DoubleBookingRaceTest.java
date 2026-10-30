package com.cafe.booking;

import com.cafe.booking.Service.ReservationService;
import com.cafe.booking.dto.ReservationRequest;
import com.cafe.booking.entity.Enum.ReservationStatus;
import com.cafe.booking.entity.Enum.Role;
import com.cafe.booking.entity.UserEntity;
import com.cafe.booking.repository.ReservationRepository;
import com.cafe.booking.repository.TableRepository;
import com.cafe.booking.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class DoubleBookingRaceTest {

    private static final String USER = "race-user";
    private static final int THREADS = 8;
    private static final int ROUNDS = 10;

    @Autowired ReservationService reservationService;
    @Autowired ReservationRepository reservationRepo;
    @Autowired
    TableRepository tableRepo;
    @Autowired
    UserRepository userRepo;


    private final List<LocalDate> usedDates = new ArrayList<>();

    @BeforeEach
    void ensureUser(){
        if(userRepo.findByUsername(USER).isEmpty()){
            UserEntity u = new UserEntity();
            u.setUsername(USER);
            u.setPassword("not-used");
            u.setRole(Role.USER);
            userRepo.save(u);
        }
    }

    @AfterEach
    void cleanup(){
        reservationRepo.findAll().stream()
                .filter(r -> usedDates.contains(r.getReservationDate()))
                .forEach(reservationRepo::delete);
        usedDates.clear();
    }

    @Test
    void parallelBookingOfOneSlotMustProduceExactlyOneBoking() throws Exception{
        Long tableId = tableRepo.findAll().get(0).getId();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        int doubleBookedRounds = 0;
        int roundsWithOneSuccess = 0;

        for(int round = 0; round<ROUNDS;round++){

            LocalDate date = LocalDate.now().plusDays(90 + round);
            usedDates.add(date);
            ReservationRequest req = new ReservationRequest(
                    tableId,date, LocalTime.of(18,0),LocalTime.of(19,0),2
            );

            CountDownLatch ready = new CountDownLatch(THREADS);
            CountDownLatch go = new CountDownLatch(1);

            List<Future<Boolean>> results = new ArrayList<>();

            for(int i = 0; i < THREADS;i++){
                results.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();

                    try{
                        reservationService.create(USER,req);
                        return true;
                    }
                    catch(ResponseStatusException ex){
                        return false;
                    }
                }));

            }
              ready.await();
            go.countDown();

            int successes = 0;
            for(Future<Boolean> f : results){
                if(f.get(30, TimeUnit.SECONDS)) successes++;
            }

            long rows = reservationRepo.countByTable_IdAndReservationDateAndStatus(
                    tableId,date, ReservationStatus.CONFIRMED);
            System.out.printf("round %d: %d calls succeeded, %d confirmed rows%n",round,successes,rows);
            if(rows>1) doubleBookedRounds++;
            if(successes == 1) roundsWithOneSuccess++;
        }
        pool.shutdown();

        assertEquals(0,doubleBookedRounds,"rounds that ended with doublebooking");
        assertEquals(ROUNDS,roundsWithOneSuccess,"rounds where exactly one request succeeded");
    }
}
