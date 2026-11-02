package com.cafe.booking.Service;

import com.cafe.booking.entity.IdempotencyRecord;
import com.cafe.booking.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Supplier;

import static org.springframework.boot.logging.DeferredLog.replay;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    public record Result(Long resourceId, boolean replayed) {
    }

    public static class ClaimExists extends RuntimeException {
    }

    private final IdempotencyRepository repo;
    private final TransactionTemplate tx;

    public Result run(String username, String key, String endpoint, Object request, Supplier<Long> action) {
        if (key.length() > 64)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idempotency-key is too long");
        }

        String hash = sha256(String.valueOf(request));
        try
        {
            Long id = tx.execute(status -> {
                IdempotencyRecord rec = new IdempotencyRecord();
                rec.setUsername(username);
                rec.setIdemKey(key);
                rec.setEndpoint(endpoint);
                rec.setRequestHash(hash);

                try
                {
                    repo.saveAndFlush(rec);
                }
                catch (DataIntegrityViolationException ex) {
                    throw new ClaimExists();
                }
                Long resourceId = action.get();
                rec.setResourceId(resourceId);
                return resourceId;
            });

            return new Result(id, false);
        }
        catch (ClaimExists duplicate) {
            return replay(username, key, endpoint, hash);
        }
    }

    private Result replay(String username, String key, String endpoint, String hash) {

        IdempotencyRecord rec = tx.execute(s ->
                repo.findByUsernameAndIdemKeyAndEndpoint(username, key, endpoint).orElse(null));

        if (rec == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Please retry the request");

        if(!rec.getRequestHash().equals(hash))
           throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                   "This Idempotency-key was already used with a different request");

        return new Result(rec.getResourceId(),true);
    }

    private static String sha256(String s){
        try{
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(s.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException ex){
            throw new IllegalStateException(ex);
        }
    }
}
