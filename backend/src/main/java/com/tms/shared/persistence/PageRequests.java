package com.tms.shared.persistence;

import com.tms.shared.error.ApiException;
import org.springframework.data.domain.*;

public final class PageRequests {
  private PageRequests() {}

  public static Pageable paging(int page, int size, String field) {
    if (page < 0 || size < 1 || size > 100)
      throw ApiException.invalid("Page must be nonnegative and size between 1 and 100.");
    return PageRequest.of(page, size, Sort.by(field).and(Sort.by("id")));
  }
}
