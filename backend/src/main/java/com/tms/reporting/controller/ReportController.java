package com.tms.reporting.controller;

import com.tms.audit.dto.response.AuditView;
import com.tms.reporting.dto.response.Occupancy;
import com.tms.reporting.service.ReportingService;
import com.tms.shared.api.response.PageResult;
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
