package com.cafe.booking.Service;

import com.cafe.booking.dto.ReservationRequest;
import com.cafe.booking.dto.ReservationResponse;
import com.cafe.booking.entity.CafeTable;
import com.cafe.booking.entity.Enum.ReservationStatus;
import com.cafe.booking.entity.Reservation;
import com.cafe.booking.entity.UserEntity;
import com.cafe.booking.repository.TableRepository;
import com.cafe.booking.repository.ReservationRepository;
import com.cafe.booking.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repo;
    private final UserRepository userRepo;
    private final TableRepository tablerepo;

    @Transactional
    public ReservationResponse create(String username, ReservationRequest req){
        UserEntity user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not Found"));

        CafeTable table = tablerepo.findById( req.tableId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Table not Found"));

        if(!req.endTime().isAfter(req.startTime())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"End time must be after start time");

        }

        if(req.partySize() > table.getCapacity()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"party too large for this table");
        }

        if(repo.existsOverlap(table.getId(),req.date(),req.startTime(),req.endTime(), ReservationStatus.CONFIRMED)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Table already booked for that time");
        }

        Reservation res = new Reservation();
        res.setUser(user);
        res.setTable(table);
        res.setReservationDate(req.date());
        res.setStartTime(req.startTime());
        res.setEndTime(req.endTime());
        res.setPartySize(req.partySize());
        return ReservationResponse.from(repo.save(res));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> mine(String username){
        UserEntity user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return repo.findByUserIdOrderByReservationDateDescStartTimeDesc(user.getId())
                .stream().map(ReservationResponse::from).toList();
    }

    @Transactional
    public ReservationResponse cancel(String username,Long id){
        Reservation r = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Reservation not found"));
        if(!r.getUser().getUsername().equals(username)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"not your reservation");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.from(repo.save(r));
    }
}
