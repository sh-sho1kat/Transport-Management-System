package com.tms.controller;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import com.tms.exception.ApiException;
import com.tms.service.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
  private final IdentityService service;
  private final AuthenticationManager auth;
  private final SecurityContextRepository contexts;
  private final RateLimiter limiter;

  public AuthController(
      IdentityService s, AuthenticationManager a, SecurityContextRepository c, RateLimiter l) {
    service = s;
    auth = a;
    contexts = c;
    limiter = l;
  }

  @GetMapping("/auth/csrf")
  public Csrf csrf(CsrfToken t) {
    return new Csrf(t.getToken(), t.getHeaderName());
  }

  @PostMapping("/auth/register")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public User register(@Valid @RequestBody Register r, HttpServletRequest req) {
    limiter.check("register:" + req.getRemoteAddr(), 10);
    return service.register(r);
  }

  @PostMapping("/auth/login")
  public User login(@Valid @RequestBody Login r, HttpServletRequest req, HttpServletResponse res) {
    limiter.check("login:" + req.getRemoteAddr(), 20);
    try {
      var result =
          auth.authenticate(
              new UsernamePasswordAuthenticationToken(
                  r.email().trim().toLowerCase(java.util.Locale.ROOT), r.password()));
      if (req.getSession(false) != null) req.changeSessionId();
      var context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(result);
      SecurityContextHolder.setContext(context);
      contexts.saveContext(context, req, res);
      new HttpSessionCsrfTokenRepository().saveToken(null, req, res);
      return service.me();
    } catch (org.springframework.security.core.AuthenticationException e) {
      throw new ApiException(401, "INVALID_CREDENTIALS", "Invalid email or password");
    }
  }

  @PostMapping("/auth/logout")
  public Message logout(HttpServletRequest req) {
    if (req.getSession(false) != null) req.getSession(false).invalidate();
    SecurityContextHolder.clearContext();
    return new Message("Signed out");
  }

  @GetMapping("/me")
  public User me() {
    return service.me();
  }

  @PatchMapping("/me")
  public User profile(@Valid @RequestBody Profile r) {
    return service.profile(r);
  }

  @PostMapping("/auth/password-reset-requests")
  public Message recovery(@Valid @RequestBody Recovery r, HttpServletRequest req) {
    limiter.check("recovery:" + req.getRemoteAddr(), 5);
    service.requestReset(r.email());
    return new Message("If the account exists, recovery instructions will be sent");
  }

  @PostMapping("/auth/password-resets")
  public Message reset(@Valid @RequestBody Reset r, HttpServletRequest req) {
    limiter.check("reset:" + req.getRemoteAddr(), 10);
    service.reset(r);
    return new Message("Password updated. Sign in again");
  }

  @GetMapping("/admin/staff")
  public java.util.List<User> staff() {
    return service.staff();
  }

  @PostMapping("/admin/staff")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public User staff(@Valid @RequestBody Staff r) {
    return service.staff(r);
  }

  @PatchMapping("/admin/staff/{id}/active")
  public User active(@PathVariable java.util.UUID id, @Valid @RequestBody Active r) {
    return service.active(id, r);
  }
}
