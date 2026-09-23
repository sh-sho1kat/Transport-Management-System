package com.tms.controller;

import com.tms.dto.response.Responses.*;
import com.tms.service.ReportingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class ReportController {
  private final ReportingService service;

  public ReportController(ReportingService service) {
    this.service = service;
  }

  @GetMapping("/reports/occupancy")
  public PageResult<Occupancy> occupancy(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.occupancy(page, size);
  }

  @GetMapping("/audit")
  public PageResult<AuditView> audit(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.audit(page, size);
  }
}
