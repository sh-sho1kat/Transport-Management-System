package com.tms.shared.api;

import com.tms.shared.api.response.PageResult;
import java.util.*;
import org.springframework.data.domain.Page;

public final class Pages {
  private Pages() {}

  public static <T, R> PageResult<R> page(Page<T> page, java.util.function.Function<T, R> mapper) {
    return new PageResult<>(
        page.getContent().stream().map(mapper).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
