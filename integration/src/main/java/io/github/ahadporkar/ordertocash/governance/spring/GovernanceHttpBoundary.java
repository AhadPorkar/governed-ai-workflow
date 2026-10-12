package io.github.ahadporkar.ordertocash.governance.spring;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
/** Applies only to the added governance surface. Explicit origin, never inferred from Host. */
@Component
@ConditionalOnProperty(name="app.governance.enabled",havingValue="true")
public class GovernanceHttpBoundary extends OncePerRequestFilter {
    private final String allowedOrigin;
    public GovernanceHttpBoundary(@Value("${app.governance.allowed-origin}") String origin) {
        URI u=URI.create(origin);
        if(u.getHost()==null||!("http".equals(u.getScheme())||"https".equals(u.getScheme()))||u.getUserInfo()!=null||u.getRawQuery()!=null||u.getFragment()!=null||!(u.getPath()==null||u.getPath().isEmpty()))
            throw new IllegalArgumentException("Configure the exact governance origin without a path or trailing slash");
        allowedOrigin=origin;
    }
    @Override protected boolean shouldNotFilter(HttpServletRequest req){return !req.getRequestURI().substring(req.getContextPath().length()).startsWith("/governance/");}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException {
        res.setHeader("Cache-Control","no-store");res.setHeader("X-Content-Type-Options","nosniff");res.setHeader("X-Frame-Options","DENY");res.setHeader("Referrer-Policy","no-referrer");
        res.setHeader("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'");
        String origin=req.getHeader("Origin");
        if(!"GET".equals(req.getMethod())&&origin!=null&&!allowedOrigin.equals(origin)) {
            res.setStatus(403);res.setContentType("application/problem+json");res.getWriter().write("{\"type\":\"urn:governance:error:origin_denied\",\"title\":\"ORIGIN_DENIED\",\"status\":403}");return;
        }
        chain.doFilter(req,res);
    }
}
