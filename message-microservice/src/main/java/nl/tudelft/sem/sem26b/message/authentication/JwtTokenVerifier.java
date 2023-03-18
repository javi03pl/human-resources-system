package nl.tudelft.sem.sem26b.message.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Verifies the JWT token in the request for validity.
 */
@Component
public class JwtTokenVerifier {
    @Value("${jwt.secret}")  // automatically loads jwt.secret from resources/application.properties
    private transient String jwtSecret;

    /**
     * Validate the JWT token for expiration.
     */
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    /**
     * Gets the netId from token.
     *
     * @param token JWT token.
     * @return the netId as String.
     */
    public String getNetIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Returns expirationDate from token.
     *
     * @param token JWT token.
     * @return the Date.
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Gets the role from the token.
     *
     * @param token JWT token.
     * @return the role as String.
     */
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role").toString();
    }

    /**
     * Returns whether the token is expired.
     *
     * @param token JWT token.
     * @return bool.
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Gets calims.
     *
     * @param token JWT token.
     * @return the claims.
     */
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaims(token);
        System.out.println(claims.get("role"));
        return claimsResolver.apply(claims);
    }

    /**
     * Returns the claims from the token.
     *
     * @param token JWT token.
     * @return the claims.
     */
    private Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }
}
