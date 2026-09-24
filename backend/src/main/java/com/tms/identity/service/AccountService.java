package com.tms.identity.service;

import com.tms.audit.service.AuditService;
import com.tms.identity.domain.Role;
import com.tms.identity.dto.request.Profile;
import com.tms.identity.dto.request.Register;
import com.tms.identity.dto.request.Reset;
import com.tms.identity.dto.request.Staff;
import com.tms.identity.dto.response.User;
import com.tms.identity.entity.Account;
import com.tms.identity.entity.ResetToken;
import com.tms.identity.mapper.IdentityMapper;
import com.tms.identity.repository.AccountRepository;
import com.tms.identity.repository.ResetTokenRepository;
import com.tms.identity.security.CurrentAccount;
import com.tms.identity.security.Permission;
import com.tms.identity.security.PublicDemoPolicy;
import com.tms.shared.api.request.Active;
import com.tms.shared.error.ApiException;
import com.tms.shared.security.Digests;
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
  private final PublicDemoPolicy demo;

  public AccountService(
      AccountRepository a,
      ResetTokenRepository t,
      PasswordEncoder p,
      CurrentAccount c,
      AuditService au,
      Clock cl,
      JavaMailSender m,
      PublicDemoPolicy demo,
      @Value("${app.mail-enabled}") boolean enabled,
      @Value("${app.mail-from}") String from,
      @Value("${app.frontend-url}") String frontend) {
    this.demo = demo;
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
    demo.requireEditable(r.email().trim());
    return IdentityMapper.user(
        create(r.email(), r.password(), r.displayName(), r.phone(), Role.PASSENGER));
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
    return IdentityMapper.user(current.get());
  }

  public User profile(Profile r) {
    var a = current.get();
    demo.requireEditable(a.getEmail());
    a.setDisplayName(r.displayName().trim());
    a.setPhone(r.phone().trim());
    return IdentityMapper.user(a);
  }

  public User staff(Staff r) {
    var admin = current.require(Permission.STAFF_MANAGE);
    if (r.role() == Role.PASSENGER)
      throw ApiException.invalid("Use passenger registration for passenger accounts");
    var a = create(r.email(), r.password(), r.displayName(), r.phone(), r.role());
    audit.record(admin, "STAFF_CREATED", a.getId(), r.role().name());
    return IdentityMapper.user(a);
  }

  public List<User> staff() {
    current.require(Permission.STAFF_MANAGE);
    return accounts.findAll().stream()
        .filter(a -> a.getRole() != Role.PASSENGER)
        .map(IdentityMapper::user)
        .toList();
  }

  public User active(UUID id, Active r) {
    var actor = current.require(Permission.STAFF_MANAGE);
    if (actor.getId().equals(id) && !r.active())
      throw ApiException.invalid("You cannot deactivate your own account");
    var a = accounts.lockById(id).orElseThrow(ApiException::missing);
    demo.requireEditable(a.getEmail());
    if (a.getRole() == Role.PASSENGER)
      throw ApiException.invalid("This endpoint manages staff only");
    a.setActive(r.active());
    a.setAuthVersion(a.getAuthVersion() + 1);
    audit.record(actor, r.active() ? "STAFF_ACTIVATED" : "STAFF_DEACTIVATED", id, r.reason());
    return IdentityMapper.user(a);
  }

  public void requestReset(String email) {
    if (demo.protectedAccount(email.trim())) return;
    var found = accounts.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    if (found.isEmpty() || !found.get().getActive() || !mailEnabled) return;
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    var token = new ResetToken();
    token.setAccount(found.get());
    token.setTokenHash(Digests.hash(raw));
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
            .findByTokenHash(Digests.hash(r.token()))
            .filter(x -> !x.getConsumed() && x.getExpiresAt().isAfter(clock.instant()))
            .orElseThrow(() -> ApiException.invalid("Reset link is invalid or expired"));
    validatePassword(r.password());
    var a = accounts.lockById(t.getAccount().getId()).orElseThrow(ApiException::missing);
    demo.requireEditable(a.getEmail());
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
}
