package com.tms.service.impl;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import com.tms.entity.*;
import com.tms.entity.Types.Role;
import com.tms.exception.ApiException;
import com.tms.mapper.Views;
import com.tms.repository.*;
import com.tms.security.CurrentAccount;
import com.tms.service.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService implements IdentityService {
  private final AccountRepository accounts;
  private final ResetTokenRepository tokens;
  private final PasswordEncoder passwords;
  private final CurrentAccount current;
  private final AuditService audit;
  private final Clock clock;
  private final JavaMailSender mail;
  private final boolean mailEnabled;
  private final String from;
  private final String frontend;

  public AccountService(
      AccountRepository a,
      ResetTokenRepository t,
      PasswordEncoder p,
      CurrentAccount c,
      AuditService au,
      Clock cl,
      JavaMailSender m,
      @Value("${app.mail-enabled}") boolean enabled,
      @Value("${app.mail-from}") String from,
      @Value("${app.frontend-url}") String frontend) {
    accounts = a;
    tokens = t;
    passwords = p;
    current = c;
    audit = au;
    clock = cl;
    mail = m;
    mailEnabled = enabled;
    this.from = from;
    this.frontend = frontend;
  }

  public User register(Register r) {
    return Views.user(create(r.email(), r.password(), r.displayName(), r.phone(), Role.PASSENGER));
  }

  public Account create(String email, String password, String name, String phone, Role role) {
    email = email.trim().toLowerCase(Locale.ROOT);
    if (accounts.existsByEmail(email))
      throw ApiException.conflict("ACCOUNT_EXISTS", "An account already exists for that email");
    validatePassword(password);
    var a = new Account();
    a.setEmail(email);
    a.setPasswordHash(passwords.encode(password));
    a.setDisplayName(name.trim());
    a.setPhone(phone.trim());
    a.setRole(role);
    return accounts.saveAndFlush(a);
  }

  public User me() {
    return Views.user(current.get());
  }

  public User profile(Profile r) {
    var a = current.get();
    a.setDisplayName(r.displayName().trim());
    a.setPhone(r.phone().trim());
    return Views.user(a);
  }

  public User staff(Staff r) {
    var admin = current.require(Role.ADMIN);
    if (r.role() == Role.PASSENGER)
      throw ApiException.invalid("Use passenger registration for passenger accounts");
    var a = create(r.email(), r.password(), r.displayName(), r.phone(), r.role());
    audit.record(admin, "STAFF_CREATED", a.getId(), r.role().name());
    return Views.user(a);
  }

  public List<User> staff() {
    current.require(Role.ADMIN);
    return accounts.findAll().stream()
        .filter(a -> a.getRole() != Role.PASSENGER)
        .map(Views::user)
        .toList();
  }

  public User active(UUID id, Active r) {
    var actor = current.require(Role.ADMIN);
    if (actor.getId().equals(id) && !r.active())
      throw ApiException.invalid("You cannot deactivate your own account");
    var a = accounts.lockById(id).orElseThrow(ApiException::missing);
    if (a.getRole() == Role.PASSENGER)
      throw ApiException.invalid("This endpoint manages staff only");
    a.setActive(r.active());
    a.setAuthVersion(a.getAuthVersion() + 1);
    audit.record(actor, r.active() ? "STAFF_ACTIVATED" : "STAFF_DEACTIVATED", id, r.reason());
    return Views.user(a);
  }

  public void requestReset(String email) {
    var found = accounts.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    if (found.isEmpty() || !found.get().getActive() || !mailEnabled) return;
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    var token = new ResetToken();
    token.setAccount(found.get());
    token.setTokenHash(hash(raw));
    token.setExpiresAt(clock.instant().plusSeconds(1800));
    tokens.saveAndFlush(token);
    var msg = new SimpleMailMessage();
    msg.setFrom(from);
    msg.setTo(found.get().getEmail());
    msg.setSubject("Reset your Wayline password");
    msg.setText(
        "Reset your password within 30 minutes: "
            + frontend
            + "/reset-password?token="
            + raw
            + "\nIf you did not request this, ignore this message.");
    try {
      mail.send(msg);
    } catch (org.springframework.mail.MailException e) {
      token.setConsumed(true);
      org.slf4j.LoggerFactory.getLogger(getClass()).warn("Password recovery delivery unavailable");
    }
  }

  public void reset(Reset r) {
    var t =
        tokens
            .findByTokenHash(hash(r.token()))
            .filter(x -> !x.getConsumed() && x.getExpiresAt().isAfter(clock.instant()))
            .orElseThrow(() -> ApiException.invalid("Reset link is invalid or expired"));
    validatePassword(r.password());
    var a = accounts.lockById(t.getAccount().getId()).orElseThrow(ApiException::missing);
    a.setPasswordHash(passwords.encode(r.password()));
    a.setAuthVersion(a.getAuthVersion() + 1);
    t.setConsumed(true);
    audit.record(a, "PASSWORD_RESET", a.getId(), null);
  }

  public static void validatePassword(String s) {
    if (s.length() < 10 || s.getBytes(StandardCharsets.UTF_8).length > 72)
      throw ApiException.invalid(
          "Password must have at least 10 characters and at most 72 UTF-8 bytes");
  }

  public static String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
