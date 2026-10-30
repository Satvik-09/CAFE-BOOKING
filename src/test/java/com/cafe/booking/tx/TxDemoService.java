package com.cafe.booking.tx;

import com.cafe.booking.entity.Enum.Category;
import com.cafe.booking.entity.MenuItem;
import com.cafe.booking.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TxDemoService {

    private final MenuItemRepository menuRepo;
    private final TxInnerService inner;

    static MenuItem item(String name){
        MenuItem m = new MenuItem();
        m.setName(name);
        m.setPrice(new BigDecimal("10.00"));
        m.setCategory(Category.TEA);
        return m;
    }

    @Transactional //bydefault mysql has propogation set as REQUIRED
    public void saveThenRuntimeFail(String name)
    {
        menuRepo.save(item(name));
        throw new IllegalStateException("boom");
    }

    @Transactional
    public void saveThenCheckedFail(String name)
    throws Exception
    {
        menuRepo.save(item(name));
        throw new Exception("boom");
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveThenCheckedFailWithRollbackFor(String name)
    throws Exception
    {
        menuRepo.save(item(name));
        throw new Exception("boom");
    }
      // not annotated : the call below goes through 'this' its in same class so obj not require, proxy never sees it
    public void selfInvocation(String name){
        saveThenRuntimeFail(name);
    }

     @Transactional
    public void outerFailsAfterInner(String outerName,String innerName, boolean innerRequiresNew){
        menuRepo.save(item(outerName));

        if(innerRequiresNew){
            inner.SaveInNewTx(item(innerName));
        }
        else {
            inner.SaveInSameTx(item(innerName));
        }

        throw new IllegalStateException("OUTER FAILS");
     }

}
