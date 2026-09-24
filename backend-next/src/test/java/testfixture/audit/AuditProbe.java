package testfixture.audit;

import com.tms.common.audit.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "audit_probe")
public class AuditProbe extends AuditableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String label;

  public AuditProbe() {}

  public AuditProbe(String label) {
    this.label = label;
  }

  public Long getId() {
    return id;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
