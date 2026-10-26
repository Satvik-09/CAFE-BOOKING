package com.cafe.booking.tx;

import com.cafe.booking.entity.MenuItem;
import com.cafe.booking.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TxInnerService {
         private final MenuItemRepository menuRepo;

         @Transactional(propagation = Propagation.REQUIRES_NEW)
         public void SaveInNewTx(MenuItem m){
             menuRepo.save(m);
         }

         @Transactional(propagation = Propagation.REQUIRED)
         public void SaveInSameTx(MenuItem m){
             menuRepo.save(m);
         }

}
